package com.example.ping.views.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.model.MessageModel
import com.example.ping.views.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {
    private lateinit var customChatRecyclerView: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var ivSendMessage: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messages: ArrayList<MessageModel>
    private var receiverRoom: String? = null
    private var senderRoom: String? = null
    private lateinit var dbRef: DatabaseReference
    private lateinit var receiverUid: String
    private lateinit var chattingWithUserStatus: TextView


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val chattingWith = intent.getStringExtra("nameOfUser")
        receiverUid = intent.getStringExtra("uid").toString()
        val chatToolbar: Toolbar = findViewById(R.id.chat_toolbar)
        chatToolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        setSupportActionBar(chatToolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        val toolbarTv = findViewById<TextView>(R.id.user_name)
        toolbarTv.text = chattingWith
        dbRef = FirebaseDatabase.getInstance().reference

        chattingWithUserStatus = findViewById(R.id.chatting_with_user_status)


        dbRef.child("activeStatus").child(receiverUid).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.getValue<Boolean>() == true) {
                    chattingWithUserStatus.text = "Online"
                } else {
                    chattingWithUserStatus.text = "Offline"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid

//        Initialising views -
        customChatRecyclerView = findViewById(R.id.rv_custom_chat)
        etMessage = findViewById(R.id.et_message)
        ivSendMessage = findViewById(R.id.img_send_message)


//        Initialising fields -
        messages = ArrayList()
        messageAdapter = MessageAdapter(this, messages)
        val messagesLayoutManager = LinearLayoutManager(this)
        messagesLayoutManager.stackFromEnd = true
        customChatRecyclerView.layoutManager = messagesLayoutManager
        customChatRecyclerView.adapter = messageAdapter
//        ivMoreOptions = findViewById(R.id.iv_more_options)


//      Adding message to database -
        ivSendMessage.setOnClickListener {
            val msg = etMessage.text.toString()
            val messageObject = MessageModel(msg, senderUid)

            if (msg.trim().isNotEmpty()) {
                uploadChatsToDatabase(senderRoom!!, receiverRoom!!, messageObject)
                etMessage.setText("")
            }
        }

//        Adding data to recyclerView -
        populateChatsToRecyclerView(senderRoom!!)


//        Handling keyboard's appearance -
        customChatRecyclerView.addOnLayoutChangeListener { v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom ->
            customChatRecyclerView.scrollToPosition(
                messages.size - 1
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (receiverUid != null) {
            dbRef.child("activeStatus").child(receiverUid).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val status = snapshot.getValue<Boolean>()
                    if (status == true) {
                        chattingWithUserStatus.text = "Online"
                    } else {
                        chattingWithUserStatus.text = "Offline"
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }

            })
        }
    }


    private fun populateChatsToRecyclerView(senderRoom: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                dbRef.child("chats").child(senderRoom).child("messages").addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        messages.clear()
                        for (item in snapshot.children) {
                            val messageItem = item.getValue(MessageModel::class.java)
                            messages.add(messageItem!!)
                        }

                        messageAdapter.notifyDataSetChanged()
                        customChatRecyclerView.scrollToPosition(messages.size - 1)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(applicationContext, "Unable To Load Messages!", Toast.LENGTH_SHORT).show()
                    }

                })

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChatActivity, "Error -> ${e.message.toString()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }


    private fun uploadChatsToDatabase(senderRoom: String, receiverRoom: String, messageObject: MessageModel) {
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                dbRef.child("chats").child(senderRoom).child("messages").push() // Generating a new child location
                    .setValue(messageObject).addOnSuccessListener {
                        dbRef.child("chats").child(receiverRoom).child("messages").push().setValue(messageObject)
                    }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChatActivity, "Error -> ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}