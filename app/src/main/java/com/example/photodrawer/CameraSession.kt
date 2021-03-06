package com.example.photodrawer

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.hardware.camera2.CameraCaptureSession.CaptureCallback
import android.hardware.camera2.params.OutputConfiguration
import android.hardware.camera2.params.SessionConfiguration
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.media.Image
import android.media.ImageReader
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.photodrawer.Constants.REQUEST_CAMERA_PERMISSION
import com.example.photodrawer.extension.rotate
import com.example.photodrawer.extension.writeBitmap
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.util.*
import kotlin.collections.ArrayList


class CameraSession(
    private val activity: Activity,
    private val textureView: TextureView
) {
    private val cameraManager: CameraManager =
        activity.getSystemService(AppCompatActivity.CAMERA_SERVICE) as CameraManager

    companion object {
        val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }
    }

    private var cameraDevice: CameraDevice? = null
    private var previewWidth: Int = 0
    private var previewHeight: Int = 0

    private var backgroundHandler: Handler? = null
    private var backgroundThread: HandlerThread? = null

    private var textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            previewWidth = width
            previewHeight = height
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            previewWidth = width
            previewHeight = height
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private val stateCallback: CameraDevice.StateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            createCameraPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    fun resumeSession() {
        if (textureView.isAvailable) {
            openCamera()
        } else {
            textureView.surfaceTextureListener = textureListener
        }
        startBackgroundThread()
    }

    fun pauseSession() {
        textureView.surfaceTextureListener = null
        cameraDevice?.close()
        stopBackgroundThread()
    }

    private fun createCameraPreview()
    {
        val texture = textureView.surfaceTexture
        texture?.setDefaultBufferSize(previewWidth, previewHeight)
        val surface = Surface(texture)
        // ???????????? ???? ???????????????????????????? ?????????????????????? ?? ????????????
        val builder = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

        builder?.addTarget(surface)
        val stateCallback = object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                builder?.let {
                    it.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                    session.setRepeatingRequest(
                        it.build(),
                        null,
                        backgroundHandler
                    )
                }
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {}
        }

        cameraDevice?.createCaptureSession(
            SessionConfiguration(
                SessionConfiguration.SESSION_REGULAR,
                listOf(OutputConfiguration(surface)),
                activity.mainExecutor,
                stateCallback
            )
        )
    }

    private fun openCamera()
    {
        if (activity.checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            || activity.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_CAMERA_PERMISSION
            )
            return
        }

        getCameraId()?.let {
            cameraManager.openCamera(it, stateCallback, backgroundHandler)
        }
    }

    private fun getCameraId(): String?
    {
        return cameraManager.cameraIdList.first { id ->
            cameraManager.getCameraCharacteristics(id)
                .get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK
        }
    }

    fun takePicture() {
        cameraDevice?.let { cameraDevice ->
            try {
                val characteristics = cameraManager.getCameraCharacteristics(cameraDevice.id)
                val jpegSizes = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
                    ?.getOutputSizes(ImageFormat.JPEG)
                var width = 640
                var height = 480
                if (!jpegSizes.isNullOrEmpty()) {
                    width = jpegSizes[0].width
                    height = jpegSizes[0].height
                }
                val reader: ImageReader =
                    ImageReader.newInstance(width, height, ImageFormat.JPEG, 1)
                val outputSurfaces: MutableList<Surface> = ArrayList(2)
                outputSurfaces.add(reader.surface)
                outputSurfaces.add(Surface(textureView.surfaceTexture))
                val captureBuilder =
                    cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                captureBuilder.addTarget(reader.surface)
                captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)
                val rotation = activity.windowManager.defaultDisplay.rotation
                captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(rotation))
                val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.getDefault())
                val name = "/" + simpleDateFormat.format(Calendar.getInstance().time) + ".jpg"
                val file = File(
                    activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString(),
                    name
                )
                val readerListener: ImageReader.OnImageAvailableListener =
                    object : ImageReader.OnImageAvailableListener {
                        override fun onImageAvailable(reader: ImageReader) {
                            var image: Image? = null
                            try {
                                image = reader.acquireLatestImage()
                                val buffer: ByteBuffer = image.planes[0].buffer
                                val bytes = ByteArray(buffer.capacity())
                                buffer.get(bytes)
                                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                                    .rotate(ORIENTATIONS.get(activity.windowManager.defaultDisplay.rotation).toFloat())
                                saveBitmap(bitmap)
                            } catch (e: FileNotFoundException) {
                                Log.e(this@CameraSession.javaClass.simpleName, e.message, e)
                            } catch (e: IOException) {
                                Log.e(this@CameraSession.javaClass.simpleName, e.message, e)
                            } finally {
                                image?.close()
                            }
                        }

                        private fun saveBitmap(bitmap: Bitmap) {
                            file.writeBitmap(bitmap, Bitmap.CompressFormat.JPEG, 85)
                        }
                    }
                reader.setOnImageAvailableListener(readerListener, backgroundHandler)
                val captureListener: CaptureCallback = object : CaptureCallback() {
                    override fun onCaptureCompleted(
                        session: CameraCaptureSession,
                        request: CaptureRequest,
                        result: TotalCaptureResult
                    ) {
                        super.onCaptureCompleted(session, request, result)
                        Toast.makeText(activity, "Saved:$file", Toast.LENGTH_SHORT).show()
                        createCameraPreview()
                    }
                }
                cameraDevice.createCaptureSession(
                    outputSurfaces,
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            try {
                                session.capture(
                                    captureBuilder.build(),
                                    captureListener,
                                    backgroundHandler
                                )
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    },
                    backgroundHandler
                )
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("Camera Background")
        backgroundThread?.start()
        backgroundThread?.let { thread ->
            backgroundHandler = Handler(thread.looper)
        }
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }
}