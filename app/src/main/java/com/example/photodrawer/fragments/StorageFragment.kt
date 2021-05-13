package com.example.photodrawer.fragments

import android.app.Activity
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.photodrawer.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import kotlin.collections.ArrayList

class StorageFragment(
    private val act: Activity
): Fragment(), OnFileSelectedListener
{
    private lateinit var recyclerView: RecyclerView
    private lateinit var imgBack: ImageView
    private lateinit var tvPathHolder: TextView
    private lateinit var fileAdapter: FileAdapter
    private var fileList: ArrayList<File> = ArrayList()

    private lateinit var myView: View

    private lateinit var storage: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        myView = inflater.inflate(R.layout.fragment_storage, container, false)

        tvPathHolder = myView.findViewById(R.id.tv_pathHolder)
        imgBack = myView.findViewById(R.id.img_back)

        storage = getStorage(arguments)

        runtimePermission()

        tvPathHolder.text = storage.absolutePath

        return myView
    }

    private fun getStorage(arguments: Bundle?): File
    {
        var storage = File(activity?.getExternalFilesDir(Environment.DIRECTORY_PICTURES).toString())

        arguments?.let {
            if (arguments.getString("path") != null) {
                storage = File(arguments.getString("path"))
            }
        }

        return storage
    }

    private fun runtimePermission()
    {
        Dexter.withContext(context).withPermissions(
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_EXTERNAL_STORAGE
        ).withListener(object : MultiplePermissionsListener {
            override fun onPermissionsChecked(p0: MultiplePermissionsReport?) {
                displayFiles()
            }

            override fun onPermissionRationaleShouldBeShown(
                p0: MutableList<PermissionRequest>?,
                p1: PermissionToken?
            ) {
                p1?.continuePermissionRequest()
            }
        }).check()
    }

    private fun displayFiles()
    {
        recyclerView = myView.findViewById(R.id.recycler_storage)
        recyclerView.setHasFixedSize(true)
        recyclerView.layoutManager = GridLayoutManager(context, 2)
        fileList.addAll(FileProvider.getFiles(storage))
        fileAdapter = FileAdapter(context!!, fileList, this)
        recyclerView.adapter = fileAdapter
    }

    override fun onFileClicked(file: File)
    {
        if (file.isDirectory) {
            val bundle = Bundle()
            bundle.putString("path", file.absolutePath)
            val storageFragment = StorageFragment(act)
            storageFragment.arguments = bundle
            fragmentManager!!
                .beginTransaction()
                .replace(R.id.fragment_container, storageFragment)
                .addToBackStack(null)
                .commit()
        } else {
            fragmentManager!!
                .beginTransaction()
                .replace(R.id.fragment_container, DrawingFragment(act, file))
                .addToBackStack(null)
                .commit()
        }
    }

    override fun onFileLongClicked(file: File, position: Int)
    {
        // not implemented
    }
}