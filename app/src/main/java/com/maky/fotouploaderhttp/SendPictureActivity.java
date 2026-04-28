package com.maky.fotouploaderhttp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.MediaStore;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import com.maky.fotouploader.R;
//import com.maky.gpxuploader.R;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.location.Location;
import android.location.LocationManager;


public class SendPictureActivity extends Activity   {


    public static String tag = "com.maky.fotouploaderhttp";
    public String token = "";
    public String guid = "";
    public static SharedPreferences settingsRe;
    public static String meno;

    //public LocationManager locationManager;

    private String[] fileNames = {};
    Button uploadButton;
    Button exitButton;
    TextView textVystup;
    TextView poznamkaLabel;
    ProgressBar pb;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };

    private static String[] PERMISSIONS_STORAGE_2 = {
            Manifest.permission.ACCESS_MEDIA_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };

    @Override
    protected void onCreate (Bundle savedInstanceState) {

        // Storage Permissions


  //      FirebaseApp.initializeApp(this);

        Log.i(tag, "onCreate - štarujem aplikáciu");

        Intent intent = getIntent();
        fileNames = intent.getStringArrayExtra("fileNames");

        super.onCreate(savedInstanceState);
        this.setTitle("Maky foto uploader");
        setContentView(R.layout.activity_upload);

        uploadButton =  (Button) findViewById(R.id.button);
        uploadButton.setVisibility(View.INVISIBLE);
        exitButton =  (Button) findViewById(R.id.buttonExit);
        exitButton.setVisibility(View.VISIBLE);

        poznamkaLabel =  (TextView) findViewById(R.id.textMsgLabel);
        textVystup =  (TextView) findViewById(R.id.textVystup);
        pb = (ProgressBar) findViewById(R.id.progressBanner);


        // Get intent, action and MIME type
        String action = intent.getAction();
        String type = intent.getType();

        meno= Settings.Secure.getString(getContentResolver(),"bluetooth_name");
        if(meno == null){
            meno = Build.MODEL;
        }


        verifyStoragePermissions(this);

        if (Intent.ACTION_SEND.equals(action) && type != null) {
            Log.i(tag, "SendPictureActivity - new Intent ACTION_SEND - akcia:" + action);
            if ("text/plain".equals(type)) {
                handleSendText(intent); // Handle text being sent
            } else if (type.startsWith("image/")) {
                handleSendImage(intent); // Handle single image being sent
            }
        } else if (Intent.ACTION_SEND_MULTIPLE.equals(action) && type != null) {
            Log.i(tag, "SendPictureActivity - new Intent ACTION_SEND_MULTIPLE - akcia:" + action);
            if (type.startsWith("image/")) {
                handleSendMultipleImages(intent); // Handle multiple images being sent
            }
        } else {

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Povoľ prístup k polohe", Toast.LENGTH_LONG).show();
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this, "Povoľ prístup k notifikáciam", Toast.LENGTH_LONG).show();
            }


            textVystup.setText("");
            poznamkaLabel.setText("Ak chceš upload, tak cez sharing v galerii");
            FirebaseMessaging.getInstance().setAutoInitEnabled(true);
            FirebaseMessaging.getInstance().setDeliveryMetricsExportToBigQuery(true);

            FirebaseMessaging.getInstance().setAutoInitEnabled(true);

            settingsRe = getPreferences(Context.MODE_PRIVATE);
            guid = settingsRe.getString("guid", "");
            token = settingsRe.getString("token", "");

            if (guid == ""){
                SharedPreferences.Editor editor = settingsRe.edit();
                guid = java.util.UUID.randomUUID().toString();
                editor.putString("guid",guid );
                editor.commit();

            }

            FirebaseMessaging.getInstance().getToken().addOnCompleteListener(new OnCompleteListener<String>() {
                @Override
                public void onComplete(@NonNull Task<String> task) {
                    if(!task.isSuccessful()){
                        return;
                    }
                    // Get new Instance ID token
                    String lToken = task.getResult();
                    if(!lToken.equals(token)) {
                        token = lToken;
                        SendPictureActivity.this.RegistrujToken(meno + "_FU");
                        /*
                        SharedPreferences.Editor editor = settingsRe.edit();
                        editor.putString("token",lToken );
                        editor.commit();
                        */
                    }

                }
            });
        }


    }


    public static void ulozToken(String token){

        /* ak sa token zapise aj na server, tak sa ulozi do lokalneho registra aby sa znova neregistroval */

        SharedPreferences.Editor editor = settingsRe.edit();
        editor.putString("token",token );
        editor.commit();

    }



    public void RegistrujToken(String login){


        if(login != "noname") {

            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1) {
                String pkg = getPackageName();
                PowerManager pm = getSystemService(PowerManager.class);

                // skontrolujem, ci aplikacia nema zapnutu oprimalizaciu
                if (!pm.isIgnoringBatteryOptimizations(pkg)) {
                    Intent i =
                            new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
                                    .setData(Uri.parse("package:" + pkg));

                    startActivity(i);
                }
            }

        }

        // Log and toast
        String msg = "Prihlásený";
        Log.i(tag, msg);

        if(login != "noname") {
            Toast.makeText(SendPictureActivity.this, msg, Toast.LENGTH_SHORT).show();
        }

        String parameter =  "service=com.maky.fotouploaderhttp";
        parameter+=  "&userId="+ Uri.encode(login);
        parameter+=  "&token="+ Uri.encode(token);
        parameter+=  "&deviceGuid="+ Uri.encode(guid);
        parameter+=  "&deviceOS=android";

        try {
            //parameter = URLEncoder.encode(parameter, "UTF-8");
            HttpRegisterToken myHttp = new HttpRegisterToken(getApplicationContext(), token);
            // toto caka, kym sa request nevykona
            myHttp.execute(parameter).get();
        } catch (Exception e){
            e.printStackTrace();
        }

        // Log and toast
        Log.i(tag, "RegistrujToken - " +  token);
        //    Toast.makeText(MainActivity.this, msg, Toast.LENGTH_SHORT).show();


    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onStop() {
        if (exitButton.getVisibility() == View.VISIBLE){
            this.finish();
        }
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    public void onUploadClick(View view) {

        if (fileNames == null)
            return;


        File[] subory = new File[fileNames.length];

        if (fileNames != null){

            for (int i = 0; i < fileNames.length; i++) {
                subory[i] = new File(fileNames[i]);
            }

            uploadButton.setVisibility(View.INVISIBLE);
            exitButton.setVisibility(View.INVISIBLE);

            ExecutorService executor = Executors.newSingleThreadExecutor();

            executor.execute(() -> {
                AsyncCallWS asc = new AsyncCallWS(subory, this);

                // sem daj logiku z doInBackground()
                asc.doInBackground();

                runOnUiThread(() -> {
                    // sem daj logiku z onPostExecute()
                    asc.onPostExecute(null);
                });
            });

        }
    }

    public void onExitClick(View view) {
        this.textVystup.setText("");
        uploadButton.setVisibility(View.INVISIBLE);
        exitButton.setVisibility(View.VISIBLE);
        super.onBackPressed();
        // onStop();
    }

    public void onGaleriaClick(View view) {
        String url = "https://maky.ddns.net/uploader/index.php"; // otvorenie galerie
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }


    public void onDaikinClick(View view) {
        String url = "https://maky.ddns.net/privat/daikin/web_gui/ui.html";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }


    public void onFondyClick(View view) {
        String url = "https://maky.ddns.net/privat/fondy/fondy.php";
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }


    public void uploadFoto() {
        /*
        Intent intent = new Intent(SendPictureActivity.this, SendPictureActivity.class);
        String[] fileArray = filenames.toArray(new String[filenames.size()]);
        intent.putExtra("fileNames", fileArray);
        startActivity(intent);
        */

     //   fileNames = files;
        uploadButton.setText(uploadButton.getText() + " (" + fileNames.length + ")");
        uploadButton.setVisibility(View.VISIBLE);

        String fileList = "";
        for (int i=0; i<fileNames.length; i++){
             fileList = fileList + fileNames[i] + "/";
        }
        setVystup(fileList);
    }


    void handleSendText(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {

            Log.i("handleSendText", sharedText);

            // Update UI to reflect text being shared
        }
    }

    void handleSendImage(Intent intent) {
        Uri imageUri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
        if (imageUri != null) {
            Log.i("handleSendImage", imageUri.getEncodedPath());
            fileNames = new String[1];
            fileNames[0] = this.getPath(imageUri);
            uploadFoto();
           // finish();
        }
    }

    void handleSendMultipleImages(Intent intent) {
        ArrayList<Uri> imageUris = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
        if (imageUris != null) {

            Iterator<Uri> it = imageUris.iterator();
            Collection<String> fileName = new ArrayList<>();
            while (it.hasNext()){
                Uri imageUri = it.next();
                fileName.add(this.getPath(imageUri));
            }
            String[] arr = new String[fileName.size()];
            fileNames = fileName.toArray(arr);
            uploadFoto();

            Log.i("handleSendMultipleImag.", "Size" + imageUris.size());
            // Update UI to reflect multiple images being shared
        }
    }



    public String getPath(Uri uri)
    {
        String[] projection = {MediaStore.Images.Media.DATA };
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if (cursor == null) return null;
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String s=cursor.getString(column_index);
        cursor.close();
        return s;
    }


    public void setVystup(String data){
        TextView tvvystup = (TextView) findViewById(R.id.textVystup);
        tvvystup.setText(data);
    }
    public void addVystup(String data){
        TextView tvvystup = (TextView) findViewById(R.id.textVystup);
        tvvystup.append(data + System.getProperty("line.separator"));
    }
    public void setProgressStatus(int progress){
        pb = (ProgressBar) findViewById(R.id.progressBanner);
        TextView pt = (TextView) findViewById(R.id.progressText);
        pb.setProgress(progress);
        pt.setText(Math.round(pb.getProgress() * 100 / pb.getMax()) + " %");
    }
    public void setProgressText(String text){
        TextView pt = (TextView) findViewById(R.id.progressFileName);
        pt.setText(text);
    }


    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_MEDIA_IMAGES);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        int permissionGranted = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissionGranted != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, PERMISSIONS_STORAGE_2, REQUEST_EXTERNAL_STORAGE);
        }
    }

}
