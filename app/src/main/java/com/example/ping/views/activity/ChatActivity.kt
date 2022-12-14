package com.example.ping.views.activity

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.model.MessageModel
import com.example.ping.views.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {
    private lateinit var customChatRecyclerView: RecyclerView
    private lateinit var message: EditText
    private lateinit var ivSendMessage: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<MessageModel>
    private var receiverRoom: String? = null
    private var senderRoom: String? = null
    private lateinit var dbRef: DatabaseReference
//    private lateinit var ivMoreOptions: ImageView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        val name = intent.getStringExtra("nameOfUser")
        val receiverUid = intent.getStringExtra("uid")
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid
        dbRef = FirebaseDatabase.getInstance().reference

//        Initialising views -
        customChatRecyclerView = findViewById(R.id.rv_custom_chat)
        message = findViewById(R.id.et_message)
        ivSendMessage = findViewById(R.id.img_send_message)


//        Initialising fields -
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        val messagesLayoutManager = LinearLayoutManager(this)
        customChatRecyclerView.layoutManager = messagesLayoutManager
        customChatRecyclerView.adapter = messageAdapter
//        ivMoreOptions = findViewById(R.id.iv_more_options)


//      Adding message to database -
        ivSendMessage.setOnClickListener{
            val msg = message.text.toString()
            val messageObject = MessageModel(msg, senderUid)

            if (msg.trim().isNotEmpty()){
                uploadChatsToDatabase(senderRoom!!, receiverRoom!!, messageObject)
                message.setText("")
            }
        }


//        Adding data to recyclerView -
        populateChatsToRecyclerView(senderRoom!!)

    }


    private fun populateChatsToRecyclerView(senderRoom: String) = CoroutineScope(Dispatchers.IO).launch {

        try {
            dbRef.child("chats").child(senderRoom).child("messages").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messageList.clear()
                    for (item in snapshot.children){
                        val messageItem = item.getValue(MessageModel::class.java)
                        messageList.add(messageItem!!)
                    }

                    messageAdapter.notifyDataSetChanged()
                    customChatRecyclerView.scrollToPosition(messageList.size-1)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Unable To Load Messages!", Toast.LENGTH_SHORT).show()
                }

            })

        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@ChatActivity, "Error -> ${e.message.toString()}", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun uploadChatsToDatabase(senderRoom: String, receiverRoom: String, messageObject: MessageModel) = CoroutineScope(Dispatchers.IO).launch{
        try {
            dbRef.child("chats").child(senderRoom).child("messages").push() // Generating a new child location
                .setValue(messageObject).addOnSuccessListener {
                    dbRef.child("chats").child(receiverRoom).child("messages").push().setValue(messageObject)
                }

        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@ChatActivity, "Error -> ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}