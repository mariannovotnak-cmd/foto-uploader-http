package com.maky.fotouploaderhttp;

import android.app.Activity;
import android.app.ProgressDialog;
import androidx.exifinterface.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.util.*;
import android.view.View;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Created by P3500130 on 25. 3. 2016.
 */
public class AsyncCallWS extends AsyncTask<String, String, HashMap<String,Object>> {

    private File[] files;
    private String komentar;
    private SendPictureActivity ma;
    private List<String> vystupWS = new ArrayList<String>();;
    //        int SIZE = 2000000;
    int SIZE = 1000000;
    int pocetPrenesenychBalikov = 0;


    public AsyncCallWS(File[] files, Activity ma) {
        //this.filename = filename;
        this.files = files;
        this.komentar = komentar;
        this.ma = (SendPictureActivity) ma;
    }



        ProgressDialog pdLoading;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        //    pdLoading = new ProgressDialog(ma);
            // Set your ProgressBar Title
       //     pdLoading.setTitle("File upload");
            //mProgressDialog.setIcon(R.drawable.dwnload);
            // Set your ProgressBar Message
       //     pdLoading.setMessage(files[0].getName() + " + " + (files.length - 1));
            ma.setProgressText(files[0].getName() + " + " + (files.length - 1));
       //     pdLoading.setIndeterminate(false);
            int pocetBalikovNaPrenos = 0;
            for(int i=0; i<files.length; i++){
                long velkostSuboru = files[i].length();
                int pocetBalikov = Math.round((velkostSuboru + SIZE - 1) / SIZE);
                pocetBalikovNaPrenos = pocetBalikovNaPrenos + pocetBalikov;
            }
       //     pdLoading.setMax(pocetBalikovNaPrenos);
       //     pdLoading.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
            // Show ProgressBar
       //     pdLoading.setCancelable(false);
            //  mProgressDialog.setCanceledOnTouchOutside(false);
       //     pdLoading.show();
            ma.pb.setMax(pocetBalikovNaPrenos);


        }
        @Override
        protected HashMap<String,Object> doInBackground(String... params) {

            //this method will be running on background thread so don't update UI frome here
            //do your long running http tasks here,you dont want to pass argument and u can access the parent class' variable url over here

            //uploadFile("/storage/emulated/0/DCIM/Camera/IMG_20160208_164752.jpg");
            for(int i=0; i<files.length; i++) {
                List<String> vystup = uploadFile2026(files[i]);
                if(vystup!= null && !vystup.isEmpty()){
                    vystupWS.addAll(vystup);
                }

                // vystupWS = uploadFile(files[i], komentar);
             //   vystupWS = new HttpAllFileUpload().sendFile(files[i].getPath());
            }
            return null;
        }

        @Override
        protected void onPostExecute(HashMap<String,Object> result) {

             ma.setVystup("");
            Iterator<String> it = vystupWS.iterator();
            while (it.hasNext()){
                ma.addVystup(it.next());
            }
            ma.exitButton.setVisibility(View.VISIBLE);
 //           pdLoading.dismiss();
        }



    public List<String> uploadFile(File file, String komentar) {


        Log.i("MainActivity.uploadFile", " -  file:" + file.getAbsolutePath());

        Call_WS_Upload ws = new Call_WS_Upload();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

        List<String> vysledok = new ArrayList<String>();

        long datumTaken = 0;
        try{
            ExifInterface exif = new ExifInterface(file.getAbsolutePath());
            float latLong[] = {0,0};
            exif.getLatLong(latLong);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                datumTaken = exif.getDateTime();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        File f = file;

        int stranka = 1;
        Base64File b64f;
        b64f = encodeFileToBase64Binary(f, stranka);
        while (b64f != null && b64f.data != null){
                HashMap mapa = new HashMap();
                mapa.put("URL", "maky.ddns.net");
                mapa.put("serviceName", "FileUpload");
                mapa.put("nazov", f.getName() + "_" + stranka);
                if (datumTaken > 0){ // ak mam datumTaken, tak ho pouzijem, ak nie, tak vezmem datum zmeny.
                    mapa.put("datum", sdf.format(datumTaken));
                }else {
                    mapa.put("datum", sdf.format(f.lastModified()));
                }
                mapa.put("data", b64f.data);
                if (stranka == 1) {
                    // komentar davam len k hlavicke. Nemusim to posielat dookola.
                    mapa.put("komentar", komentar);
                }
                int koniec = 0;
                if (b64f.koniec){
                    koniec = 1;
                }
                mapa.put("id", koniec);

                Log.i("MainActivity", " - file:" + f.getName() + " s:" + stranka + " encoded");
                Log.i("MainActivity", " - file:" + f.getName() + " koniec=" + b64f.koniec);

                //ws.CallService(mapa);
                HashMap vystupMap = ws.Call(mapa);
                if (vystupMap != null && vystupMap.containsKey("vysledok")){
                    vysledok.add(vystupMap.get("vysledok").toString());
                }

                String[] message = new String[2];
                pocetPrenesenychBalikov ++;
                message[0] = pocetPrenesenychBalikov + "";
                message[1] = file.getName();
                stranka ++;
                b64f = encodeFileToBase64Binary(f, stranka);
                publishProgress(message);
        }
        return vysledok;

    }



    public List<String> uploadFile2026 (File file) {

        List<String> vysledok = new ArrayList<String>();

        Log.i("MainActivity.uploadFile2026", " - file:" + file.getAbsolutePath());
        String boundary = "===" + System.currentTimeMillis() + "===";
        String LINE_FEED = "\r\n";

        try {
            InetAddress address = InetAddress.getByName("maky.ddns.net");
            String ip = address.getHostAddress();
            URL url = new URL("http://"+ip+"/uploader/upload_html.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            String userCredentials = "user:user";
            String basicAuth = "Basic " + Base64.encodeToString(userCredentials.getBytes(), Base64.NO_WRAP);
            connection.setRequestProperty("Authorization", basicAuth);

            connection.setUseCaches(false);
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            DataOutputStream request = new DataOutputStream(connection.getOutputStream());

            // --- FILE PART ---
            request.writeBytes("--" + boundary + LINE_FEED);
            request.writeBytes("Content-Disposition: form-data; name=\"fileToUpload\"; filename=\""
                    + file.getName() + "\"" + LINE_FEED);
            request.writeBytes("Content-Type: application/octet-stream" + LINE_FEED);
            request.writeBytes(LINE_FEED);

            FileInputStream inputStream = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                request.write(buffer, 0, bytesRead);
            }

            inputStream.close();
            request.writeBytes(LINE_FEED);

            // --- EXTRA FIELD (datum) ---
            request.writeBytes("--" + boundary + LINE_FEED);
            request.writeBytes("Content-Disposition: form-data; name=\"datum\"" + LINE_FEED);
            request.writeBytes(LINE_FEED);
            request.writeBytes("2026-04-17" + LINE_FEED);

            // --- END ---
            request.writeBytes("--" + boundary + "--" + LINE_FEED);
            request.flush();
            request.close();

            int responseCode = connection.getResponseCode();

            InputStream responseStream = (responseCode == 200)
                    ? connection.getInputStream()
                    : connection.getErrorStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(responseStream));
            StringBuilder response = new StringBuilder();

            String line;
            while ((line = reader.readLine()) != null) {
                if(line.contains("ERROR") || line.contains("INFO")) {
                    response.append(line).append("\n");
                }
            }
            reader.close();

            Log.d("UPLOAD_RESPONSE", response.toString());

            vysledok.add(file.getName() + " HTTP:"+responseCode);
            vysledok.add(response.toString() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("MainActivity.uploadFile2026", " - e:" + e.getMessage());
        }
        return vysledok;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        int prenasanyBalik = new Integer(values[0]).intValue();
  //      pdLoading.setMessage(values[1]);
  //      pdLoading.setProgress(prenasanyBalik);
        ma.setProgressStatus(prenasanyBalik);
    }


    /**
     * Method used for encode the file to base64 binary format
     * @param file
     * @return encoded file format
     */
    private Base64File encodeFileToBase64Binary(File file, int stranka){



        Base64File vystup = new Base64File();
        vystup.koniec = false;

        int strankaOd = (stranka - 1) * SIZE;
        int strankaDo = (stranka * SIZE) - 1;
        if (file.length() < strankaOd){
            // uz som na konci suboru
            vystup.koniec = true;
            return null;
        }

        if (strankaDo > file.length()){
            strankaDo = (int)file.length();
            vystup.koniec = true;
        }

        Log.i("Base64"," - "+strankaOd + "/" + strankaDo + " - " + file.length());



        String encodedfile = null;
        try {
            int pocet = SIZE;
            if ((file.length() - strankaOd) < SIZE){
                pocet = (int)file.length() - strankaOd;
                vystup.koniec = true;
            }



            FileInputStream fileInputStreamReader = new FileInputStream(file);
            byte[] bytes; // = new byte[pocet];

            //fileInputStreamReader.read(bytes,strankaOd,pocet);
            bytes = toByteArray(file,strankaOd,pocet);
            encodedfile = android.util.Base64.encodeToString(bytes, android.util.Base64.DEFAULT);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        vystup.data = encodedfile;

        return vystup;
    }

    public static byte[] toByteArray(File file, long start, long count) throws Exception{
        long length = file.length();
        if (start >= length) return new byte[0];
        count = Math.min(count, length - start);
        byte[] array = new byte[(int)count];
        InputStream in = new FileInputStream(file);
        in.skip(start);
        long offset = 0;
        while (offset < count) {
            int tmp = in.read(array, (int)offset, (int)count);
            offset += tmp;
        }
        in.close();
        return array;
    }

    public class Base64File{
        public String data;
        public boolean koniec;
    }

}