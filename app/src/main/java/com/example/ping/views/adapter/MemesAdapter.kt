package com.example.ping.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ping.R

class MemesAdapter(
    private val context: Context
) : RecyclerView.Adapter<MemesAdapter.MemeViewHolder>() {
    inner class MemeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val ivMeme : ImageView = itemView.findViewById(R.id.iv_meme)
    }

    private val diffCallBack = object : DiffUtil.ItemCallback<String>(){
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

    }
    private val differ = AsyncListDiffer(this, diffCallBack)

    var memes: List<String>
    get() = differ.currentList
    set(value){differ.submitList((value))}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.meme_design, parent, false)
        return MemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemeViewHolder, position: Int) {
        Glide.with(context)
            .load(memes[position])
            .into(holder.ivMeme)
    }

    override fun getItemCount(): Int {
        return memes.size
    }
}