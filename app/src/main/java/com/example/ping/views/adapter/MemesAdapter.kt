package com.example.ping.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ping.R
import com.example.ping.network.RandomMemeAPIModel

class MemesAdapter(
    private val context: Context,
    private val memes: List<RandomMemeAPIModel>
) : RecyclerView.Adapter<MemesAdapter.MemesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemesViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.meme_design, parent, false)
        return MemesViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemesViewHolder, position: Int) {
        Glide.with(context)
            .load(memes[position].url)
            .into(holder.imgMeme)
    }

    override fun getItemCount(): Int {
        return memes.size
    }

    class MemesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val imgMeme : ImageView = itemView.findViewById(R.id.img_meme)
    }
}