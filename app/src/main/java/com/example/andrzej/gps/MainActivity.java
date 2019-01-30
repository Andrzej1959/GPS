package com.example.andrzej.gps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 0;

    public TextView latTextView;
    public TextView lonTextView;
    public TextView woeidTextView;
    public TextView cityTextView;

    public WebView weatherWebView;

    private LocationManager locationManager;
    private LocationProvider locationProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latTextView = findViewById(R.id.latTextView);
        lonTextView = findViewById(R.id.lonTextView);
        woeidTextView = findViewById(R.id.woeidTextView);
        cityTextView = findViewById(R.id.cityTextView_);

        weatherWebView = findViewById(R.id.weatherWebView);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    accessLocation();
                }
            }
        }
    }
    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
            // MY_PERMISSIONS_REQUEST_LOCATION is an
            // app-defined int constant. The callback method gets the
            // result of the request.
        } else{
            accessLocation();
        }
    }

    private void accessLocation(){
        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        this.locationProvider = this.locationManager.getProvider(LocationManager.GPS_PROVIDER);
        if (locationProvider != null) {
            Toast.makeText(this, "Location listener registered!", Toast.LENGTH_SHORT).show();
            try {
                this.locationManager.requestLocationUpdates(locationProvider.getName(), 10000, 10,
                        this.locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this,
                    "Location Provider is not avilable at the moment!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationProvider != null) {
            Toast.makeText(this, "Location listener unregistered!", Toast.LENGTH_SHORT).show();
            try {
                this.locationManager.removeUpdates(this.locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Location Provider is not avilable at the moment!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            final double lat = (location.getLatitude());
            final double lon = location.getLongitude();
            final double time = location.getTime();

            DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSSXXX");
            Date date = new Date(location.getTime());
            final String formatted = format.format(date);


            final JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("name", "Śmiały");
                jsonObject.put("opis", "Mały podróżnik");
                jsonObject.put("lat", lat);
                jsonObject.put("lon", lon);
                jsonObject.put("time", formatted);
                jsonObject.put("icon", "fa-truck");
                jsonObject.put("iconColor", "DarkGreen");
                jsonObject.put("color", "DarkGreen");

            } catch (JSONException e) {
                e.printStackTrace();
            }

            new Thread(new Runnable() {
                @Override
                public void run() {

                   // SendJSON sendJSON = new SendJSON();
                   // sendJSON.send(jsonObject);

                    SendJSON.send("http://lukan.sytes.net:1880/mapa",jsonObject);
                    /*
                    URL url = null;
                    HttpURLConnection urlConnection = null;
                    try {
                        url = new URL("http://lukan.sytes.net:1880/mapa");
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
                    */
                }
            }).start();


            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateWeather(lat,lon);
                }
            }).start();
        };

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    private void updateWeather(double lat, double lon){
        String weather = getContentFromUrl(String.format(OPENWEATHER_WEATHER_QUERY, lat,lon) );
        Message m = myHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("lat", String.valueOf(lat));
        b.putString("lon", String.valueOf(lon));
        b.putString("web", weather);
        m.setData(b);
        myHandler.sendMessage(m);
    }

    public static String OPENWEATHER_WEATHER_QUERY = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&mode=html&appid=4526d487f12ef78b82b7a7d113faea64";
    // usage String.format(OPENWEATHER_WEATHER_QUERY, lat,lon)

    public String getContentFromUrl(String addr) {
        String content = null;

        Log.v("[GEO WEATHER ACTIVITY]", addr);
        HttpURLConnection urlConnection = null;
        URL url = null;
        try {
            url = new URL(addr);
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = in.readLine()) != null)
            {
                stringBuilder.append(line + "\n");
            }
            content = stringBuilder.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(urlConnection!= null) urlConnection.disconnect();
        }
        return content;
    }

/*
    Handler myHandler = new Handler(){
        public void handleMessage(Message msg) {
            String lat = msg.getData().getString("lat");
            String lon = msg.getData().getString("lon");
            String woeid = msg.getData().getString("woeid");
            String web = msg.getData().getString("web");
            String city = msg.getData().getString("city");
//referencje pobrane wcześniej w metodzie onCreate(...)
            latTextView.setText(lat);
            lonTextView.setText(lon);
            woeidTextView.setText(woeid);
            cityTextView.setText(city);
            weatherWebView.loadDataWithBaseURL(null, web, "text/html", "utf-8", null);
        }
    };
    */

    private static class MyHandler extends Handler {
        private final WeakReference<MainActivity> mActivity;
        MyHandler(MainActivity activity) {
            mActivity = new WeakReference<MainActivity>(activity);
        }
        @Override
        public void handleMessage(Message msg) {
            MainActivity activity = mActivity.get();
            String lat = msg.getData().getString("lat");
            String lon = msg.getData().getString("lon");
            String woeid = msg.getData().getString("woeid");
            String web = msg.getData().getString("web");
            String city = msg.getData().getString("city");
            //referencje pobrane wcześniej w metodzie onCreate(...)
            activity.latTextView.setText("lat: " + lat);
            activity.lonTextView.setText("lon: " + lon);
            activity.woeidTextView.setText("woeid: " + woeid);
            activity.cityTextView.setText("city: " + city);
            activity.weatherWebView.loadDataWithBaseURL(null, web, "text/html", "utf-8", null);

            //activity.weatherWebView.loadUrl(String.format(OPENWEATHER_WEATHER_QUERY, lat,lon));
        }
    }
    Handler myHandler = new MyHandler(this);

}
