package com.example.ping.views.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.meme_lib.network.Meme
import com.example.meme_lib.util.MemesLib
import com.example.ping.R
import com.example.ping.views.adapter.MemesAdapter
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MemesActivity : AppCompatActivity() {
    private lateinit var rvMemes : RecyclerView
    private lateinit var memesAdapter: MemesAdapter
    private lateinit var dbRef: DatabaseReference
    private val TAG = "MemesActivityTag"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memes)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reddit Meme"
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_black)

//        Initialising views -
        rvMemes = findViewById(R.id.rv_memes)
        val progressBar: ProgressBar = findViewById(R.id.pb_loading_memes)
        val tvLoadingMemes: TextView = findViewById(R.id.tv_loading_memes)



//        Instantiating variables -
        val memesList : ArrayList<Meme> = ArrayList()
        memesAdapter = MemesAdapter(this, this)
        rvMemes.layoutManager = LinearLayoutManager(this)
        dbRef = FirebaseDatabase.getInstance().reference

        val memesLib = MemesLib()
        memesLib.getMultipleMemes(count = 10) { response ->
            if (response != null) {
                val memes = response.memes
                rvMemes.visibility = View.VISIBLE
                memesList.addAll(memes)
                Log.d(TAG, "onCreate: $memes")
                memesAdapter.memes = memes
                rvMemes.adapter = memesAdapter
                progressBar.visibility = View.GONE
                tvLoadingMemes.visibility = View.GONE

            } else {
                val layout = CoordinatorLayout(this@MemesActivity)
                Snackbar.make(layout, "Error Fetching Memes!\nCheck Your Internet Connection!", Snackbar.LENGTH_SHORT).show()
                progressBar.visibility = View.GONE
                tvLoadingMemes.visibility = View.GONE
            }
        }


//        Fetching Memes -
//        lifecycleScope.launch {
//            progressBar.isVisible = true
//            tvLoadingMemes.isVisible = true
//            val response = try {
//                ApiService.api.getMemes()
//            }
//            catch (e: IOException){
//                Log.e("MainActivity", "IOException, You might not have internet connection!")
//                Toast.makeText(this@MemesActivity, "You Might Not Have Internet Connection!", Toast.LENGTH_SHORT).show()
//                progressBar.isVisible = false
//                return@launch
//            }
//
//            if (response.isSuccessful && response.body() != null){
//                rvMemes.visibility = View.VISIBLE
//                memesList.add(response.body()!!.url)
//                memesAdapter.memes = memesList
//                rvMemes.adapter = memesAdapter
//            }
//            else{
//                Toast.makeText(this@MemesActivity, "Unable To Fetch Meme", Toast.LENGTH_SHORT).show()
//            }
//
//            progressBar.isVisible = false
//            tvLoadingMemes.isVisible = false
//        }
    }

    fun saveMemeToDatabase(memeUrl: String){
        addUrlToDatabase(memeUrl)
    }

//    Come up with a unique childName for every memeUrl so that I can add multiple memes to database

    private fun addUrlToDatabase(memeUrl: String) = CoroutineScope(Dispatchers.IO).launch{
        try {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            val child = encryptUrl(memeUrl)

            dbRef.child("savedMemes").child(uid!!).child("memes").child(child).setValue(memeUrl)
            withContext(Dispatchers.Main){
                Toast.makeText(this@MemesActivity, "Meme Saved", Toast.LENGTH_SHORT).show()
            }

        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@MemesActivity, "Unable To Save Meme, ${e.message.toString()}", Toast.LENGTH_SHORT).show()
                Log.i("Unable to add meme", e.message.toString())
            }
        }
    }

    private fun encryptUrl(url: String): String{
        var string = ""

        for (i in url.indices){
            if (url[i] == '/'){
                string += '!'
            }
            else if (url[i] == '.'){
                string += '+'
            }
            else{
                string += url[i]
            }
        }
        return string
    }
}