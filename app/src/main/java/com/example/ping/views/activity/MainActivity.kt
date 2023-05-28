package com.example.ping.views.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.example.ping.R
import com.example.ping.services.OnClearFromRecentService
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.hbb20.CountryCodePicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    private val TAG = "MainActivity"
    private lateinit var codeSent: String
    private lateinit var mCallback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mProgressBar: ProgressBar
    private lateinit var mUsername: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        Initialising views -
        val btnSendOtp = findViewById<TextView>(R.id.btn_send_otp)
        val etPhoneNumber = findViewById<EditText>(R.id.et_phone_number)
        val ccp = findViewById<CountryCodePicker>(R.id.btn_ccp)
        mProgressBar = findViewById(R.id.progressBar)
        val etUsername = findViewById<EditText>(R.id.et_name)
        var countryCode = ccp.selectedCountryCodeWithPlus

        startService(Intent(baseContext, OnClearFromRecentService::class.java))


//        if country is changed -
        ccp.setOnCountryChangeListener {
            countryCode = ccp.selectedCountryCodeWithPlus
        }

        btnSendOtp.setOnClickListener{
            val number = etPhoneNumber.text.toString()
            mUsername = etUsername.text.toString()
            if (number.length != 10){
                Toast.makeText(this, "Enter A Valid Number!", Toast.LENGTH_SHORT).show()
            }
            else if (mUsername.trim().isEmpty()){
                Toast.makeText(this, "Enter Your Name!", Toast.LENGTH_SHORT).show()
            }
            else{
                val phoneNumber = countryCode + number
                mProgressBar.visibility = View.VISIBLE
                sendOtp(phoneNumber, mUsername)
            }
        }
    }

    private fun sendOtp(phoneNumber: String, username: String) = CoroutineScope(Dispatchers.IO).launch{
        auth = FirebaseAuth.getInstance()

        mCallback = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                Log.d(TAG, "onVerificationCompleted:$credential")
            }

            override fun onVerificationFailed(e: FirebaseException) {
                e.printStackTrace()
            }

            override fun onCodeSent(code: String, p1: PhoneAuthProvider.ForceResendingToken) {
                Toast.makeText(this@MainActivity, "OTP Sent", Toast.LENGTH_SHORT).show()
                codeSent = code
                val intent = Intent(this@MainActivity, VerifyOTP::class.java)
                intent.putExtra("otp", codeSent)
                intent.putExtra("name", username)
                intent.putExtra("number", phoneNumber)
                startActivity(intent)
            }
        }

        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS) // Won't send another OTP for 60 seconds
            .setActivity(this@MainActivity)
            .setCallbacks(mCallback)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    override fun onStart() {
        super.onStart()
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser != null){
            // means user is already verified
            val intent = Intent(this@MainActivity, UsersActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        mProgressBar.isVisible = false
    }
}