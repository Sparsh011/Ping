package com.example.ping.views.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ping.R
import com.example.ping.model.User
import com.google.firebase.auth.*
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class VerifyOTP : AppCompatActivity() {
    private lateinit var mChangeNumber: TextView
    private lateinit var mGetOtp: EditText
    private lateinit var mVerifyOTP: Button
    private lateinit var enteredOTP: String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mProgressBarOfOTPAuth: ProgressBar
    private lateinit var dbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_otp)

//        Initialising views -
        mChangeNumber = findViewById(R.id.changeNumber)
        mVerifyOTP = findViewById(R.id.verifyOTP)
        mGetOtp = findViewById(R.id.getOTP)
        mProgressBarOfOTPAuth = findViewById(R.id.progressBarOfOTPAuthenticationActivity)
        firebaseAuth = FirebaseAuth.getInstance()

        mChangeNumber.setOnClickListener {
            val intent = Intent(this@VerifyOTP, MainActivity::class.java)
            startActivity(intent)
        }

        mVerifyOTP.setOnClickListener {
            enteredOTP = mGetOtp.text.toString()
            if (enteredOTP.trim { it <= ' ' }.isEmpty()) {
                Toast.makeText(this, "Enter OTP!", Toast.LENGTH_SHORT).show()
            } else {
                mProgressBarOfOTPAuth.visibility = View.VISIBLE
                val codeReceived = intent.getStringExtra("otp")
                val credential = PhoneAuthProvider.getCredential(codeReceived!!, enteredOTP)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }


    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                mProgressBarOfOTPAuth.visibility = View.INVISIBLE
                Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
                val username = intent.getStringExtra("name")
                val phoneNumber = intent.getStringExtra("number")
                addUserToDatabase(username!!, phoneNumber!!, firebaseAuth.currentUser?.uid!!)
                val intent = Intent(this@VerifyOTP, UsersActivity::class.java)
                finish()
                startActivity(intent)
            } else {
                if (task.exception is FirebaseAuthInvalidCredentialsException) {
                    mProgressBarOfOTPAuth.visibility = View.INVISIBLE
                    Toast.makeText(applicationContext, "Login Failed!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun addUserToDatabase(name: String, number: String, uid: String) = CoroutineScope(Dispatchers.IO).launch{
        try {
            dbRef = FirebaseDatabase.getInstance().reference
            dbRef.child("user").child(uid).setValue(User(name, number, uid))
        } catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(applicationContext, "Error -> ${e.message.toString()}", Toast.LENGTH_SHORT).show()
            }
        }

    }
}