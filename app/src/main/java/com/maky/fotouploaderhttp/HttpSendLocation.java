package com.maky.fotouploaderhttp;


import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;

/**
 * Created by P3500130 on 24.7.2018.
 */

public class HttpSendLocation extends AsyncTask<String, Void, String> {

    public String response = null;

    public HttpSendLocation(Context context) {

        response = null;
    }

    public String returnValue = "";
    public String stav = "new";

    protected String doInBackground(String... urls) {
        try {

            String parameterString = urls[0];

            try {
                HttpURLConnection con = WebServer.getConnectionFullUrl("http://maky.ddns.net/gps_tracker/GpsLog.php ", false);
//                HttpURLConnection con = WebServer.getConnectionFullUrl("http://95.102.25.111/gps_tracker/GpsLog.php ", false);
                con.setRequestMethod("GET");
                con.setReadTimeout(2000);
                con.setConnectTimeout(2000);

                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                out.writeBytes(parameterString);
                out.flush();
                Log.i(SendPictureActivity.tag, "HttpSendLocation: " +  con.getURL().toString());

                int status = con.getResponseCode();
                out.close();

                BufferedReader in = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                returnValue = content.toString();
                in.close();
                con.disconnect();

            }catch(Exception e){
                System.err.println("ERROR524");
                e.printStackTrace();
                stav = "chyba524";

            }
            stav = "odoslane";
            return returnValue;
        } catch (Exception e) {
            e.printStackTrace();
            stav = "chyba522";
            return null;
        } finally {
            stav = "odoslane";
            return returnValue;
        }
    }

}
