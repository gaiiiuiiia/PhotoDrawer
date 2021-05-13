package com.example.photodrawer.fragments

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.example.photodrawer.FileProvider
import com.example.photodrawer.PaintView
import com.example.photodrawer.R
import java.io.File

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
            paintView.init(paintView, activity, filePicture.absolutePath)
        } else {
            val lastPicture: File? = getLastPicture()

            if (lastPicture != null) {
                paintView.init(paintView, activity, lastPicture.absolutePath)
            } else {
                paintView.init(paintView)
            }
        }
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