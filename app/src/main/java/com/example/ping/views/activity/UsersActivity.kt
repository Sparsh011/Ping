package com.example.ping.views.activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.model.User
import com.example.ping.views.adapter.UsersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class UsersActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersList: ArrayList<User>
    private lateinit var adapter: UsersAdapter
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

//        Initialising fields -
        auth = FirebaseAuth.getInstance()
        usersList = ArrayList()
        adapter = UsersAdapter(this, usersList)
        usersRecyclerView = findViewById(R.id.rv_users)
        dbRef = FirebaseDatabase.getInstance().reference

        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = adapter

        dbRef.child("user").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (item in snapshot.children){
                    val currentUser = item.getValue(User::class.java)
                    if (auth.currentUser?.uid != currentUser?.uid){
                        usersList.add(currentUser!!)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout -> {
                auth = FirebaseAuth.getInstance()
                auth.signOut()
                Toast.makeText(applicationContext, "Logged Out", Toast.LENGTH_SHORT).show()
                finish()
                startActivity(Intent(this@UsersActivity, MainActivity::class.java))
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }
}