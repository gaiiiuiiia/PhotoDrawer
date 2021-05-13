package com.example.photodrawer

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class FileViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
{
    val tvName: TextView = itemView.findViewById(R.id.tv_fileName)
    val tvSize: TextView = itemView.findViewById(R.id.tv_fileSize)
    val container: CardView = itemView.findViewById(R.id.container)
    val imgFile: ImageView = itemView.findViewById(R.id.img_fileType)
}