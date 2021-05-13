package com.example.photodrawer

import java.io.File

interface OnFileSelectedListener
{
    fun onFileClicked(file: File)

    fun onFileLongClicked(file: File, position: Int)
}