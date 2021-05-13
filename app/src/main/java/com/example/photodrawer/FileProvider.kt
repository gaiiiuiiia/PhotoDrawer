package com.example.photodrawer

import java.io.File

object FileProvider
{
    fun getFiles(file: File): ArrayList<File>
    {
        val result: ArrayList<File> = ArrayList()
        val files: Array<File>? = file.listFiles()
        files?.let {
            for (singleFile: File in files) {
                if (singleFile.isDirectory && !singleFile.isHidden) {
                    result.add(singleFile)
                }
            }

            for (singleFile: File in files) {
                if (FileAllowManager.isAllowedImage(singleFile.name)) {
                    result.add(singleFile)
                }
            }
        }

        return result
    }
}