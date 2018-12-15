package com.sangamesh.android.cabnotifier;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Telephony;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListener extends BroadcastReceiver {

    private static final String SMS_RECEIVED_ACTION = "android.provider.Telephony.SMS_RECEIVED";
    private static final String TAG = "MessageListener";
    private static String[] senderSubStrings = {"MISETS", "MVINSY"}; //TODO: Load from sharedpreferences
    private static String delimiters = "[\\s-]";
    private static String regex =  ".*([A-Za-z]{2}" + delimiters + "\\d{2}" + delimiters
            + "[A-Za-z]" + delimiters + "\\d{%d}).*";
    private static String sampleMsg = "Your drop is scheduled on 12/14/18 5:31 PM by Cab: TW - 497 (KA-41 C 1736)";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("MessageListener", "OnReceive enter");

        try {
            String action = intent.getAction();
            if (!SMS_RECEIVED_ACTION.equals(action)) {
                Log.i(TAG, "Action: " + action + " does not equal " + SMS_RECEIVED_ACTION + ". Returning.");
                return;
            }

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
                for (int i = 4; i > 0; --i) {
                    String patternString = String.format(regex, i);
                    Pattern pattern = Pattern.compile(patternString);
                    Matcher matcher = pattern.matcher(body);
                    if (matcher.find()) {
                        String cabNumber = matcher.group(1);
                        Log.i(TAG, "Identified Cab Number: " + cabNumber);
                        //TODO: Push it as a notification
                        break;
                    }
                    Log.w(TAG, "Could not find Vehicle number for i value: " + i);
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
        for (String senderSubString : senderSubStrings) {
            if (sender.contains(senderSubString)) {
                return true;
            }
        }
        //TODO: change this to false
        return true;
    }
}
