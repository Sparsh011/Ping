package com.example.ping.views.activity

import android.Manifest
import android.R.attr.path
import android.app.Activity
import android.app.AlertDialog
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.ping.R
import com.example.ping.model.MessageModel
import com.example.ping.utils.Constants
import com.example.ping.views.adapter.CustomListItemAdapter
import com.example.ping.views.adapter.MessageAdapter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import java.lang.System.out


class ChatActivity : AppCompatActivity() {
    private lateinit var customChatRecyclerView: RecyclerView
    private lateinit var message: EditText
    private lateinit var ivSendMessage: ImageView
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<MessageModel>
    private var receiverRoom: String? = null
    private var senderRoom: String? = null
    private lateinit var dbRef: DatabaseReference
    private lateinit var ivMoreOptions: ImageView
    private var mImagePath: String = ""
    private lateinit var mCustomListDialog: Dialog
    private lateinit var mDialogToSendImage: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val name = intent.getStringExtra("nameOfUser")
        val receiverUid = intent.getStringExtra("uid")
        supportActionBar?.title = name
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val senderUid = FirebaseAuth.getInstance().currentUser?.uid
        senderRoom = receiverUid + senderUid
        receiverRoom = senderUid + receiverUid
        dbRef = FirebaseDatabase.getInstance().reference

//        Initialising views -
        customChatRecyclerView = findViewById(R.id.rv_custom_chat)
        message = findViewById(R.id.et_message)
        ivSendMessage = findViewById(R.id.img_send_message)
        messageList = ArrayList()
        messageAdapter = MessageAdapter(this, messageList)
        customChatRecyclerView.layoutManager = LinearLayoutManager(this)
        customChatRecyclerView.adapter = messageAdapter
        ivMoreOptions = findViewById(R.id.iv_more_options)

//        Choosing image -
        ivMoreOptions.setOnClickListener{
            showPopupForSelectionFromCameraOrStorage("Select Image From", Constants.selectImageFrom())
        }

//      Adding message to database -
        ivSendMessage.setOnClickListener{
            val msg = message.text.toString()
            val messageObject = MessageModel(msg, senderUid)

            if (msg.trim().isNotEmpty()){
                dbRef.child("chats").child(senderRoom!!).child("messages").push() // Generating a new child location
                    .setValue(messageObject).addOnSuccessListener {
                        dbRef.child("chats").child(receiverRoom!!).child("messages").push().setValue(messageObject)
                    }

                message.setText("")
            }
        }

//        Adding data to recyclerView -
        dbRef.child("chats").child(senderRoom!!).child("messages").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                messageList.clear()
                for (item in snapshot.children){
                    val messageItem = item.getValue(MessageModel::class.java)
                    messageList.add(messageItem!!)
                }

                messageAdapter.notifyDataSetChanged()
                customChatRecyclerView.scrollToPosition(messageList.size-1)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(applicationContext, "Unable To Load Messages!", Toast.LENGTH_SHORT).show()
            }

        })
    }

    private fun showPopupForSelectionFromCameraOrStorage(title: String, selectImageFrom: ArrayList<String>) {
        mCustomListDialog = Dialog(this)
        val inflater = layoutInflater
        val dialogLayoutInflater = inflater.inflate(R.layout.dialog_custom_list, null)
        mCustomListDialog.setContentView(dialogLayoutInflater)
        val tvTitle = mCustomListDialog.findViewById<TextView>(R.id.tv_title)
        val rvList = mCustomListDialog.findViewById<RecyclerView>(R.id.rv_list)
        rvList.layoutManager = LinearLayoutManager(this)
        tvTitle.text = title

        val adapter = CustomListItemAdapter(this, selectImageFrom)
        rvList.adapter = adapter
        mCustomListDialog.show()
    }

    fun selectImageVia(selectVia: String){
        if (selectVia == Constants.SELECT_FROM_CAMERA){
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            startForResultToLoadImage.launch(intent)
                        }
                    }
                }
                override fun onPermissionRationaleShouldBeShown(permission: MutableList<PermissionRequest>?, token: PermissionToken?) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()
        }

        else{
            Dexter.withContext(this)
                .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                .withListener(object : PermissionListener {
                    override fun onPermissionGranted(response: PermissionGrantedResponse) {
                        imageChooser()
                    }
                    override fun onPermissionDenied(response: PermissionDeniedResponse) {
                        Toast.makeText(this@ChatActivity, "You have denied the storage permission. Enable it to select image from storage.", Toast.LENGTH_SHORT).show()
                    }
                    override fun onPermissionRationaleShouldBeShown(permission: PermissionRequest, token: PermissionToken) {
                        showRationalDialogForPermissions()
                    }
                }).onSameThread().check()
        }
    }

    private fun imageChooser() {
        val i = Intent()
        i.type = "image/*"
        i.action = Intent.ACTION_GET_CONTENT
        launchSomeActivity.launch(i)
    }

    private var launchSomeActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {

            val data = result.data
            if (data != null && data.data != null) {
                val selectedImageUri = data.data

                if (selectedImageUri != null) {
                    showPopupToSendImageUsingUri(selectedImageUri)
                }
            }
        }
    }

    private val startForResultToLoadImage =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                try {
                    val selectedImage: Uri? = result.data?.data
                    if (selectedImage != null) {
                        Toast.makeText(applicationContext, "Got IMG", Toast.LENGTH_SHORT).show()
                        showPopupToSendImageUsingUri(selectedImage)
                    } else {
                        // From Camera code goes here.
                        // Get the bitmap directly from camera
                        result.data?.extras?.let {

                            val bitmap: Bitmap = result.data?.extras?.get("data") as Bitmap

                            showPopupToSendImageUsingBitmap(bitmap)
                            Log.i("ImagePath", mImagePath)
                        }
                    }
                } catch (error: Exception) {
                    Log.d("log==>>", "Error : ${error.localizedMessage}")
                }
            }
        }

    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("Looks like you have not allowed the required permissions. You have to enable them to use this feature.")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }


    private fun showPopupToSendImageUsingBitmap(bitmap: Bitmap) {
        mDialogToSendImage = Dialog(this)
        val inflater = layoutInflater
        val dialogLayoutInflater = inflater.inflate(R.layout.dialog_send_image, null)
        mDialogToSendImage.setContentView(dialogLayoutInflater)
        val imgToBeSent = mDialogToSendImage.findViewById<ImageView>(R.id.iv_this_img_will_be_sent)

        imgToBeSent.setImageBitmap(bitmap)

        mDialogToSendImage.show()
    }

    private fun showPopupToSendImageUsingUri(selectedImage: Uri) {
        mDialogToSendImage = Dialog(this)
        val inflater = layoutInflater
        val dialogLayoutInflater = inflater.inflate(R.layout.dialog_send_image, null)
        mDialogToSendImage.setContentView(dialogLayoutInflater)
        val imgToBeSent = mDialogToSendImage.findViewById<ImageView>(R.id.iv_this_img_will_be_sent)

        imgToBeSent.setImageURI(selectedImage)

        mDialogToSendImage.show()
    }
}