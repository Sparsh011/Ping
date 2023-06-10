package com.example.ping.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.example.ping.R
import com.example.ping.views.activity.UsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val channelId = "i.apps.notifications"
    private val description = "Test notification"

    override fun onNewToken(token: String) {
        Log.d("New_Token", token)
        sendToken(token)
    }

    private fun sendToken(token: String) {
        FirebaseDatabase.getInstance().reference.child("Users/${FirebaseAuth.getInstance().currentUser?.uid}").updateChildren(
            mapOf("deviceToken" to token)
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("onMessageReceived", "onMessageReceived: ${remoteMessage.from}")

        // it is a class to notify the user of events that happen.
        // This is how you tell the user that something has happened in the
        // background.
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        // pendingIntent is an intent for future use i.e after
        // the notification is clicked, this intent will come into action
        val intent = Intent(this, UsersActivity::class.java)

        // FLAG_UPDATE_CURRENT specifies that if a previous
        // PendingIntent already exists, then the current one
        // will update it with the latest intent
        // 0 is the request code, using it later with the
        // same method again will get back the same pending
        // intent for future reference
        // intent passed here is to our afterNotification class
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)


        // checking if android version is greater than oreo(API 26) or not
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = NotificationChannel(channelId, description, NotificationManager.IMPORTANCE_HIGH)
            notificationChannel.enableLights(true)
            notificationChannel.lightColor = Color.GREEN
            notificationChannel.enableVibration(false)
            notificationManager.createNotificationChannel(notificationChannel)

             builder = Notification.Builder(this, channelId)
                .setContentTitle(remoteMessage.notification!!.title)
                .setContentText(remoteMessage.notification!!.body)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_background))
                .setContentIntent(pendingIntent)
        } else {

            builder = Notification.Builder(this, description)
                .setContentTitle(remoteMessage.notification!!.title)
                .setContentText(remoteMessage.notification!!.body)
                .setLargeIcon(BitmapFactory.decodeResource(this.resources, R.drawable.ic_launcher_background))
                .setContentIntent(pendingIntent)
        }

        notificationManager.notify(1234, builder.build())
    }
}