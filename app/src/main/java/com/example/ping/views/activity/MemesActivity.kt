package com.example.ping.views.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.network.ApiService
import com.example.ping.network.RandomMemeAPIModel
import com.example.ping.views.adapter.MemesAdapter
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers

class MemesActivity : AppCompatActivity() {
    private val randomMemeApiService = ApiService()
    private lateinit var memesList : ArrayList<String>
    private val compositeDisposable = CompositeDisposable()
    private lateinit var memesRecyclerView: RecyclerView
    private lateinit var memesAdapter: MemesAdapter
    private var flag = false
    private lateinit var progressBar: ProgressBar
    private lateinit var tvLoadingMemes: TextView
    private lateinit var memesSet: HashSet<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memes)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Memes Center"

        progressBar = findViewById(R.id.pb_loading_memes)
        tvLoadingMemes = findViewById(R.id.tv_loading_memes)
        memesList = ArrayList()
        memesSet = HashSet()

        if (flag){
            progressBar.visibility = View.GONE
            tvLoadingMemes.visibility = View.GONE
        }
        getRandomMeme()
    }

    private fun getRandomMeme(){
        for (i in 0..90){
            compositeDisposable.add(
                randomMemeApiService.getRandomMeme()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribeWith(object : DisposableSingleObserver<RandomMemeAPIModel>(){
                        override fun onSuccess(value: RandomMemeAPIModel) {
                            if (i == 89){
                                memesSet.add(value.url)
                                memesRecyclerView = findViewById(R.id.rv_memes)
                                memesList = memesSet.toList() as ArrayList<String>
                                memesAdapter = MemesAdapter(this@MemesActivity, memesList)
                                memesRecyclerView.layoutManager = LinearLayoutManager(this@MemesActivity)
                                memesRecyclerView.adapter = memesAdapter
                                memesRecyclerView.visibility = View.VISIBLE
                                flag = true
                                progressBar.visibility = View.GONE
                                tvLoadingMemes.visibility = View.GONE
                            }
                            else{
                                memesSet.add(value.url)
                            }
                        }

                        override fun onError(e: Throwable) {
                            e.printStackTrace()
                        }
                    })
            )
        }
    }
}