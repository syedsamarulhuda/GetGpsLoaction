package com.example.samar.getmylocation;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity implements LocationListener {

    TextView tvLat, tvLong, tvAddress, tvResponse;
    protected LocationManager locationManager;

    String url = "https://node.flyrobeapp.com:8002";

    final int REFRESHTIME = 5 * 1000; // Minimum time interval for update in seconds, i.e. 5 seconds.
    final float MINDISTANCE = 10; // Minimum distance change for update in meters, i.e. 10 meters.


    private final int REQUEST_EXTERNAL_STORAGE = 1;
    private String[] PERMISSIONS_STORAGE = {
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };


    private Socket mSocket;

    {
        try {
            mSocket = IO.socket(url);
        } catch (URISyntaxException e) {
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvAddress = (TextView) findViewById(R.id.tv_address);
        tvResponse = (TextView) findViewById(R.id.tv_response);

        Log.d("#Loading", "activity....");
        mSocket.connect();

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        int permissionFine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        int permissionCoarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION);

        if (permissionFine != PackageManager.PERMISSION_GRANTED && permissionCoarse != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    MainActivity.this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, REFRESHTIME, MINDISTANCE, this);


    }


    private void getAddress(Location location) {
        Geocoder geocoder;
        List<Address> addresses = null;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }

        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName();

        String fullAddress = address + ", " + city + ", " + state + ", " + country + ", " + postalCode + ", " + knownName;
        tvAddress.setText(fullAddress);

        Log.d("#", "=======================ADDRESS========================");
        Log.d("#Latitude-value", location.getLatitude() + "");
        Log.d("#Longitude-value", location.getLongitude() + "");
        Log.d("#Address", address);
        Log.d("#City", city);
        Log.d("#State", state);
        Log.d("#Country", country);
        Log.d("#Postal Code", postalCode);
        Log.d("#knownName", knownName);

        Emitter.Listener onNewMessage = new Emitter.Listener() {
            @Override
            public void call(final Object... args) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        JSONObject data = (JSONObject) args[0];
                        Log.d("##RESPONSE-SOCKET", data.toString());


                    }
                });
            }


        };


    }

    private void sendLatLong(String longt, String lat) {
        JSONObject params = new JSONObject();

        try {
            params.put("biker_id", "32");
            params.put("first_name", "pritam");
            params.put("last_name", "jana");
            params.put("warehouse_id", "1");
            params.put("address", "test");
            params.put("long", longt);
            params.put("lat", lat);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        // mSocket.send(params);
        mSocket.emit("start_tracking", params);
    }


    @Override
    public void onLocationChanged(Location location) {
        tvLat = (TextView) findViewById(R.id.tv_lat);
        tvLong = (TextView) findViewById(R.id.tv_long);
        tvLat.setText("Latitude:" + location.getLatitude());
        tvLong.setText(" Longitude:" + location.getLongitude());

        getAddress(location);

        sendLatLong(location.getLongitude() + "", location.getLatitude() + "");


    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d("#Latitude", "disable");
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d("#Latitude", "enable");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d("#Latitude", "status");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mSocket.disconnect();
        //mSocket.off("new message", onNewMessage);
    }
}
