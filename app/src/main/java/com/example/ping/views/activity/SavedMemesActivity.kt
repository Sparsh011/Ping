package com.example.ping.views.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.views.adapter.MemesAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SavedMemesActivity : AppCompatActivity() {
    private lateinit var rvSavedMemes : RecyclerView
    private lateinit var memesAdapter: MemesAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var memesList: ArrayList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_saved_memes)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Saved Memes"

        rvSavedMemes = findViewById(R.id.rv_saved_memes)
//        val progressBar: ProgressBar = findViewById(R.id.pb_loading_saved_memes)
//        val tvLoadingMemes: TextView = findViewById(R.id.tv_loading_saved_memes)


        memesList = ArrayList()
        memesAdapter = MemesAdapter(this, this)
        rvSavedMemes.layoutManager = LinearLayoutManager(this)
        rvSavedMemes.adapter = memesAdapter
        dbRef = FirebaseDatabase.getInstance().reference
        showSavedMemes()

    }

//    Memes not loading

    private fun showSavedMemes(){
        loadMemesFromDatabase()
    }

    private fun loadMemesFromDatabase() = CoroutineScope(Dispatchers.IO).launch{
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid

            dbRef.child("savedMemes").child(uid!!).child("memes").addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    memesList.clear()
                    for (item in snapshot.children){
                        val memeUrl = item.value.toString()
                        memesList.add(memeUrl)
                    }

                    Log.i("MemesList ->", memesList.toString())
                    memesAdapter.memes = memesList
                    memesAdapter.notifyDataSetChanged()

                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(applicationContext, "Unable To Load Saved Memes!", Toast.LENGTH_SHORT).show()
                }

            })
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@SavedMemesActivity, "Unable To Load Memes, ${e.message.toString()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun deleteMeme(memeUrl: String){
        deleteMemeFromDatabase(memeUrl)
    }

    private fun deleteMemeFromDatabase(meme: String) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            dbRef.child("savedMemes").child(uid!!).child("memes").removeValue().addOnSuccessListener {
                Toast.makeText(this@SavedMemesActivity, "Removed From Saved!", Toast.LENGTH_SHORT).show()
            }.addOnFailureListener{
                Toast.makeText(this@SavedMemesActivity, "Unable To Remove!", Toast.LENGTH_SHORT).show()
            }

            showSavedMemes()
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@SavedMemesActivity, "Error Removing Meme, ${e.message.toString()}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}