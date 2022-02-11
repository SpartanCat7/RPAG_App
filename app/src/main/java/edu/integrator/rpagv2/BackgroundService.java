package edu.integrator.rpagv2;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import edu.integrator.rpagv2.Models.AlertData;
import edu.integrator.rpagv2.Providers.AlertProvider;


public class BackgroundService extends Service {

    final static String ACTION_LISTALERTAS_UPDATE = "ACTION_LISTALERTAS_UPDATE";
    final static String LISTALERTAS_UPDATE_TAGNAME = "LISTALERTAS_UPDATE_TAGNAME";

    final static String ACTION_LOCATION_PERMISSIONS_GRANTED = "ACTION_UPDATE_REQUEST";

    NotificationManager notificationManager;
    ArrayList<String> alreadyNotifiedAlarms = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    final static String CHANNEL_ID = "RPAG_CHANNEL_ID";
    final static String LAST_CHECK_TIME_KEY = "LAST_CHECK_TIME_KEY";

    private ArrayList<AlertData> alertDataList;

    LocationManager locationManager;
    MyLocationListener locationListener;
    ArrayList<ListenerRegistration> alertListenerRegistrationList;
    ArrayList<QuerySnapshot> alertQuerySnapshotsList;

    LocationPermissionGrantedReceiver locationPermissionGrantedReceiver;

    AlertProvider mAlertProvider;

    private final double MIN_DELTA_FOR_UPDATE = 2 * 1000;
    GeoLocation lastUsedLocation;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(MainActivity.LOG_TAG, "Background Service Initialized");

        nextNotifID = 1000;
        RegisterBroadcastReceivers();
        createNotificationChannel();
        mAlertProvider = new AlertProvider();
        alertListenerRegistrationList = new ArrayList<>();
        InitializeLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    void RegisterBroadcastReceivers() {

        IntentFilter locationPermissionGrantedFilter = new IntentFilter(ACTION_LOCATION_PERMISSIONS_GRANTED);
        locationPermissionGrantedFilter.addCategory(Intent.CATEGORY_DEFAULT);
        locationPermissionGrantedReceiver = new LocationPermissionGrantedReceiver();

        try {
            registerReceiver(locationPermissionGrantedReceiver, locationPermissionGrantedFilter);
        } catch (Exception e) {
            Log.e(MainActivity.LOG_TAG, Objects.requireNonNull(e.getMessage()));
        }

    }

    void sendAlertList(ArrayList<AlertData> alertList) {
        Log.i(MainActivity.LOG_TAG, "enviarListaAlertas()");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_LISTALERTAS_UPDATE);
        broadcastIntent.putExtra(LISTALERTAS_UPDATE_TAGNAME, alertList);
        sendBroadcast(broadcastIntent);
    }

    void InitializeLocation() {
        locationListener = new MyLocationListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permisos no concedidos", Toast.LENGTH_SHORT).show();
        } else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);
        }
    }

    private class LocationPermissionGrantedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("RPAG-Log", "LocationPermissionGrantedReceiver Activated");
            InitializeLocation();
        }
    }

    public class MyLocationListener implements LocationListener {

        public Location location;
        final static String ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE";
        final static String LATITUDE_TAGNAME = "LATITUDE_TAGNAME";
        final static String LONGITUDE_TAGNAME = "LONGITUDE_TAGNAME";

        @Override
        public void onLocationChanged(final Location location) {
            Toast.makeText(getApplicationContext(), "Localizacion actualizada", Toast.LENGTH_SHORT).show();
            Log.i("RPAG-Log", "Location Updated: " + location.getLongitude() + " - " + location.getLatitude());
            this.location = location;

            GeoLocation currentLocation = new GeoLocation(location.getLatitude(), location.getLongitude());

            if (lastUsedLocation == null || GeoFireUtils.getDistanceBetween(lastUsedLocation, currentLocation) >= MIN_DELTA_FOR_UPDATE) {
                Log.d(MainActivity.LOG_TAG, "Enough movement delta. Updating listeners.");

                for (ListenerRegistration listenerRegistration : alertListenerRegistrationList) {
                    listenerRegistration.remove();
                }
                alertListenerRegistrationList = new ArrayList<>();
                alertQuerySnapshotsList = new ArrayList<>();

                final GeoLocation center = new GeoLocation(location.getLatitude(), location.getLongitude());
                final double radiusInM = 5 * 1000;

                final List<GeoQueryBounds> boundsList = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
                for (final GeoQueryBounds bounds : boundsList) {
                    ListenerRegistration newListener = mAlertProvider.getAlertsByGeohashBounds(bounds).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        int resultsIndex = -1;
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                            Log.d(MainActivity.LOG_TAG, "Response from listener " + boundsList.indexOf(bounds));

                            if (resultsIndex == -1) {
                                alertQuerySnapshotsList.add(value);
                                resultsIndex = alertQuerySnapshotsList.indexOf(value);
                            } else {
                                alertQuerySnapshotsList.set(resultsIndex, value);
                            }

                            if (alertQuerySnapshotsList.size() == boundsList.size()) { //This condition would mean all listeners have deposited their results by now

                                alertDataList = new ArrayList<>();

                                for (QuerySnapshot snap : alertQuerySnapshotsList) {
                                    if (snap != null) {
                                        for (DocumentSnapshot doc : snap.getDocuments()) {
                                            AlertData alertData = doc.toObject(AlertData.class);

                                            GeoLocation alertLocation = new GeoLocation(alertData.getLatitude(), alertData.getLongitude());

                                            if (GeoFireUtils.getDistanceBetween(alertLocation, lastUsedLocation) <= radiusInM) {
                                                BackgroundService.this.alertDataList.add(doc.toObject(AlertData.class));
                                            }
                                        }
                                    }
                                }

                                sendAlertList(alertDataList);
                                NotifyNearbyAlerts();
                            }
                        }
                    });
                    alertListenerRegistrationList.add(newListener);
                }

                lastUsedLocation = currentLocation;
            }

            Intent locationUpdateBroadcastIntent = new Intent();
            locationUpdateBroadcastIntent.setAction(ACTION_LOCATION_UPDATE);
            locationUpdateBroadcastIntent.putExtra(LATITUDE_TAGNAME, location.getLatitude());
            locationUpdateBroadcastIntent.putExtra(LONGITUDE_TAGNAME, location.getLongitude());
            getApplicationContext().sendBroadcast(locationUpdateBroadcastIntent);
            Log.i(MainActivity.LOG_TAG, "Location Broadcast Sent");
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {
            Toast.makeText( getApplicationContext(), "GPS is Enabled", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderDisabled(String provider) {
            Toast.makeText( getApplicationContext(), "GPS is Disabled.Please enable GPS", Toast.LENGTH_SHORT ).show();
            location = null;
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "RPAG Notif";
            String description = "RPAG Notifications Channel";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    void NotifyNearbyAlerts() {
        Log.i(MainActivity.LOG_TAG, "NotifyNearbyAlerts()");

        if(alertDataList != null && locationListener.location != null) {
            for (int i = 0; i < alertDataList.size(); i++) {
                //if(!alreadyNotifiedAlarms.contains(alertDataList.get(i).getId())){
                if(alertShouldBeNotified(alertDataList.get(i))){
                    Log.i(MainActivity.LOG_TAG, "Alert " + alertDataList.get(i).getId() + " Notifying");
                    NotifyAlert(alertDataList.get(i));
                    alreadyNotifiedAlarms.add(alertDataList.get(i).getId());
                } else {
                    Log.i(MainActivity.LOG_TAG, "Alert " + alertDataList.get(i).getId() + " already notified");
                }
            }

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            sharedPref.edit().putString(LAST_CHECK_TIME_KEY, Long.toString(new Date().getTime())).apply();

        }
    }

    private boolean alertShouldBeNotified(AlertData alertData) {
        String lastCheck = PreferenceManager.getDefaultSharedPreferences(this).getString(LAST_CHECK_TIME_KEY, Long.toString(new Date().getTime() - 3600 * 1000));
        if (alertData.getDate().getTime() >= Long.parseLong(lastCheck))
            return true;

        return false;
    }

    private void NotifyAlert(AlertData alerta) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(MainActivity.getClass(alerta.getClassId()).icon)
                .setContentTitle("Alerta cercana")
                .setContentText("Una alerta de " + getString(MainActivity.getClass(alerta.getClassId()).name_string_ID) + " ha sido enviada cerca de usted")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(getNewNotificationID(), builder.build());
    }

    int nextNotifID = 1000;
    int getNewNotificationID() {
        int res = nextNotifID;
        nextNotifID += 1;
        return res;
    }
}