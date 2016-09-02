package com.ftv_fashionshop.helixtech_android;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONException;
import org.json.JSONObject;

import accounts.SocialProfileClasses;

/**
 * Created by admin on 13-06-2016.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FTV NOTIF";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // TODO(developer): Handle FCM messages here.
        // If the application is in the foreground handle both data and notification messages here.
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
        Log.d(TAG, "From: " + remoteMessage.getFrom());


        Log.d(TAG, "Notification Message Body: " + remoteMessage.getData());
//        {"message":"Hello5","title":"Test5"}
        sendNotification(remoteMessage.getData().get("data"));
    }

    private void sendNotification(String messageBody) {
        try {
            JSONObject jsonObject = new JSONObject(messageBody);


            Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentTitle(jsonObject.getString("title"))
                    .setContentText(jsonObject.getString("message"))
                    .setAutoCancel(true)
                    .setSound(defaultSoundUri);

            PendingIntent pendingIntent = null;
            if (jsonObject.has("type")) {
                if (jsonObject.getInt("type") == 1) {
                    accounts.AccountManager accountManager = new accounts.AccountManager(this);
                    if(accountManager.isSignedIn()){
                        Intent intent = new Intent(this, OfferProducts.class);
                        intent.putExtra("offer_id", jsonObject.getInt("offer_id"));
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                                PendingIntent.FLAG_ONE_SHOT);
                        notificationBuilder.setContentIntent(pendingIntent);
                    }


                } else if (jsonObject.getInt("type") == 2) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse("http://play.google.com/store/apps/details?id=" + jsonObject.getString("package_name")));
                    notificationBuilder.setContentIntent(PendingIntent.getActivity(this, 0, intent, 0));
                }
            }
            NotificationManager notificationManager =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
