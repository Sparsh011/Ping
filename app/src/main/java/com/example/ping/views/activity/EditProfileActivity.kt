package com.example.ping.views.activity

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide
import com.example.ping.R
import com.example.ping.model.entities.User
import com.example.ping.views.fragment.DialogChangeProfilePic
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.io.File

class EditProfileActivity : AppCompatActivity() {
    private lateinit var dbRef: DatabaseReference
    private lateinit var etName: EditText
    private lateinit var ivProfilePic: ImageView
    private lateinit var auth: FirebaseAuth
    private lateinit var btnSaveProfile: TextView
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {

        } else {

        }
        askGalleryPermission()
    }
    val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        // Callback is invoked after the user selects a media item or closes the
        // photo picker.
        if (uri != null) {
            loadImage(uri)
        } else {
//                    Log.d("PhotoPicker", "No media selected")
        }
    }

    private fun loadImage(uri: Uri?) {
        Glide.with(this@EditProfileActivity)
            .load(uri)
            .into(ivProfilePic)
    }

    private lateinit var dialogFragment : DialogFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        val profileToolbar: Toolbar = findViewById(R.id.profile_toolbar)
        profileToolbar.setNavigationIcon(R.drawable.baseline_arrow_back_24)
        setSupportActionBar(profileToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Your Profile"

        dbRef = FirebaseDatabase.getInstance().reference
        window.statusBarColor = ContextCompat.getColor(this, R.color.my_black)
        etName = findViewById(R.id.et_profile_name)
        ivProfilePic = findViewById(R.id.ivProfilePic)
        auth = FirebaseAuth.getInstance()
        btnSaveProfile = findViewById(R.id.btn_save_profile)

        btnSaveProfile.setOnClickListener{

        }


        dbRef.child("user").child(auth.currentUser!!.uid).get().addOnSuccessListener {
            val currentUser = it.getValue(User::class.java)
            etName.setText(currentUser?.name.toString())
        }

        ivProfilePic.setOnClickListener {
            dialogFragment = DialogChangeProfilePic(this@EditProfileActivity)
            dialogFragment.show(supportFragmentManager, "My  Fragment")
        }


    }

    private fun askCameraPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {

        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    private fun askGalleryPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
        {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
        else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    fun openGallery() {
        askGalleryPermission()
        dialogFragment.dismiss()
    }
    fun openCamera() {
        askCameraPermission()
        dialogFragment.dismiss()
    }
}