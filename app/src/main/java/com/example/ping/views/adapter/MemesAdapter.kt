package com.example.ping.views.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.meme_lib.network.Meme
import com.example.ping.R
import com.example.ping.views.activity.MemesActivity
import com.example.ping.views.activity.SavedMemesActivity

class MemesAdapter(
    private val context: Context,
    private val activity: Activity
) : RecyclerView.Adapter<MemesAdapter.MemeViewHolder>() {
    private val TAG = "MemesAdapter"

    private val diffCallBack = object : DiffUtil.ItemCallback<Meme>(){
        override fun areItemsTheSame(oldItem: Meme, newItem: Meme): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: Meme, newItem: Meme): Boolean {
            return oldItem == newItem
        }

    }

    private val diffCallBack2 = object : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
            return oldItem == newItem
        }
    }

    private val differ2 = AsyncListDiffer(this, diffCallBack2)
    var savedMemes : List<String>
    get() = differ2.currentList
    set(value){differ2.submitList(value)}


    private val differ = AsyncListDiffer(this, diffCallBack)

    var memes: List<Meme>
    get() = differ.currentList
    set(value){differ.submitList((value))}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MemeViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.meme_design, parent, false)
        return MemeViewHolder(view)
    }

    override fun onBindViewHolder(holder: MemeViewHolder, position: Int) {
        if (activity is MemesActivity){
            holder.saveOrDeleteMeme.text = "Save "
            holder.separator.visibility = View.VISIBLE
            Glide.with(context)
                .load(memes[position].preview[memes[position].preview.size - 1])
                .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                .fitCenter()
                .into(holder.ivMeme)
        }
        else{
            Glide.with(context)
                .load(savedMemes[position])
                .override(com.bumptech.glide.request.target.Target.SIZE_ORIGINAL)
                .fitCenter()
                .into(holder.ivMeme)
            holder.saveOrDeleteMeme.text = "Remove"
        }

        holder.saveOrDeleteMeme.setOnClickListener{
            if (activity is MemesActivity){
                activity.saveMemeToDatabase(memes[position].url)
            }
            else if (activity is SavedMemesActivity){
                activity.deleteMeme(savedMemes[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return if (activity is MemesActivity) memes.size
        else savedMemes.size
    }

    inner class MemeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val ivMeme : ImageView = itemView.findViewById(R.id.iv_meme)
        val saveOrDeleteMeme: TextView = itemView.findViewById(R.id.tv_save_or_delete_meme)
        val separator: View = itemView.findViewById(R.id.meme_separator)
    }
}