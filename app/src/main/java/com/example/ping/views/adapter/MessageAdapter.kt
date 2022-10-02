package com.example.ping.views.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.net.Uri
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.ping.R
import com.example.ping.model.MessageModel
import com.example.ping.utils.Constants
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    private val context: Context,
    private val messagesList: ArrayList<MessageModel>,
    private val senderUid: String?,
    private val receiverUid: String?
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2
    private val IMAGE_SENT = 3
    private val IMAGE_RECEIVED = 4

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1){
//            Inflate received message
            val view = LayoutInflater.from(context).inflate(R.layout.received_message_layout, parent, false)
            return ReceivedViewHolder(view)
        }
        else if (viewType == 2){
//            Inflate sent message -
            val view = LayoutInflater.from(context).inflate(R.layout.sent_message_layout, parent, false)
            return SentViewHolder(view)
        }
        else if (viewType == 3){
            val view = LayoutInflater.from(context).inflate(R.layout.sent_image_layout, parent, false)
            return SentImageViewHolder(view)
        }
        else{
            val view = LayoutInflater.from(context).inflate(R.layout.received_image_layout, parent, false)
            return ReceivedImageViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messagesList[position]
        if (holder.javaClass == SentViewHolder::class.java){
            val viewHolder = holder as SentViewHolder
                holder.sentMessage.text = currentMessage.message
                holder.sentMessage.visibility = View.VISIBLE
        }
        else if (holder.javaClass == ReceivedViewHolder::class.java){
            val viewHolder = holder as ReceivedViewHolder
            holder.receivedMessage.text = currentMessage.message
                holder.receivedMessage.text = currentMessage.message
                holder.receivedMessage.visibility = View.VISIBLE
        }

        else if (holder.javaClass == SentImageViewHolder::class.java){
            val viewHolder = holder as SentImageViewHolder
//            holder.sentImage.setImageURI(Uri.parse(currentMessage.message?.substring(Constants.IMAGE.length)))
            Glide.with(context)
                .load(Uri.parse(currentMessage.message?.substring(Constants.IMAGE.length)))
                .listener(object : RequestListener<Drawable>{
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.i("Loading Failed ->", e.toString())
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        Log.i("Loading success", " -> Image loaded")
                        return false
                    }

                })
                .into(holder.sentImage)
        }
        else{
            val viewHolder = holder as ReceivedImageViewHolder
            holder.receivedImage.setImageURI(Uri.parse(currentMessage.message?.substring(Constants.IMAGE.length)))
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messagesList[position]

        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId) && currentMessage.message?.startsWith(Constants.IMAGE) == true){
            return IMAGE_SENT
        }
        else if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
            return ITEM_SENT
        }
        else if (currentMessage.message?.startsWith(Constants.IMAGE) == true){
            return IMAGE_RECEIVED
        }
        else{
            return ITEM_RECEIVE
        }
    }

    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val sentMessage: TextView = itemView.findViewById(R.id.tv_sent_message)
    }

    class ReceivedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val receivedMessage: TextView = itemView.findViewById(R.id.tv_received_message)
    }

    class SentImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val sentImage: ImageView = itemView.findViewById(R.id.iv_sent_image)
    }

    class ReceivedImageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val receivedImage: ImageView = itemView.findViewById(R.id.iv_received_image)
    }
}