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
        supportActionBar?.title = "Memes Center"

//        Initialising views -
        rvMemes = findViewById(R.id.rv_memes)
        val progressBar: ProgressBar = findViewById(R.id.pb_loading_memes)
        val tvLoadingMemes: TextView = findViewById(R.id.tv_loading_memes)

//        Instantiating variables -
        var memesList : ArrayList<String>
        val memesSet: HashSet<String> = HashSet()
        memesAdapter = MemesAdapter(this)
        rvMemes.layoutManager = LinearLayoutManager(this)

//        Fetching memes -
        for (i in 0 .. 35){
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
                    memesSet.add(response.body()!!.url)
                }
                else Toast.makeText(this@MemesActivity, "Unable to fetch", Toast.LENGTH_SHORT).show()

                if (i == 34){
                    progressBar.isVisible = false
                    tvLoadingMemes.isVisible = false

//                    Populating data to recyclerView -
                    rvMemes.visibility = View.VISIBLE
                    memesList = memesSet.toList() as ArrayList<String>
                    memesAdapter.memes = memesList
                    rvMemes.adapter = memesAdapter
                }
            }
        }
//        Fetching multiple but problem is ki jb 2nd time load open kro activity tb ye s 3,4 memes hi load kr rha hai
//        memesSet.clear()
    }
}