package com.example.ping.views.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.network.ApiService
import com.example.ping.views.adapter.MemesAdapter
import java.io.IOException

class MemesActivity : AppCompatActivity() {
    private lateinit var rvMemes : RecyclerView
    private lateinit var memesAdapter: MemesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memes)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Reddit Meme"

//        Initialising views -
        rvMemes = findViewById(R.id.rv_memes)
        val progressBar: ProgressBar = findViewById(R.id.pb_loading_memes)
        val tvLoadingMemes: TextView = findViewById(R.id.tv_loading_memes)



//        Instantiating variables -
        val memesList : ArrayList<String> = ArrayList()
        memesAdapter = MemesAdapter(this)
        rvMemes.layoutManager = LinearLayoutManager(this)



//        Fetching Memes -
        lifecycleScope.launchWhenCreated {
            progressBar.isVisible = true
            tvLoadingMemes.isVisible = true
            val response = try {
                ApiService.api.getMemes()
            }
            catch (e: IOException){
                Log.e("MainActivity", "IOException, You might not have internet connection!")
                Toast.makeText(this@MemesActivity, "You Might Not Have Internet Connection!", Toast.LENGTH_SHORT).show()
                progressBar.isVisible = false
                return@launchWhenCreated
            }

            if (response.isSuccessful && response.body() != null){
                rvMemes.visibility = View.VISIBLE
                memesList.add(response.body()!!.url)
                memesAdapter.memes = memesList
                rvMemes.adapter = memesAdapter
            }
            else{
                Toast.makeText(this@MemesActivity, "Unable To Fetch Meme", Toast.LENGTH_SHORT).show()
            }

            progressBar.isVisible = false
            tvLoadingMemes.isVisible = false
        }
    }
}