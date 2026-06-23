package com.tcssol.expensetracker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import java.util.Currency
import java.util.Locale

class AlarmReceiver : BroadcastReceiver() {

    companion object {
        private const val CHANNEL_ID = "TCS_Manages_Expenses_Channel"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getStringExtra("Type") ?: "Money Reminder"
        val name = intent.getStringExtra("Person_Name") ?: "Friend"
        val amount = intent.getStringExtra("Amount") ?: "0"
        val sendSms = intent.getBooleanExtra("SendMsg", false)
        val msg = intent.getStringExtra("TextToSent") ?: ""
        val number = intent.getStringExtra("Number") ?: ""

        showNotification(context, name, type, amount, sendSms, msg, number)
    }

    private fun showNotification(
        context: Context,
        name: String,
        type: String,
        amount: String,
        sendSms: Boolean,
        message: String,
        phoneNumber: String
    ) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Payment Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val pattern = longArrayOf(0, 300, 200, 500)
        val symbol = Currency.getInstance(Locale.getDefault()).symbol

        if (!sendSms) {
            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_money_24)
                .setContentTitle(type)
                .setContentText("$name: $symbol$amount")
                .setVibrate(pattern)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)

            notificationManager.notify(getUniqueNotificationId(), builder.build())
        } else {
            // Intent to open SMS sharing dialog
            val smsUri = Uri.parse("smsto:$phoneNumber")
            val smsIntent = Intent(Intent.ACTION_SENDTO, smsUri).apply {
                putExtra("sms_body", message)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            
            val pendingIntent = PendingIntent.getActivity(
                context,
                getUniqueNotificationId(),
                smsIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.baseline_money_24)
                .setContentTitle("Send reminder to $name ($phoneNumber)")
                .setContentText("Tap to send: $message")
                .setVibrate(pattern)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            notificationManager.notify(getUniqueNotificationId(), builder.build())
        }
    }

    private fun getUniqueNotificationId(): Int {
        return System.currentTimeMillis().toInt()
    }
}
