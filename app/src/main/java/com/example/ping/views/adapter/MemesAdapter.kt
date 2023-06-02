package com.example.ping.views.adapter

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.provider.MediaStore.*
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.meme_lib.network.Meme
import com.example.ping.R
import com.example.ping.views.activity.MemesActivity
import com.example.ping.views.activity.SavedMemesActivity
import java.io.*
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.util.concurrent.Executors

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
            holder.saveOrDeleteMeme.text = "Save"
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

        holder.downloadMeme.setOnClickListener {
            Toast.makeText(activity, "Downloading...", Toast.LENGTH_SHORT).show()
            if (activity is MemesActivity) {
                downloadMeme(memes[position].url)
            }
            else if (activity is SavedMemesActivity) {
                downloadMeme(savedMemes[position])
            }
        }

        holder.shareMeme.setOnClickListener {
            if (activity is MemesActivity) {
                activity.shareMeme(memes[position].url)
            }
            else if (activity is SavedMemesActivity) {
                activity.shareMeme(savedMemes[position])
            }
        }
    }

    private fun downloadMeme(url: String) {
        val myExecutor = Executors.newSingleThreadExecutor()
        val myHandler = Handler(Looper.getMainLooper())
        var mImage: Bitmap?

        myExecutor.execute {
            mImage = mLoad(url)
            myHandler.post {
                if(mImage != null){
                    mSaveMediaToStorage(mImage)
                }
            }
        }
    }

    private fun mLoad(string: String): Bitmap? {
        val url: URL = mStringToURL(string)!!
        val connection: HttpURLConnection?
        try {
            connection = url.openConnection() as HttpURLConnection
            connection.connect()
            val inputStream: InputStream = connection.inputStream
            val bufferedInputStream = BufferedInputStream(inputStream)
            return BitmapFactory.decodeStream(bufferedInputStream)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(activity, "Error Downloading!", Toast.LENGTH_LONG).show()
        }
        return null
    }

    private fun mStringToURL(string: String): URL? {
        try {
            return URL(string)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return null
    }

    private fun mSaveMediaToStorage(bitmap: Bitmap?) {
        val filename = "${System.currentTimeMillis()}.jpg"
        var fos: OutputStream? = null
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            activity.contentResolver?.also { resolver ->
                val contentValues = ContentValues().apply {
                    put(MediaColumns.DISPLAY_NAME, filename)
                    put(MediaColumns.MIME_TYPE, "image/jpg")
                    put(MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
                }
                val imageUri: Uri? = resolver.insert(Images.Media.EXTERNAL_CONTENT_URI, contentValues)
                fos = imageUri?.let { resolver.openOutputStream(it) }
            }
        } else {
            val imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val image = File(imagesDir, filename)
            fos = FileOutputStream(image)
        }
        fos?.use {
            bitmap?.compress(Bitmap.CompressFormat.JPEG, 100, it)
            Toast.makeText(activity, "Saved To Gallery!", Toast.LENGTH_SHORT).show()
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
        val downloadMeme: TextView = itemView.findViewById(R.id.tv_download_meme)
        val shareMeme: TextView = itemView.findViewById(R.id.tv_share_meme)
    }
}