package com.example.ping.views.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.model.MessageModel
import com.google.firebase.auth.FirebaseAuth

class MessageAdapter(
    private val context: Context,
    private val messagesList: ArrayList<MessageModel>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val ITEM_RECEIVE = 1
    private val ITEM_SENT = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == 1){
//            Inflate received message
            val view = LayoutInflater.from(context).inflate(R.layout.received_message_layout, parent, false)
            return ReceivedViewHolder(view)
        }
        else{
//            Inflate sent message -
            val view = LayoutInflater.from(context).inflate(R.layout.sent_message_layout, parent, false)
            return SentViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = messagesList[position]
        if (holder.javaClass == SentViewHolder::class.java){
            val viewHolder = holder as SentViewHolder
            holder.sentMessage.text = currentMessage.message
        }
        else{
            val viewHolder = holder as ReceivedViewHolder
            holder.receivedMessage.text = currentMessage.message
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    override fun getItemViewType(position: Int): Int {
        val currentMessage = messagesList[position]

        if (FirebaseAuth.getInstance().currentUser?.uid.equals(currentMessage.senderId)){
            return ITEM_SENT
        }
        else{
            return ITEM_RECEIVE
        }
    }

    class SentViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val sentMessage: TextView = itemView.findViewById<TextView>(R.id.tv_sent_message)
    }

    class ReceivedViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val receivedMessage: TextView = itemView.findViewById<TextView>(R.id.tv_received_message)
    }
}