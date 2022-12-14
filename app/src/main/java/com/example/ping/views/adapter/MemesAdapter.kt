package com.example.ping.views.adapter

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.ping.R
import com.example.ping.views.activity.MemesActivity
import com.example.ping.views.activity.SavedMemesActivity

class MemesAdapter(
    private val context: Context,
    private val activity: Activity
) : RecyclerView.Adapter<MemesAdapter.MemeViewHolder>() {

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

        if (activity is MemesActivity){
            holder.saveOrDeleteMeme.text = "Save Meme"
            holder.separator.visibility = View.GONE
        }
        else{
            holder.saveOrDeleteMeme.text = "Remove From Saved"
            if (position == itemCount-1){
                holder.separator.visibility = View.GONE
            }
            else{
                holder.separator.visibility = View.VISIBLE
            }
        }

        holder.saveOrDeleteMeme.setOnClickListener{
            if (activity is MemesActivity){
                activity.saveMemeToDatabase(memes[position])
            }
            else if (activity is SavedMemesActivity){
                activity.deleteMeme(memes[position])
            }
        }
    }

    override fun getItemCount(): Int {
        return memes.size
    }

    inner class MemeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val ivMeme : ImageView = itemView.findViewById(R.id.iv_meme)
        val saveOrDeleteMeme: TextView = itemView.findViewById(R.id.tv_save_or_delete_meme)
        val separator: View = itemView.findViewById(R.id.meme_separator)
    }
}