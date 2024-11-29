package com.example.cheesemiz
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

 class FirebaseMessagingService : FirebaseMessagingService() {
     override fun onMessageReceived(message: RemoteMessage) {
         super.onMessageReceived(message)
         Log.d ("FirebaseOMR", "message ${message.data.getValue("notification-type")}")
     }

}