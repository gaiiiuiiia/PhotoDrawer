package com.example.photodrawer

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.example.photodrawer.extension.rotate
import java.io.File
import java.util.*
import kotlin.math.abs

class PaintView (
    context: Context?,
    attrs: AttributeSet? = null)
    : View(context, attrs)
{
    private var mX = 0f
    private var mY = 0f
    private var mPath: Path? = null
    private val mPaint: Paint = Paint()
    private val paths = ArrayList<FingerPath>()
    private lateinit var mBitmap: Bitmap
    private val mBitmapPaint = Paint(Paint.DITHER_FLAG)
    private var strokeWidth = BRUSH_SIZE
    private var currentColor = DEFAULT_BG_COLOR
    private var mDestRect: Rect? = null

    companion object
    {
        var BRUSH_SIZE = 10
        const val DEFAULT_COLOR = Color.BLACK
        const val DEFAULT_BG_COLOR = Color.WHITE
        private const val TOUCH_TOLERANCE = 4f
    }

    init
    {
        mPaint.isAntiAlias = true
        mPaint.isDither = true
        mPaint.color = DEFAULT_COLOR
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeJoin = Paint.Join.ROUND
        mPaint.strokeCap = Paint.Cap.ROUND
        mPaint.xfermode = null
        mPaint.alpha = 0xff
    }

    fun init(surface: PaintView, activity: Activity? = null, fileName: String? = null)
    {
        mBitmap = if (fileName != null){
            val options = BitmapFactory.Options()
            options.inMutable = true
            mBitmap = BitmapFactory.decodeFile(fileName, options)

            if (activity != null) {
                mBitmap = mBitmap.rotate(CameraSession.ORIENTATIONS.get(activity.windowManager.defaultDisplay.rotation).toFloat())
            }
            mBitmap
        } else {
            Bitmap.createBitmap(surface.width, surface.height, Bitmap.Config.ARGB_8888)
        }
        mDestRect = Rect(0, 0, width, height)
    }

    override fun onDraw(canvas: Canvas)
    {
        canvas.save()
        mDestRect?.apply {
            right = width
            bottom = height
        }
        mDestRect?.let {
            canvas.drawBitmap(mBitmap, null, it, mBitmapPaint)
        }
        for (fp in paths) {
            mPaint.color = fp.color
            mPaint.strokeWidth = fp.strokeWidth.toFloat()
            canvas.drawPath(fp.path, mPaint)
        }
        canvas.restore()
    }

    private fun touchStart(x: Float, y: Float)
    {
        mPath = Path()
        mPath?.let {
            paths.add(FingerPath(currentColor, strokeWidth, it))
            it.reset()
            it.moveTo(x, y)
            mX = x
            mY = y
        }
    }

    private fun touchMove(x: Float, y: Float)
    {
        val dx = abs(x - mX)
        val dy = abs(y - mY)
        if (dx >= TOUCH_TOLERANCE || dy >= TOUCH_TOLERANCE) {
            mPath!!.quadTo(mX, mY, (x + mX) / 2, (y + mY) / 2)
            mX = x
            mY = y
        }
    }

    private fun touchUp()
    {
        mPath!!.lineTo(mX, mY)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean
    {
        val x = event.x
        val y = event.y
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                touchStart(x, y)
            }
            MotionEvent.ACTION_MOVE -> {
                touchMove(x, y)
            }
            MotionEvent.ACTION_UP -> {
                touchUp()
            }
        }
        invalidate()
        return true
    }
}