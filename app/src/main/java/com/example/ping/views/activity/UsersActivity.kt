package com.example.ping.views.activity

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.model.User
import com.example.ping.services.MyFirebaseMessagingService
import com.example.ping.views.adapter.UsersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.RemoteMessage
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
    private lateinit var btnOpenMenu: ImageView
    private val TAG = "UsersActivity"
    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)
        supportActionBar?.hide()
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_black)


//        Initialising fields -
        btnOpenMenu = findViewById(R.id.open_menu)
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

        btnOpenMenu.setOnClickListener{
            showPopup(btnOpenMenu)
        }


//        Loading Users from database -
        loadUsers()
        changeActiveStatus(true)

//        Enabling sharing capability -
        onSharedIntent()


//        Notifications -
        askNotificationPermission()
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        else{
            val fcm = FirebaseMessaging.getInstance()

            // Build the message to be sent
            val message = RemoteMessage.Builder("your_sender_id")
                .setMessageId("unique_message_id")
                .addData("title", "Hello World!")
                .addData("body", "This is a test notification.")
                .build()

            // Send the message
            fcm.send(message)
        }
    }

    private fun showPopup(view: View) {
        val popup = PopupMenu(this, view)
        popup.inflate(R.menu.main_menu)

        popup.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.logout -> {
                    auth = FirebaseAuth.getInstance()
                    auth.signOut()
                    Toast.makeText(applicationContext, "Logged Out", Toast.LENGTH_SHORT).show()
                    finish()
                    startActivity(Intent(this@UsersActivity, MainActivity::class.java))

                }
                R.id.show_saved_memes -> {
                    startActivity(Intent(this, SavedMemesActivity::class.java))
                }
            }

            true
        }

        popup.show()
    }

    private fun changeActiveStatus(status: Boolean) {
        val firebaseAuth = FirebaseAuth.getInstance()
        firebaseAuth.currentUser?.uid?.let { uid ->
            dbRef.child("activeStatus").child(uid).setValue(status)
        }
    }

    override fun onStop() {
        super.onStop()
        val isMyAppRunning = isAppRunning(this, "com.example.ping")
        if (!isMyAppRunning) {
            // App is still running
            changeActiveStatus(false)
        }
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

    private fun onSharedIntent() {
        val receivedIntent = intent
        val receivedAction = receivedIntent.action
        val receivedType = receivedIntent.type
        if (receivedAction == Intent.ACTION_SEND) {

            // check mime type
            if (receivedType!!.startsWith("text/")) {
                val receivedText = receivedIntent
                    .getStringExtra(Intent.EXTRA_TEXT)
                if (receivedText != null) {
                    //do your stuff
                    Toast.makeText(applicationContext, receivedText, Toast.LENGTH_SHORT).show()
                }
            } else if (receivedType.startsWith("image/")) {
                val receiveUri = receivedIntent
                    .getParcelableExtra<Parcelable>(Intent.EXTRA_STREAM) as Uri?
                if (receiveUri != null) {
                    //do your stuff
//                    fileUri = receiveUri // save to your own Uri object
                    Log.e(TAG, receiveUri.toString())
                }
            }
        } else if (receivedAction == Intent.ACTION_MAIN) {
            Log.e(TAG, "onSharedIntent: nothing shared")
        }
    }
}