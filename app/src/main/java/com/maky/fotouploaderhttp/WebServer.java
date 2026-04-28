package com.maky.fotouploaderhttp;

import android.util.Base64;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

public final class WebServer {


    public static String SERVER_URL = "http://bazant.azurewebsites.net/!test/marian/ekarta/";
    public static String SERVER_URL_ANKETA = "http://anketa.westeurope.cloudapp.azure.com/notifikator/";
    public static String SERVER_LOGIN = "user";
    public static String SERVER_PASSWORD = "user";
    public static boolean isBasicAuthorisation = false;


    public WebServer() {
    }

    public static HttpURLConnection getConnection(String adresa){

        return getConnectionFullUrl(SERVER_URL + adresa, false);
    }

    public static HttpURLConnection getConnectionNotifikator(){
        try {
            InetAddress address = InetAddress.getByName("maky.ddns.net");
            String ip = address.getHostAddress();
            URL url = new URL("http://"+ip+"/privat/notifikator/registerToken.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String userCredentials = "user:user";
            String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes(), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", basicAuth);
            connection.setRequestMethod("POST");
            return(connection);
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }
    }


    public static HttpURLConnection getConnectionFullUrl(String adresa, boolean autorizuj){
        try {
            URL url = new URL(adresa);
//            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress("proxy-fra.hosting.heiway.net", 88));
//            HttpURLConnection con = (HttpURLConnection) url.openConnection(proxy);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setDoOutput(true);
            con.setConnectTimeout(2000);
            con.setReadTimeout(2000);
            if (autorizuj) {
                String userPass = SERVER_LOGIN + ":" + SERVER_PASSWORD;
                String basicAuth = "Basic " + Base64.encodeToString(userPass.getBytes(), Base64.DEFAULT);//or
                con.setRequestProperty("Authorization", basicAuth);
            }
            return con;

        }catch (MalformedURLException mue){
            mue.printStackTrace();
            return null;
        }catch (ProtocolException e){
            e.printStackTrace();
            return null;
        }catch (IOException e){
            e.printStackTrace();
            return null;
        }

    }


}


