package com.example.ping.views.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.bumptech.glide.Glide
import com.example.ping.R
import com.example.ping.network.ApiService
import com.example.ping.network.RandomMemeAPIModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.observers.DisposableSingleObserver
import io.reactivex.rxjava3.schedulers.Schedulers

class MemesActivity : AppCompatActivity() {

    private val randomRecipeApiService = ApiService()

    private val compositeDisposable = CompositeDisposable()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memes)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Dang Memes"

//        findViewById<Button>(R.id.btnadfa).setOnClickListener{
            getRandomMeme()
//        }

    }

    private fun getRandomMeme(){
        compositeDisposable.add(
            randomRecipeApiService.getRandomMeme()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<RandomMemeAPIModel>(){
                    override fun onSuccess(value: RandomMemeAPIModel) {
//                        Toast.makeText(applicationContext, value.author, Toast.LENGTH_SHORT).show()
                        Glide.with(this@MemesActivity)
                            .load(value.url)
                            .into(findViewById(R.id.imgmemeloaded))
                    }

                    override fun onError(e: Throwable) {

                        e.printStackTrace()
                    }
                })
        )

//        val memes = randomRecipeApiService.getRandomMeme()

    }
}