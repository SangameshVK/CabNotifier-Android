package com.sangamesh.android.cabnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListener extends BroadcastReceiver {

    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "MessageListener";

    private String mSenderSubStrings[] = {"MISETS", "MVINSY"}; //TODO: Load from sharedpreferences
    private String mDelimiters;
    private String mRegex;

    private static String sampleMsg = "Your drop is scheduled on 12/14/18 5:31 PM by Cab: TW - 497 (KA-41 AC 1736)";

    public MessageListener() {
        mDelimiters = "[\\s-]";
        mRegex =  ".*([A-Za-z]{2}" + mDelimiters + "\\d{1,2}" + mDelimiters
                + "[A-Za-z]{1,2}" + mDelimiters + "\\d{1,4}).*";
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MessageListener", "OnReceive enter");

        try {
            String action = intent.getAction();
            Toast.makeText(context, "Received Action " + action, Toast.LENGTH_SHORT).show();
            if (!SMS_RECEIVED_ACTION.equals(action)) {
                //TODO: Remove these two LOC
                //String cabNumber = spotCabNumber(sampleMsg);
                //notifyCabNumber(context, cabNumber);
                Log.i(TAG, "Action: " + action + " does not equal " + SMS_RECEIVED_ACTION + ". Returning.");
                return;
            }

            // TODO: Make app compatible with older versions (Older than Api level 19?) by using PDUs
            SmsMessage smsMessages[] = Telephony.Sms.Intents.getMessagesFromIntent(intent);
            if (smsMessages == null || smsMessages.length == 0) {
                Log.e(TAG, "SmsMessages does not contain any SMS");
                return;
            }
            for (SmsMessage smsMessage : smsMessages) {
                String from = smsMessage.getOriginatingAddress();
                String body = smsMessage.getMessageBody();
                Log.i(TAG, "Message From: " + from + "\n Body: " + body);
                if (!isSenderMoveInSync(from)) {
                    continue;
                }
                Log.i(TAG, "Message from MoveInSync spotted.");
                String cabNumber = spotCabNumber(body);
                if (!TextUtils.isEmpty(cabNumber)) {
                    notifyCabNumber(context, cabNumber);
                } else {
                    Log.e(TAG, "Could not spot Cab Number");
                    notifyError(context);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.toString());
        }
    }

    private boolean isSenderMoveInSync(String sender) {
        if (TextUtils.isEmpty(sender)) {
            throw new IllegalArgumentException("Argument sender is null or empty");
        }
        for (String senderSubString : mSenderSubStrings) {
            if (sender.contains(senderSubString)) {
                return true;
            }
        }
        //TODO: change this to false
        return true;
    }

    private String spotCabNumber(String messageBody) {
        Pattern pattern = Pattern.compile(mRegex);
        Matcher matcher = pattern.matcher(messageBody);
        if (matcher.find()) {
            String cabNumber = matcher.group(1);
            Log.i(TAG, "Identified Cab Number: " + cabNumber);
            return cabNumber;
        }
        Log.e(TAG, "Could not find Vehicle number in message body: " + messageBody);
        return null;
    }

    private void notifyCabNumber(Context context, String cabNumber) {
        // TODO: Use custom content view
        // https://developer.android.com/reference/android/app/Notification.Builder#setCustomContentView(android.widget.RemoteViews)
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText(cabNumber)
                .setPriority(NotificationCompat.PRIORITY_MAX);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(Constants.CAB_NOTIFICATION_ID, notificationBuilder.build());
    }

    private void notifyError(Context context) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentText("Could not Identify Cab number in the text message")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify(Constants.ERROR_NOTIFICATION_ID, notificationBuilder.build());
    }
}
