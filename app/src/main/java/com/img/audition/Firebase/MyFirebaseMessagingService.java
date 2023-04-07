package com.img.audition.Firebase;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.RingtoneManager;
import android.os.Build;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.img.audition.R;
import com.img.audition.screens.SplashActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import androidx.core.app.NotificationCompat;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "FCM Service";

    Bitmap bitmap;
    String id;
    String prod_id="";
    String title="Champions";
    String message;
    String type="";
    int x = 0;


    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // ...

        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ

        Log.i(TAG+"1", "From: " + remoteMessage.getFrom());

        Log.i("Received notification",remoteMessage.toString());

        System.out.println("From: " + remoteMessage.getFrom());

        id=  remoteMessage.getMessageId();

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Log.i(TAG+"2", "Message data payload: " + remoteMessage.getData());
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.i(TAG+"3", "Message Notification Body: " + remoteMessage.getNotification().getTitle());
            message= remoteMessage.getNotification().getBody();
            title= remoteMessage.getNotification().getTitle();
        }

        Log.i("mess",remoteMessage.getData().toString());

        System.out.println("mess  "+remoteMessage.getData().toString());

        String data=remoteMessage.getData().toString();
        Log.i(TAG,data);
        try {
            JSONObject mainob=new JSONObject(data);

            JSONObject ob=mainob.getJSONObject("data");

            title = ob.getString("title");
            message = ob.getString("message");

            type= "Notification";

        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i(TAG+"4", "Message Notification Body: " + message);
        Log.i(TAG+"5", "Message Notification Body: " + prod_id);
        Log.i(TAG+"6", "Message Notification Body: " + type);


        noti(message,title, bitmap, type);
//        sendNotification(message,title, bitmap, type,prod_id);
    }

    public void noti(String messageBody,String title, Bitmap image, String TrueOrFalse)
    {

        NotificationManager notificationManager = (NotificationManager)
                getSystemService(Context.NOTIFICATION_SERVICE);

        Intent notificationIntent;
        notificationIntent = new Intent(this, SplashActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
                | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent intent =
                PendingIntent.getActivity(this, 0,
                        notificationIntent,PendingIntent.FLAG_IMMUTABLE);

        String message=messageBody;
        int icon = R.drawable.logo;
        long when = System.currentTimeMillis();

        Notification notification;

        if(!type.equals("")) {
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(intent)
                    .setSmallIcon(icon)
                    .setWhen(when)
                    .setStyle(new NotificationCompat.BigPictureStyle()
                            .bigPicture(image).setSummaryText(message))
                    .build();
        }

        else {
            notification = new NotificationCompat.Builder(this)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setContentIntent(intent)
                    .setSmallIcon(icon)
                    .setWhen(when)
                    .build();
        }
        notification.flags |= Notification.FLAG_AUTO_CANCEL;

        notification.defaults |= Notification.DEFAULT_SOUND;
        notification.defaults |= Notification.DEFAULT_VIBRATE;

        x= new Random().nextInt(101);
        notificationManager.notify(x, notification);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), "notify_001");
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
                notificationIntent,PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.BigTextStyle bigText = new NotificationCompat.BigTextStyle();
        bigText.bigText(message);
//        bigText.setBigContentTitle("title 2");
//        bigText.setSummaryText("title 3");

        Log.i("FCM Message",message);

        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setSmallIcon(R.drawable.noti_icon);
        mBuilder.setContentTitle(title);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mBuilder.setPriority(Notification.PRIORITY_MAX);
        mBuilder.setStyle(bigText);
        mBuilder.setContentText(message).build();

        /*notification = new NotificationCompat.Builder(this,"notify_001")
                .setContentTitle(title)
                .setContentText(message)
                .setContentIntent(intent)
                .setSmallIcon(R.drawable.logo_grad)
                .setWhen(when)
                .setPriority(Notification.PRIORITY_MAX)
                .setStyle(bigText)
                .build();*/

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("notify_001",
                    "Notification Channel",
                    NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(x, mBuilder.build());
    }
}