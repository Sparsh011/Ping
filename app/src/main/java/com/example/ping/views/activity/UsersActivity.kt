package com.example.ping.views.activity

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.model.User
import com.example.ping.views.adapter.UsersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private lateinit var usersRecyclerView: RecyclerView
    private lateinit var usersList: ArrayList<User>
    private lateinit var adapter: UsersAdapter
    private lateinit var dbRef: DatabaseReference
    private lateinit var progressBar: ProgressBar
    private lateinit var etSearchContact: EditText
    private lateinit var showMemes: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        supportActionBar?.hide()

//        Initialising fields -
        auth = FirebaseAuth.getInstance()
        usersList = ArrayList()
        adapter = UsersAdapter(this, usersList)
        usersRecyclerView = findViewById(R.id.rv_users)
        dbRef = FirebaseDatabase.getInstance().reference
        progressBar = findViewById(R.id.pb_loading_users)
        etSearchContact = findViewById(R.id.et_search_contact)
        showMemes = findViewById(R.id.btn_show_memes)
        showMemes.setOnClickListener{
            startActivity(Intent(this, MemesActivity::class.java))
        }

        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = adapter

        etSearchContact.addTextChangedListener {
            showFilteredList(it.toString())
        }


        Log.d("UserAct me", "onCreate: Here")
//        Loading Users from database -
        loadUsers()
        changeActiveStatus(true)
    }

    private fun changeActiveStatus(status: Boolean) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.currentUser?.uid?.let { uid ->
            dbRef.child("activeStatus").child(uid).setValue(status)
        }
    }

    override fun onStop() {
        super.onStop()
//        val isMyAppRunning = isAppRunning(this, "com.example.ping")
//        if (!isMyAppRunning) {
//            // App is still running
//            changeActiveStatus(false)
//        }
    }

    private fun isAppRunning(context: Context, packageName: String): Boolean {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager

        // Get the list of running processes
        val runningProcesses = activityManager.runningAppProcesses

        // Iterate through the running processes to check if the app is in the foreground
        if (runningProcesses != null) {
            for (processInfo in runningProcesses) {
                if (processInfo.processName == packageName && processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    return true
                }
            }
        }

        return false
    }

    override fun onResume() {
        super.onResume()
        changeActiveStatus(true)
    }

    private fun loadUsers() {
        lifecycleScope.launch(Dispatchers.IO){
            try {
                dbRef.child("user").addValueEventListener(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        usersList.clear()
                        for (item in snapshot.children){
                            val currentUser = item.getValue(User::class.java)
                            if (auth.currentUser?.uid != currentUser?.uid){
                                usersList.add(currentUser!!)
                            }
                        }

                        progressBar.visibility = View.GONE
                        usersRecyclerView.visibility = View.VISIBLE
                        adapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            } catch (e: Exception){
                withContext(Dispatchers.Main){
                    Toast.makeText(this@UsersActivity, "Error loading chats -> ${e.message.toString()}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

//    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        val inflater: MenuInflater = menuInflater
//        inflater.inflate(R.menu.main_menu, menu)
//
//        val menuItem = menu?.findItem(R.id.search)
//        val searchView: SearchView = menuItem?.actionView as SearchView
//        searchView.queryHint = "Search Here..."
//
//        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
//            override fun onQueryTextSubmit(p0: String?): Boolean {
//                // Called when the user presses enter
//                return true
//            }
//
//            override fun onQueryTextChange(text: String?): Boolean {
//                showFilteredList(text)
//                return true
//            }
//        })
//
//        return true
//    }

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
            R.id.show_memes -> {
                startActivity(Intent(this@UsersActivity, MemesActivity::class.java))
            }

            R.id.show_saved_memes ->{
                startActivity(Intent(this@UsersActivity, SavedMemesActivity::class.java))
            }
            else -> super.onOptionsItemSelected(item)
        }
        return true
    }

    private fun showFilteredList(initials: String?){
        dbRef.child("user").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                usersList.clear()
                for (item in snapshot.children){
                    val currentUser = item.getValue(User::class.java)
                    if (auth.currentUser?.uid != currentUser?.uid && currentUser?.name?.lowercase()!!.contains(initials!!.lowercase())){
                        usersList.add(currentUser)
                    }
                }

                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}