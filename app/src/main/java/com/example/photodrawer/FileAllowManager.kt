package com.example.photodrawer

object FileAllowManager
{
    private val allowedImageFormatList = arrayOf(
        ".jpeg", ".jpg", ".png",
    )

    fun isAllowedImage(fileName: String): Boolean {
        return allowedImageFormatList.any { format ->
            fileName.lowercase().endsWith(format)
        }
    }
}