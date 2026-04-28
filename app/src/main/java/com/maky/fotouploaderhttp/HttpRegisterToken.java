package com.maky.fotouploaderhttp;


import android.app.Application;
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

public class HttpRegisterToken extends AsyncTask<String, Void, String> {

    public String token;

    public HttpRegisterToken(Context context, String token) {
        this.token = token;
    }

    public String returnValue = "";
    public String stav = "new";

    protected String doInBackground(String... urls) {
        try {

            String parameterString = urls[0];

            try {

                HttpURLConnection con = WebServer.getConnectionNotifikator();

                DataOutputStream out = new DataOutputStream(con.getOutputStream());
                out.writeBytes(parameterString);
                out.flush();

                Log.i(SendPictureActivity.tag, "HttpRegisterToken: " +  con.getURL().toString());

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
                if (returnValue.contains("OK_ZAPISANE")) {  // ak tam nie je chyba, tak ulozim token
                    SendPictureActivity.ulozToken(token);
                }
                in.close();
                con.disconnect();

            }catch(Exception e){
                System.err.println("ERROR224");
                e.printStackTrace();
                stav = "chyba21";

            }
            stav = "odoslane";
            return returnValue;
        } catch (Exception e) {
            e.printStackTrace();
            stav = "chyba22";
            return null;
        } finally {
            stav = "odoslane";
            return returnValue;
        }
    }

}
