package com.example.michael.geofences;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

public class MockManager {

    private Context context;
    private GoogleApiClient apiClient;
    private boolean mockMode;

    public MockManager(Context context, GoogleApiClient apiClient) {
        this.apiClient = apiClient;
        this.context = context;
    }

    public void setLocation(double latitude, double longitude) {
        if (!mockMode) {
            LocationServices.FusedLocationApi.setMockMode(apiClient, true);
            mockMode = true;
        }

        Location mockLocation = new Location("MockProvider");
        mockLocation.setLatitude(latitude);
        mockLocation.setLongitude(longitude);
        mockLocation.setAccuracy(1.0f);
        mockLocation.setTime(System.currentTimeMillis());
        mockLocation.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        if(apiClient.isConnected()) {
            LocationServices.FusedLocationApi.setMockLocation(apiClient, mockLocation);
        }else{
            Toast.makeText(context, "Google Services api disconnected", Toast.LENGTH_SHORT).show();
            mockMode = false;
        }
    }

    public void startThread(final double latitude, final double longitude, final double amount) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                double currentLatitude = latitude;
                double currentLongitude = longitude;

                while (true) {
                    setLocation(currentLatitude, currentLongitude);
                    currentLatitude += amount;
                    currentLongitude += amount;
                    SystemClock.sleep(500);
                }
            }
        }).start();
    }

    public void startThread(final double[][] locations) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    double[] location = locations[i];
                    setLocation(location[0], location[1]);
                    Log.i("App", String.format("Location: (%.4f, %.4f)", location[0], location[1]));
                    SystemClock.sleep(1000);
                    i = (i + 1) % locations.length;
                }
            }
        }).start();
    }
}
