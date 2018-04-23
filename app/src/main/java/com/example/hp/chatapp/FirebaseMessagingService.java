package com.example.hp.chatapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.messaging.RemoteMessage;

/**
 * Created by HP on 10-04-2018.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //This function states that when message is received what we want to do
        super.onMessageReceived(remoteMessage);
        //payload in nodejs is sending this remote message so to show name of sender that is sending request info get from remote message
        /*String notification_title = remoteMessage.getNotification().getTitle();
        String notification_body = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();*/
        String from_user_id = remoteMessage.getData().get("from_user_id");
        String notification_title = remoteMessage.getData().get("title");
        String notification_body = remoteMessage.getData().get("body");
        String click_action = remoteMessage.getData().get("click_action");

        //create notification  by notification builder
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(notification_title)
                .setContentText(notification_body);
        //Now on clicking the notification
        Intent resultIntent = new Intent(click_action);
        resultIntent.putExtra("user_id",from_user_id);
        //this is required because when notification is clicked it leads to profileActivity which need from_user_id

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);
        //Sets an id for notifiation each time different id
        int  mNotificationId = (int) System.currentTimeMillis();
        //Get an instance of NotificationManager Service
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //Builds the notification and issues it
        notificationManager.notify(mNotificationId, mBuilder.build());
    }
}
