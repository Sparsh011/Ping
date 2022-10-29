package com.example.ping.views.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.ContactsContract
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.model.ContactModel
import com.example.ping.model.User
import com.example.ping.views.adapter.UsersAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.coroutines.CoroutineScope
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
    private lateinit var contactsList : HashSet<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

//        Initialising fields -
        auth = FirebaseAuth.getInstance()
        usersList = ArrayList()
        adapter = UsersAdapter(this, usersList)
        usersRecyclerView = findViewById(R.id.rv_users)
        dbRef = FirebaseDatabase.getInstance().reference
        progressBar = findViewById(R.id.pb_loading_users)

        usersRecyclerView.layoutManager = LinearLayoutManager(this)
        usersRecyclerView.adapter = adapter


//        Retrieving contacts -
        showContacts()
 //        CONTACT LIST RETRIEVED BUT OPERATIONS HAVE BECOME SLOWER(LIKE CRUISING THROUGH ACTIVITIES

    }

    private fun showContacts() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.READ_CONTACTS), 10)
        } else {
            // If android version < 6, permission to read contacts is not required
            contactsList = getContactsList()
        }
    }

    @SuppressLint("Recycle", "Range")
    private fun getContactsList() : HashSet<String>{
        contactsList = hashSetOf()
        val uri = ContactsContract.Contacts.CONTENT_URI
        val sort = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"

        val cursor = contentResolver.query(uri, null, null, null, sort)

        if (cursor!!.count > 0){
            while (cursor.moveToNext()){
                val id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                val uriPhone = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                val selection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=?"
                val phoneCursor = contentResolver.query(uriPhone, null, selection, arrayOf(id), null)

                if (phoneCursor!!.moveToNext()){
                    val number = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
//                    val contact = ContactModel(name, number)
                    contactsList.add(removeSpaces(number))
                    phoneCursor.close()
                }
            }
        }

 //        Loading Users from database -
        loadUsers(contactsList)
        cursor.close()
        return contactsList
    }

    private fun removeSpaces(number: String) : String{
        var numberWithoutSpaces = ""
        for (i in number.indices){
            if(number[i] == ' '){
                continue
            }
            numberWithoutSpaces += number[i]
        }

        return numberWithoutSpaces
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                showContacts()
            } else {
                Toast.makeText(this, "Until you grant the permission, we cannot display users.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    private fun loadUsers(contacts: HashSet<String>) = CoroutineScope(Dispatchers.IO).launch {
        try {
//            contactsList = hashSetOf()
            dbRef.child("user").addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    usersList.clear()
                    for (item in snapshot.children){
                        val currentUser = item.getValue(User::class.java)
                        if (auth.currentUser?.uid != currentUser?.uid && contacts.contains(currentUser!!.number)){
                            usersList.add(currentUser)
                        }
                    }
                    Log.i("Contacts List ->", contacts.toString())
                    progressBar.visibility = View.GONE
                    usersRecyclerView.visibility = View.VISIBLE
                    adapter.notifyDataSetChanged()
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(this@UsersActivity, "Error loading chats -> ${e.message.toString()}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.main_menu, menu)

        val menuItem = menu?.findItem(R.id.search)
        val searchView: SearchView = menuItem?.actionView as SearchView
        searchView.queryHint = "Search Here..."

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(p0: String?): Boolean {
                // Called when the user presses enter
                return true
            }

            override fun onQueryTextChange(text: String?): Boolean {
                showFilteredList(text)
                return true
            }
        })

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
                    if (auth.currentUser?.uid != currentUser?.uid && currentUser?.name?.lowercase()!!.contains(initials!!.lowercase()) && contactsList.contains(currentUser.number)){
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