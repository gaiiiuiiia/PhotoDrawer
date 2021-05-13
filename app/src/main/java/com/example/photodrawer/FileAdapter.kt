package com.example.photodrawer

import android.content.Context
import android.text.format.Formatter
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class FileAdapter(
    private val context: Context,
    private val fileList: List<File>,
    private val listener: OnFileSelectedListener,
    ) : RecyclerView.Adapter<FileViewHolder>()
{
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FileViewHolder
    {
        return FileViewHolder(LayoutInflater.from(context).inflate(R.layout.file_container, parent, false))
    }

    override fun onBindViewHolder(holder: FileViewHolder, position: Int)
    {
        val currentFile = fileList[position]

        holder.tvName.text = currentFile.name
        holder.tvName.isSelected = true

        var items = 0
        if (currentFile.isDirectory) {
            val files = currentFile.listFiles()
            files?.let {
                for (singleFile: File in files) {
                    if (!singleFile.isHidden) {
                        items++
                    }
                }
            }
            holder.tvSize.text = items.toString().plus(" files")
        } else {
            holder.tvSize.text = Formatter.formatShortFileSize(context, currentFile.length())
        }

        setImages(currentFile.name, holder)

        holder.container.setOnClickListener {
            listener.onFileClicked(currentFile)
        }

        holder.container.setOnLongClickListener {
            listener.onFileLongClicked(currentFile, position)
            true
        }
    }

    override fun getItemCount(): Int {
        return fileList.size
    }

    private fun setImages(fileName: String, holder: FileViewHolder)
    {
        if (FileAllowManager.isAllowedImage(fileName)) {
            holder.imgFile.setImageResource(R.drawable.ic_image)
        }
    }
}