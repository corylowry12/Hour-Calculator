package com.cory.hourcalculator.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkManager
import com.cory.hourcalculator.R
import com.cory.hourcalculator.activities.MainActivity
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService: FirebaseMessagingService() {

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d("Deleted", "Deleted Message")
    }

    override fun onMessageReceived(p0: RemoteMessage) {
        super.onMessageReceived(p0)
        Log.d("Message", "From: ${p0.from}")

        if (p0.data.isNotEmpty()) {
            Log.d("Message", "Message data payload: ${p0.data}")

            scheduleJob()
        }
            p0.notification?.let {
                Log.d("Message", "Message Notification Body: ${it.body}")
            }
    }

    private fun scheduleJob() {
        val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        WorkManager.getInstance(applicationContext).enqueue(work)
    }

    override fun onNewToken(p0: String) {
        super.onNewToken(p0)
        Log.d("Token", "Refreshed token: $p0")

        sendRegistrationToServer(p0)
    }

    private fun sendRegistrationToServer(p0: String?) {
        Log.d("token", "sendRegistrationTokenToServer($p0)")
    }

   /* fun sendNotification(messageBody: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT)

        val channelId = "12345"
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.hourcalculatorlogo)
            .setContentTitle("Hour Calculator")
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                "Notifications",
                NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())

    }*/
}