package com.tcssol.expensetracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import java.util.Currency;
import java.util.Locale;

/***
 * This is the broadcast receiver which displayed the message and sends notification for pending payments
 */

public class AlarmReceiver extends BroadcastReceiver {
    private static final String CHANNEL_ID = "TCS_Manages_Expenses_Channel";
    @Override
    public void onReceive(Context context, Intent intent) {
        String type=intent.getStringExtra("Type");
        String name=intent.getStringExtra("Person_Name");
        String amount=intent.getStringExtra("Amount");
        boolean sendSms=intent.getBooleanExtra("SendMsg",false);
        String msg=intent.getStringExtra("TextToSent");
        String number=intent.getStringExtra("Number");

        showNotification(context,name,type,amount,sendSms,msg,number);
    }
    private void showNotification(Context context, String name,String type,String amount,boolean sendSms,String message,String phoneNumber) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Create a notification channel (required for Android Oreo and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Get_That_Money",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);

        }
        long[] pattern={0, 300, 200, 500};
        if(sendSms==false) {
            // Build the notification
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_money_24)
                    .setContentTitle(type)
                    .setContentText(name + ": " + Currency.getInstance(Locale.getDefault()).getSymbol() + amount)
                    .setVibrate(pattern)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);

            // Display the notification
            notificationManager.notify(getUniqueNotificationId(), builder.build());
        }else{
            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_money_24)
                    .setContentTitle("Reminder sent to "+phoneNumber+" for Money")
                    .setContentText(name + ": " + Currency.getInstance(Locale.getDefault()).getSymbol() + amount)
                    .setVibrate(pattern)
                    .setPriority(NotificationCompat.PRIORITY_HIGH);
            notificationManager.notify(getUniqueNotificationId(), builder.build());
            /*
            Sending SMS using sms Manager
             */
            SmsManager smsManager = SmsManager.getDefault();
            /*
            TODO get default country codes or add a feature for users to select the country
            TODO Use contact database to directly save contact
             */
            smsManager.sendTextMessage("+91"+phoneNumber, null, message, null, null);

            Log.d("SmG6996","Message Sent");

        }
    }


    // Method to generate a unique notification ID
    private int getUniqueNotificationId() {
        return (int) System.currentTimeMillis();
    }
}

