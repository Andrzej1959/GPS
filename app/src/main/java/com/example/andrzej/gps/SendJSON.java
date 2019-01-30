package com.example.andrzej.gps;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class SendJSON {
    public static void send(String targetUrl, JSONObject jsonObject){

        URL url = null;
        HttpURLConnection urlConnection = null;
        try {
            url = new URL(targetUrl);
            //url = new URL("http://lukan.sytes.net:1880/mapa");
            urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setDoOutput(true);
            urlConnection.setChunkedStreamingMode(0);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("content-type","application/json");

            OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());

            out.write(jsonObject.toString().getBytes());
            out.close();

            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            //readStream(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

}
