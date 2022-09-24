package com.example.ping.views.activity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.ping.R
import com.google.firebase.auth.*
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
            }
            else {
                mProgressBarOfOTPAuth.visibility = View.VISIBLE
                val codeReceived = intent.getStringExtra("otp")
                val credential = PhoneAuthProvider.getCredential(codeReceived!!, enteredOTP)
                signInWithPhoneAuthCredential(credential)
            }
        }
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) = CoroutineScope(Dispatchers.IO).launch {
        try {
            firebaseAuth.signInWithCredential(credential).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    mProgressBarOfOTPAuth.visibility = View.INVISIBLE
                    Toast.makeText(applicationContext, "Login Successful", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@VerifyOTP, ChatActivity::class.java)
                startActivity(intent)
                    finish()
                } else {
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        mProgressBarOfOTPAuth.visibility = View.INVISIBLE
                        Toast.makeText(applicationContext, "Login Failed!", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        catch (e: Exception){
            withContext(Dispatchers.Main){
                Toast.makeText(applicationContext, "Login Failed!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}