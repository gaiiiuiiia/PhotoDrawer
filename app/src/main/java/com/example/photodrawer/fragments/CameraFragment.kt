package com.example.photodrawer.fragments

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.TextureView
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.photodrawer.CameraSession
import com.example.photodrawer.MainActivity
import com.example.photodrawer.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class CameraFragment(
    private val act: Activity
): Fragment()
{
    private lateinit var myView: View
    private lateinit var cameraSession: CameraSession
    private lateinit var btnTakePhoto: FloatingActionButton

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View
    {
        myView = inflater.inflate(R.layout.fragment_camera, container, false)
        val tvCamera: TextureView = myView.findViewById(R.id.tvCamera)
        btnTakePhoto = myView.findViewById(R.id.btn_takePhoto)
        cameraSession = CameraSession(act, tvCamera)

        btnTakePhoto.setOnClickListener {
            cameraSession.takePicture()
        }

        return myView
    }

    override fun onPause() {
        cameraSession.pauseSession()
        super.onPause()
    }

    override fun onResume() {
        cameraSession.resumeSession()
        super.onResume()
    }
}