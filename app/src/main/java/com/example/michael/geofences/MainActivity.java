package com.example.michael.geofences;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String ACTION_GEOFENCE = "br.com.softblue.android.action.GEOFENCE";

    private GoogleApiClient apiClient;
    private MockManager mockManager;
    private TextView txtMsg;
    private PendingIntent pi;
    private GeofenceReceiver receiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        txtMsg = (TextView) findViewById(R.id.txt_msg);

        apiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        mockManager = new MockManager(this, apiClient);
        receiver = new GeofenceReceiver();
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerReceiver(receiver, new IntentFilter(ACTION_GEOFENCE));
        apiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        apiClient.disconnect();
        unregisterReceiver(receiver);
    }

    @Override
    public void onConnected(Bundle bundle) {

        Geofence.Builder builder = new Geofence.Builder();
        builder.setExpirationDuration(Geofence.NEVER_EXPIRE);
        builder.setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT);

        builder.setCircularRegion(48.85760, 2.29597, 300);
        builder.setRequestId(getString(R.string.eiffel_tower));
        Geofence geofence1 = builder.build();

        builder.setCircularRegion(40.68997, -74.04543, 300);
        builder.setRequestId(getString(R.string.liberty_statue));
        Geofence geofence2 = builder.build();

        List<Geofence> geofences = new ArrayList<>();
        geofences.add(geofence1);
        geofences.add(geofence2);

        Intent intent = new Intent(ACTION_GEOFENCE);
        pi = PendingIntent.getBroadcast(getApplicationContext(), 0, intent, 0);

        PendingResult<Status> status = LocationServices.GeofencingApi.addGeofences(apiClient, geofences, pi);
        status.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.geofences_registred), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.geofences_not_registred) + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }


    @Override
    public void onConnectionSuspended(int i) {
    }

    @Override
    public void onConnectionFailed(ConnectionResult connResult) {
    }

    public void startLocation(View view) {
        double[][] locations = { { 48.85760, 2.29597 }, { 48.95873, 2.30948 }, { 48.96873, 2.31098 }, { 40.68997, -74.04543 }, { 40.66432, -74.02094 }, { 40.65434, -74.01098 } };
        mockManager.startThread(locations);
    }

    public void removeGeofences(View view) {
        PendingResult<Status> status = LocationServices.GeofencingApi.removeGeofences(apiClient, pi);
        status.setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                if (status.isSuccess()) {
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.geogences_removed), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, MainActivity.this.getString(R.string.geofences_not_removed) + status.getStatusMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private class GeofenceReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            GeofencingEvent event = GeofencingEvent.fromIntent(intent);

            List<Geofence> geofences = event.getTriggeringGeofences();
            Geofence geofence = geofences.get(0);

            int transition = event.getGeofenceTransition();

            String text;

            if (transition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                text = MainActivity.this.getString(R.string.entering) + geofence.getRequestId();
            } else {
                text = MainActivity.this.getString(R.string.leaving) + geofence.getRequestId();
            }

            txtMsg.setText(text);
        }
    }
}
