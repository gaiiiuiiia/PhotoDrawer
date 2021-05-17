package com.example.photodrawer.fragments

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.Canvas
import android.icu.text.SimpleDateFormat
import android.icu.util.Calendar
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.photodrawer.FileProvider
import com.example.photodrawer.PaintView
import com.example.photodrawer.R
import com.example.photodrawer.extension.writeBitmap
import java.io.File
import java.util.*

class DrawingFragment(
    private val act: Activity,
    private val filePicture: File? = null
): Fragment()
{
    private lateinit var myView: View
    private lateinit var paintView: PaintView
    private lateinit var btnBack: Button
    private lateinit var btnSave: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        myView = inflater.inflate(R.layout.fragment_drawing, container, false)
        paintView = myView.findViewById(R.id.paintView)
        btnBack = myView.findViewById(R.id.btn_back)
        btnSave = myView.findViewById(R.id.btn_save)

        return myView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

        if (filePicture != null) {
            paintView.init(filePicture.absolutePath)
        } else {
            val lastPicture: File? = getLastPicture()

            if (lastPicture != null) {
                paintView.init(lastPicture.absolutePath)
            } else {
                paintView.init()
            }
        }

        btnBack.setOnClickListener {
            act.onBackPressed()
        }

        btnSave.setOnClickListener {

            val simpleDateFormat = SimpleDateFormat("dd-MM-yyyy-hh-mm-ss", Locale.getDefault())
            val name = "/" + simpleDateFormat.format(Calendar.getInstance().time) + "PhotoDrawer" + ".jpg"
            val file = File(
                act.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString(),
                name
            )

            file.writeBitmap(viewToBitmap(paintView), Bitmap.CompressFormat.JPEG, 85)
            
            Toast.makeText(context, R.string.saved_success, Toast.LENGTH_SHORT).show()
        }
    }

    private fun viewToBitmap(view: View): Bitmap
    {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }


    private fun getLastPicture(): File?
    {
        val pictureDir = act.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        pictureDir?.let {
            val files = FileProvider.getFiles(it)
            if (files.size != 0) {
                return files.last()
            }
        }

        return null
    }
}