package com.hemant239.chatbox;


import android.content.Context;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.onesignal.OSMutableNotification;
import com.onesignal.OSNotification;
import com.onesignal.OSNotificationReceivedEvent;
import com.onesignal.OneSignal.OSRemoteNotificationReceivedHandler;

import org.json.JSONObject;

import java.util.Objects;

public class NotificationServiceExtension implements OSRemoteNotificationReceivedHandler {

    @Override
    public void remoteNotificationReceived(Context context, OSNotificationReceivedEvent notificationReceivedEvent) {
        OSNotification notification = notificationReceivedEvent.getNotification();

        Log.i("FUCKER", "reached here");
        String phone = notification.getTitle();

        Log.i("FUCKER", "phone is :" + phone);
        if (AllChatsActivity.allContacts.get(phone) != null) {
            phone = Objects.requireNonNull(AllChatsActivity.allContacts.get(phone)).getName();
            Log.i("FUCKER", "inside if conditions:");
            Log.i("FUCKER", " new phone is :" + phone);
        }
        // Example of modifying the notification's accent color
        OSMutableNotification mutableNotification = notification.mutableCopy();


        String finalPhone = phone;
        mutableNotification.setExtender(builder -> builder.setSmallIcon(R.drawable.ic_baseline_local_phone_24)
                .setContentTitle(finalPhone)
                .setStyle(new NotificationCompat.DecoratedCustomViewStyle())
                .setContentText(notification.getBody()));


        JSONObject data = notification.getAdditionalData();
        Log.i("OneSignalExample", "Received Notification Data: " + data);

        // If complete isn't call within a time period of 25 seconds, OneSignal internal logic will show the original notification
        // To omit displaying a notification, pass `null` to complete()
        notificationReceivedEvent.complete(mutableNotification);
    }
}
