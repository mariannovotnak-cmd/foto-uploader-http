package com.maky.fotouploaderhttp;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.maky.fotouploader.R;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.location.Location;
import android.widget.Toast;


public class MyFirebaseMessagingService extends FirebaseMessagingService {

    Poloha poloha;


    @Override
    public void onCreate() {
        poloha = new Poloha();

        super.onCreate();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        Log.i(SendPictureActivity.tag, "MyFirebaseMessagingService.onTaskRemoved");
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        Log.i(SendPictureActivity.tag, "MyFirebaseMessagingService.onDestroy - start");
        super.onDestroy();;
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages
        // are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data
        // messages are the type
        // traditionally used with GCM. Notification messages are only received here in
        // onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated
        // notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages
        // containing both notification
        // and data payloads are treated as notification messages. The Firebase console always
        // sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.i(SendPictureActivity.tag, "MyFirebaseMessagingService.onMessageReceived From: " + remoteMessage.getFrom());


        // Check if message contains a Data payload.
        if (remoteMessage.getData() != null) {
            Log.i(SendPictureActivity.tag, "MyFirebaseMessagingService.onMessageReceived Message Notification Title:" + remoteMessage.getData().get("title")
                    + " Message:" + remoteMessage.getData().get("message"));
        }

        String prikaz = remoteMessage.getData().get("title");
        String hodnota = remoteMessage.getData().get("message");
        boolean callGpsLogger = false;

        Log.i(SendPictureActivity.tag, "MyFirebaseMessagingService.onMessageReceived title/message: " + prikaz + "/" + hodnota);

        Intent i = new Intent("com.mendhak.gpslogger.TASKER_COMMAND");
        i.setPackage("com.mendhak.gpslogger");
        i.putExtra("setnextpointdescription", "MnoWebFw: " + prikaz + "/" + hodnota);

        if (prikaz.equals("immediatestart")) {
            i.putExtra("immediatestart", true);
            callGpsLogger = true;
        }
        if (prikaz.equals("immediatestop")) {
            i.putExtra("immediatestop", true);
            callGpsLogger = true;
        }
        if (prikaz.equals("logonce")) {
            i.putExtra("logonce", true);
            callGpsLogger = true;
        }
        if (prikaz.equals("settimebeforelogging")) {
            i.putExtra("settimebeforelogging", Integer.parseInt(hodnota));
            callGpsLogger = true;
        }
        if (prikaz.equals("setdistancebeforelogging")) {
            i.putExtra("setdistancebeforelogging", Integer.parseInt(hodnota));
            callGpsLogger = true;
        }
        if (prikaz.equals("setretrytime")) {
            i.putExtra("setretrytime", Integer.parseInt(hodnota));
            callGpsLogger = true;
        }
        if (prikaz.equals("setabsolutetimeout")) {
            i.putExtra("setabsolutetimeout", Integer.parseInt(hodnota));
            callGpsLogger = true;
        }

        if (prikaz.equals("log")) {
            Log.i(SendPictureActivity.tag, "Start log: " + poloha.latitude + "/" + poloha.longitude);
            getLastLocationNewMethod();
            sendLocation();
            return;
        }

        if (callGpsLogger) {
            Log.i(SendPictureActivity.tag, "MyFirebaseMessagingService.onMessageReceived: sendBroadcast to " + i.getPackage() + " " + prikaz);
            sendBroadcast(i);

        } else {
            String message = remoteMessage.getData().get("message");// + " Nenašla sa zhoda ["+prikaz+"]";

            // Check if message contains a data payload.
            if (remoteMessage.getData().size() > 0) {
                Log.i(SendPictureActivity.tag, "MyFirebaseMessagingService.onMessageReceived Message data payload: " + remoteMessage.getData());
                sendNotification(remoteMessage.getData().get("title"), message, remoteMessage.getFrom(), remoteMessage.getData().toString());
            }
        }
        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]


    // [START on_new_token]

    /**
     * Called if InstanceID token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the InstanceID token
     * is initially generated so this is where you would retrieve the token.
     */
    @Override
    public void onNewToken(String token) {
        Log.i(SendPictureActivity.tag, "MyFirebaseMessagingService.onNewToken Refreshed token: " + token);

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // Instance ID token to your app server.
        sendRegistrationToServer(token);
    }
    // [END on_new_token]


    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM InstanceID token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private void sendRegistrationToServer(String token) {
        // TODO: Implement this method to send token to your app server.
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     */
    public void sendNotification(String titul, String message, String from, String data) {
        Log.i(SendPictureActivity.tag, "MyFirebaseMessagingService.sendNotification Data:" + data);
//        Intent intent = new Intent(this, PushActivity.class);
        Intent intent = new Intent(this, SendPictureActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        intent.putExtra("title", titul);
        intent.putExtra("message", message + "/" + from + "/" + data);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME) + new Date()));
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent, PendingIntent.FLAG_IMMUTABLE);


        String channelId = getString(R.string.default_notification_channel_id);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setLargeIcon(BitmapFactory.decodeResource(this.getResources(), R.mipmap.ic_launcher_mno))
                        .setSmallIcon(R.drawable.ic_star_notification)
                        .setContentTitle(titul)
                        .setContentText(message)
                        .setBadgeIconType(NotificationCompat.BADGE_ICON_LARGE)
                        .setAutoCancel(false)
                        .setColorized(true)
                        .setColor(getResources().getColor(R.color.cervena))
                        .setContentIntent(pendingIntent);


        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "Channel human readable title",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("Kanál - Maky");
            channel.canShowBadge();
            channel.setShowBadge(true);
            channel.setName("Maky - info");
            notificationManager.createNotificationChannel(channel);
        }

        int notifyId = 1;
        notificationManager.notify(notifyId, notificationBuilder.build());

    }


    public void sendLocation() {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        String meno = SendPictureActivity.meno;
        if(meno == null){
            meno = Settings.Secure.getString(getContentResolver(),"bluetooth_name");
            if(meno == null){
                meno = Build.MODEL;
            }
        }


        String parameter = "lat=" + poloha.latitude;
        parameter += "&lon=" + poloha.longitude;  // 18&Provider=gps&user=test&dateGPS=2023-11-03T17:31:00\"userId="+ Uri.encode(login);
        parameter += "&Provider=" + poloha.provider;
        parameter += "&user=" + Uri.encode(meno);
        parameter += "&Speed=" + poloha.speed;
        parameter += "&dateGPS=" + sdf.format(poloha.time);

        Log.i(SendPictureActivity.tag, "MyFirebaseMessagingService.sendLocation("+parameter+")");

        try {
            //parameter = URLEncoder.encode(parameter, "UTF-8");
            HttpSendLocation myHttp = new HttpSendLocation(this);
            // toto caka, kym sa request nevykona
            myHttp.execute(parameter).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void getLastLocationNewMethod() {

        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Log.e(SendPictureActivity.tag, "getLastLocationNewMethod - Chýba prístup k polohe");
            Toast.makeText(this, "Povoľ prístup k polohe", Toast.LENGTH_LONG).show();
            return;
        }


        LocationManager locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        Location lastKnownLocation = locationManager
                .getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);

        // Check everytime this value, it may be null
        if(lastKnownLocation != null){
            double latitude = lastKnownLocation.getLatitude();
            double longitude = lastKnownLocation.getLongitude();

            Log.d(SendPictureActivity.tag, "getLastKnownLocation - "+latitude+"/"+longitude);

            // Use values as you wish
        }else {
            Log.d(SendPictureActivity.tag, "getLastKnownLocation - no location");
        }




        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            poloha.latitude = location.getLatitude();
                            poloha.longitude = location.getLongitude();
                            poloha.accuracy = location.getAccuracy();
                            poloha.provider = location.getProvider();
                            poloha.speed = location.getSpeed();
                            poloha.time = location.getTime();
                            Log.i(SendPictureActivity.tag, "getLastLocation - Poloha:"+poloha.latitude + "/" + poloha.longitude);
                        }else{
                            Log.d(SendPictureActivity.tag, "getLastLocationNewMethod - Nenačítal som polohu");
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(SendPictureActivity.tag, "getLastLocationNewMethod - Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }
   }

   class Poloha{
        double latitude;
        double longitude;
        double accuracy;
        String provider;
        float speed;
        long time;
   }
