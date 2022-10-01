package com.example.ping.views.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.views.activity.ChatActivity

class CustomListItemAdapter(
    private val activity: Activity,
    private val listItems: List<String>,
) :
    RecyclerView.Adapter<CustomListItemAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_custom_list, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = listItems[position]
        holder.tvText.text = item

        holder.tvText.setOnClickListener{
            if (activity is ChatActivity){
                activity.selectImageVia(item)
            }
        }

    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvText : TextView = view.findViewById(R.id.tv_text)
    }
}