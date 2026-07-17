package com.tcssol.expensetracker;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.tcssol.expensetracker.Data.ObservationDatabase;
import com.tcssol.expensetracker.Model.Observation;
import com.tcssol.expensetracker.Utils.BankSmsParser;

import java.util.Currency;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Listens for incoming SMS, extracts bank/UPI transactions and stores them as
 * observations for the user to categorize later.
 */
public class TransactionSmsReceiver extends BroadcastReceiver {
    private static final String TAG = "TransactionSms";
    private static final String CHANNEL_ID = "Transaction_Observations_Channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (!Telephony.Sms.Intents.SMS_RECEIVED_ACTION.equals(intent.getAction())) return;

        SmsMessage[] messages = Telephony.Sms.Intents.getMessagesFromIntent(intent);
        if (messages == null || messages.length == 0) return;

        // Multipart messages arrive as several parts from the same sender — join them
        Map<String, StringBuilder> bodies = new HashMap<>();
        Map<String, Long> times = new HashMap<>();
        for (SmsMessage message : messages) {
            if (message == null) continue;
            String sender = message.getDisplayOriginatingAddress();
            bodies.computeIfAbsent(sender, k -> new StringBuilder())
                    .append(message.getMessageBody());
            times.put(sender, message.getTimestampMillis());
        }

        final PendingResult pendingResult = goAsync();
        ObservationDatabase.databaseWriterExecutor.execute(() -> {
            try {
                for (Map.Entry<String, StringBuilder> entry : bodies.entrySet()) {
                    String sender = entry.getKey();
                    String body = entry.getValue().toString();
                    Long time = times.get(sender);
                    Observation observation = BankSmsParser.parse(sender, body,
                            time == null ? System.currentTimeMillis() : time);
                    if (observation != null) {
                        long id = ObservationDatabase.getDatabase(context)
                                .observationDao().insert(observation);
                        if (id != -1) {
                            Log.d(TAG, "Transaction detected: " + observation.getAmount());
                            showNotification(context, observation);
                        }
                    }
                }
            } finally {
                pendingResult.finish();
            }
        });
    }

    private void showNotification(Context context, Observation observation) {
        try {
            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager == null) return;

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID, "Detected transactions",
                        NotificationManager.IMPORTANCE_DEFAULT);
                manager.createNotificationChannel(channel);
            }

            Intent open = new Intent(context, ObservationsActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(context, 0, open,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            String symbol = Currency.getInstance(Locale.getDefault()).getSymbol();
            String amount = String.format(Locale.getDefault(), "%s%,.2f",
                    symbol, observation.getAmount());
            String title = observation.isCredit()
                    ? context.getString(R.string.notif_credit_title, amount)
                    : context.getString(R.string.notif_debit_title, amount);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.baseline_money_24)
                    .setContentTitle(title)
                    .setContentText(context.getString(R.string.notif_tap_to_categorize))
                    .setContentIntent(contentIntent)
                    .setAutoCancel(true);

            manager.notify((int) System.currentTimeMillis(), builder.build());
        } catch (SecurityException e) {
            // Notification permission not granted — the observation is still saved
            Log.d(TAG, "Notification skipped: " + e.getMessage());
        }
    }
}
