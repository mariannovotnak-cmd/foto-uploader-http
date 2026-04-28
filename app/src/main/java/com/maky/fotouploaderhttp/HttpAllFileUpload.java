package com.maky.fotouploaderhttp;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class HttpAllFileUpload {

    public List<String> sendFile(String filePath) {
        // Odošle súbor v jednom kuse na web server. Na výstup vráti info, co poslal php skript

        List<String> vysledok = new ArrayList<String>();

        String url = "http://anketa.westeurope.cloudapp.azure.com/uploader/upload_file.php";
        String charset = "UTF-8";
        File binaryFile = new File(filePath);
        String boundary = Long.toHexString(System.currentTimeMillis()); // Just generate some unique random value.
        String CRLF = "\r\n"; // Line separator required by multipart/form-data.

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        String datum = sdf.format(binaryFile.lastModified());

        try {
            URLConnection connection = new URL(url).openConnection();
            connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "multipart/form-data; boundary=" + boundary);

            OutputStream output = connection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(output, charset), true);
            // Send binary file.
            writer.append("--" + boundary).append(CRLF);
            writer.append("Content-Disposition: form-data; datum=\"" + datum + "\"; name=\"file\"; filename=\"" + binaryFile.getName() + "\"").append(CRLF);
            writer.append("Content-Type: " + URLConnection.guessContentTypeFromName(binaryFile.getName())).append(CRLF);
            writer.append("Content-Transfer-Encoding: binary").append(CRLF);
            writer.append(CRLF).flush();
            Files.copy(binaryFile.toPath(), output);

            /*
            writer.append("--" + boundary + "--").append(CRLF).flush();
            writer.append("Content-Disposition: form-data; name=\"datum\"").append(CRLF);
            writer.append("datum").append(CRLF);
            */

            output.flush(); // Important before continuing with writer!


            writer.append(CRLF).flush(); // CRLF is important! It indicates end of boundary.
            // End of multipart/form-data.
            writer.append("--" + boundary + "--").append(CRLF).flush();


            int responseCode = ((HttpURLConnection) connection).getResponseCode();




            BufferedReader br = null;
            if (responseCode == 200) {
                br = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String strCurrentLine;
                while ((strCurrentLine = br.readLine()) != null) {
                    System.out.println(strCurrentLine);
                    vysledok.add(strCurrentLine);
                }
            } else {
                br = new BufferedReader(new InputStreamReader(((HttpURLConnection) connection).getErrorStream()));
                String strCurrentLine;
                while ((strCurrentLine = br.readLine()) != null) {
                    System.out.println(strCurrentLine);
                    vysledok.add(strCurrentLine);
                }
            }
            System.out.println(responseCode); // Should be 200
            vysledok.add("Respose code: " + responseCode);

        }catch (Exception e) {
            e.printStackTrace();
            vysledok.add("Exception: " + e.getMessage());
        }
        return vysledok;

    }

}


