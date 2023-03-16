package edu.gradproject.rpagv3;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.firebase.geofire.GeoFireUtils;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQueryBounds;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import edu.gradproject.rpagv3.Models.AlertData;
import edu.gradproject.rpagv3.Models.AlertType;
import edu.gradproject.rpagv3.Providers.AlertProvider;
import edu.gradproject.rpagv3.Providers.AlertTypeProvider;
import edu.gradproject.rpagv3.Utils.LocaleManager;


public class ForegroundService extends Service {

    public static final String ACTION_START_FOREGROUND_SERVICE = "ACTION_START_FOREGROUND_SERVICE";
    public static final String ACTION_STOP_FOREGROUND_SERVICE = "ACTION_STOP_FOREGROUND_SERVICE";

    final static String ACTION_LISTALERTAS_UPDATE = "ACTION_LISTALERTAS_UPDATE";
    final static String LISTALERTAS_UPDATE_TAGNAME = "LISTALERTAS_UPDATE_TAGNAME";

    final static String ACTION_LOCATION_PERMISSIONS_GRANTED = "ACTION_LOCATION_PERMISSIONS_GRANTED";
    final static String ACTION_DATA_UPDATE_REQUEST = "ACTION_DATA_UPDATE_REQUEST";

    public Location location;
    final static String ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE";
    final public static String LATITUDE_TAGNAME = "LATITUDE_TAGNAME";
    final public static String LONGITUDE_TAGNAME = "LONGITUDE_TAGNAME";

    NotificationManager notificationManager;
    //ArrayList<String> alreadyNotifiedAlarms = new ArrayList<>();

    private final static int ONGOING_NOTIFICATION_ID = 195733;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    final static String CHANNEL_ID = "RPAG_CHANNEL_ID";
    final static String SERVICE_PREFERENCES_NAME = "SERVICE_PREFERENCES_NAME";
    final static String LAST_CHECK_TIME_KEY = "LAST_CHECK_TIME_KEY";

    private ArrayList<AlertData> alertDataList;
    private ArrayList<AlertData> pendingAlertsToNotify = new ArrayList<>();

    private FusedLocationProviderClient fusedLocationClient;

//    LocationManager locationManager;
//    MyLocationListener locationListener;
    ArrayList<ListenerRegistration> alertListenerRegistrationList;
    ArrayList<QuerySnapshot> alertQuerySnapshotsList;

    LocationPermissionGrantedReceiver locationPermissionGrantedReceiver;
//    DataUpdateRequestReceiver dataUpdateRequestReceiver;

    AlertProvider mAlertProvider;
    AlertTypeProvider mAlertTypeProvider;

    private final double MIN_DELTA_FOR_UPDATE = 2 * 1000;
    GeoLocation lastUsedLocation;

    private ArrayList<AlertType> alertTypeList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(MainActivity.LOG_TAG, "Foreground Service onCreate()");

        RegisterBroadcastReceivers();
        createNotificationChannel();
        mAlertProvider = new AlertProvider();
        mAlertTypeProvider = new AlertTypeProvider();
        alertListenerRegistrationList = new ArrayList<>();

        LoadAlertTypes();

        startForegroundService();
        Toast.makeText(getApplicationContext(), "Foreground service started.", Toast.LENGTH_LONG).show();

        InitializeLocation();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(MainActivity.LOG_TAG, "Service onStartCommand()");

        if(intent != null)
        {
            String action = intent.getAction();
            switch (action)
            {
                case ACTION_START_FOREGROUND_SERVICE:
                    if (alertDataList != null) {
                        sendAlertList(alertDataList);
                    }
                    break;
                case ACTION_STOP_FOREGROUND_SERVICE:
                    stopForegroundService();
                    //Toast.makeText(getApplicationContext(), "Foreground service is stopped.", Toast.LENGTH_LONG).show();
                    break;
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void LoadAlertTypes() {
        mAlertTypeProvider.getActiveAlertTypes().addOnSuccessListener(queryDocumentSnapshots -> {
            for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
                AlertType type = new AlertType(snap);
                mAlertTypeProvider.getTypeIconFile(this, type.getIcon(), new AlertTypeProvider.getTypeIconFileCallback() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        type.setIconBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                    }

                    @Override
                    public void onFailure() {
                        Log.e(MainActivity.LOG_TAG, "(Service) COULD NOT GET AN ICON FILE FOR TYPE: " + type.getName() + " (" + type.getId() + ")");
                        type.setIconBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.custom_alert_icon_orange));
                    }

                    @Override
                    public void Finally() {
                        alertTypeList.add(type);
                        for (AlertData alert : pendingAlertsToNotify) {
                            if (Objects.equals(alert.getTypeId(), type.getId())) {
                                NotifyAlert(alert);
                                pendingAlertsToNotify.remove(alert);
                            }
                        }
                    }
                });
            }
        });
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

//        IntentFilter dataUpdateRequestFilter = new IntentFilter(ACTION_DATA_UPDATE_REQUEST);
//        dataUpdateRequestFilter.addCategory(Intent.CATEGORY_DEFAULT);
//        dataUpdateRequestReceiver = new DataUpdateRequestReceiver();
//
//        try {
//            registerReceiver(dataUpdateRequestReceiver, dataUpdateRequestFilter);
//        } catch (Exception e) {
//            Log.e(MainActivity.LOG_TAG, Objects.requireNonNull(e.getMessage()));
//        }

    }

    void sendAlertList(ArrayList<AlertData> alertList) {
        Log.i(MainActivity.LOG_TAG, "enviarListaAlertas()");
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_LISTALERTAS_UPDATE);
        broadcastIntent.putExtra(LISTALERTAS_UPDATE_TAGNAME, alertList);
        sendBroadcast(broadcastIntent);
    }

    void InitializeLocation() {
//        locationListener = new MyLocationListener();
//        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permisos no concedidos", Toast.LENGTH_SHORT).show();
        } else {
            Log.d(MainActivity.LOG_TAG, "Requesting location updates");
//            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 100, locationListener);

            //Initializing Fused Location Client
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> LocationUpdate(location));

            LocationRequest locationRequest = LocationRequest.create()
                    .setInterval(30 * 1000)
                    .setFastestInterval(15 * 1000)
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            LocationUpdate(locationResult.getLastLocation());
        }
    };

    void LocationUpdate(Location newLocation) {
        if (newLocation != null) {
            location = newLocation;

            Toast.makeText(getApplicationContext(), "Localizacion actualizada", Toast.LENGTH_SHORT).show();
            Log.d(MainActivity.LOG_TAG, "Location Updated: " + location.getLongitude() + " - " + location.getLatitude());

            GeoLocation currentLocation = new GeoLocation(location.getLatitude(), location.getLongitude());

            if (lastUsedLocation == null || GeoFireUtils.getDistanceBetween(lastUsedLocation, currentLocation) >= MIN_DELTA_FOR_UPDATE) {
                Log.d(MainActivity.LOG_TAG, "Updating listeners");

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

                            if (resultsIndex == -1) { // -1 means this listener has not entered the list yet
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
                                            AlertData alert = new AlertData(doc);
                                            alertDataList.add(alert);
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
        } else {
            Log.d(MainActivity.LOG_TAG, "LocationUpdate: newLocation is null");
        }
    }

    private class LocationPermissionGrantedReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("RPAG-Log", "LocationPermissionGrantedReceiver Activated");
            InitializeLocation();
        }
    }

//    private class DataUpdateRequestReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//            Log.i("RPAG-Log", "DataUpdateRequestReceiver Activated");
//            if (alertDataList != null) {
//                sendAlertList(alertDataList);
//            } else {
//                Log.d(MainActivity.LOG_TAG, "Update requested, but alertDataList is still null");
//            }
//        }
//    }


//    public class MyLocationListener implements LocationListener {
//
//        public Location location;
//        final static String ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE";
//        final static String LATITUDE_TAGNAME = "LATITUDE_TAGNAME";
//        final static String LONGITUDE_TAGNAME = "LONGITUDE_TAGNAME";
//
//        @Override
//        public void onLocationChanged(final Location location) {
//            Toast.makeText(getApplicationContext(), "Localizacion actualizada", Toast.LENGTH_SHORT).show();
//            Log.i("RPAG-Log", "Location Updated: " + location.getLongitude() + " - " + location.getLatitude());
//            this.location = location;
//
//            GeoLocation currentLocation = new GeoLocation(location.getLatitude(), location.getLongitude());
//
//            if (lastUsedLocation == null || GeoFireUtils.getDistanceBetween(lastUsedLocation, currentLocation) >= MIN_DELTA_FOR_UPDATE) {
//                Log.d(MainActivity.LOG_TAG, "Enough movement delta. Updating listeners.");
//
//                for (ListenerRegistration listenerRegistration : alertListenerRegistrationList) {
//                    listenerRegistration.remove();
//                }
//                alertListenerRegistrationList = new ArrayList<>();
//                alertQuerySnapshotsList = new ArrayList<>();
//
//                final GeoLocation center = new GeoLocation(location.getLatitude(), location.getLongitude());
//                final double radiusInM = 5 * 1000;
//
//                final List<GeoQueryBounds> boundsList = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM);
//                for (final GeoQueryBounds bounds : boundsList) {
//                    ListenerRegistration newListener = mAlertProvider.getAlertsByGeohashBounds(bounds).addSnapshotListener(new EventListener<QuerySnapshot>() {
//                        int resultsIndex = -1;
//                        @Override
//                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
//                            Log.d(MainActivity.LOG_TAG, "Response from listener " + boundsList.indexOf(bounds));
//
//                            if (resultsIndex == -1) {
//                                alertQuerySnapshotsList.add(value);
//                                resultsIndex = alertQuerySnapshotsList.indexOf(value);
//                            } else {
//                                alertQuerySnapshotsList.set(resultsIndex, value);
//                            }
//
//                            if (alertQuerySnapshotsList.size() == boundsList.size()) { //This condition would mean all listeners have deposited their results by now
//
//                                alertDataList = new ArrayList<>();
//
//                                for (QuerySnapshot snap : alertQuerySnapshotsList) {
//                                    if (snap != null) {
//                                        for (DocumentSnapshot doc : snap.getDocuments()) {
//                                            AlertData alertData = doc.toObject(AlertData.class);
//
//                                            GeoLocation alertLocation = new GeoLocation(alertData.getLatitude(), alertData.getLongitude());
//
//                                            if (GeoFireUtils.getDistanceBetween(alertLocation, lastUsedLocation) <= radiusInM) {
//                                                ForegroundService.this.alertDataList.add(doc.toObject(AlertData.class));
//                                            }
//                                        }
//                                    }
//                                }
//
//                                sendAlertList(alertDataList);
//                                NotifyNearbyAlerts();
//                            }
//                        }
//                    });
//                    alertListenerRegistrationList.add(newListener);
//                }
//
//                lastUsedLocation = currentLocation;
//            }
//
//            Intent locationUpdateBroadcastIntent = new Intent();
//            locationUpdateBroadcastIntent.setAction(ACTION_LOCATION_UPDATE);
//            locationUpdateBroadcastIntent.putExtra(LATITUDE_TAGNAME, location.getLatitude());
//            locationUpdateBroadcastIntent.putExtra(LONGITUDE_TAGNAME, location.getLongitude());
//            getApplicationContext().sendBroadcast(locationUpdateBroadcastIntent);
//            Log.i(MainActivity.LOG_TAG, "Location Broadcast Sent");
//        }
//
//        @Override
//        public void onStatusChanged(String provider, int status, Bundle extras) {
//
//        }
//
//        @Override
//        public void onProviderEnabled(String provider) {
//            Toast.makeText( getApplicationContext(), "GPS is Enabled", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onProviderDisabled(String provider) {
//            Toast.makeText( getApplicationContext(), "GPS is Disabled.Please enable GPS", Toast.LENGTH_SHORT ).show();
//            location = null;
//        }
//    }

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

        if(alertDataList != null && location != null) {
            for (AlertData alertData : alertDataList) {
                if(alertShouldBeNotified(alertData)){
                    if (AlertTypeProvider.getAlertType(alertData.getTypeId(), alertTypeList) != null) {
                        Log.i(MainActivity.LOG_TAG, "Alert " + alertData.getId() + " Notifying");
                        NotifyAlert(alertData);
                    } else {
                        pendingAlertsToNotify.add(alertData);
                    }
                }
            }
//            for (int i = 0; i < alertDataList.size(); i++) {
//                //if(!alreadyNotifiedAlarms.contains(alertDataList.get(i).getId())){
//                if(alertShouldBeNotified(alertDataList.get(i))){
//                    Log.i(MainActivity.LOG_TAG, "Alert " + alertDataList.get(i).getId() + " Notifying");
//                    NotifyAlert(alertDataList.get(i));
//                    //alreadyNotifiedAlarms.add(alertDataList.get(i).getId());
//                } /*else {
//                    Log.i(MainActivity.LOG_TAG, "Alert " + alertDataList.get(i).getId() + " already notified");
//                }*/
//            }

            SharedPreferences sharedPref = getSharedPreferences(SERVICE_PREFERENCES_NAME, MODE_PRIVATE);
            sharedPref.edit().putString(LAST_CHECK_TIME_KEY, Long.toString(new Date().getTime())).apply();
        }
    }

    private boolean alertShouldBeNotified(AlertData alertData) {
        String lastCheck = getSharedPreferences(SERVICE_PREFERENCES_NAME, MODE_PRIVATE).getString(LAST_CHECK_TIME_KEY, Long.toString(new Date().getTime() - 3600 * 1000));
        return alertData.getDate().getTime() >= Long.parseLong(lastCheck);
    }

    private void NotifyAlert(AlertData alert) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setAction(MainActivity.ACTION_SHOW_ALERT);
        intent.putExtra(MainActivity.EXTRA_SHOW_ALERT_ID_TAG, alert.getId());
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        AlertType type = AlertTypeProvider.getAlertType(alert.getTypeId(), alertTypeList);
        if (type != null) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_foreground_notif_icon)
                    .setContentTitle("Alerta cercana")
                    .setContentText("Alerta " + type.getName() + " cercana")
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);
            notificationManager.notify(getNewNotificationID(), builder.build());
        }
    }

    int getNewNotificationID() {
        return new Random().nextInt();
    }

    private void startForegroundService() {
        Log.d(MainActivity.LOG_TAG, "Start foreground service.");

        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);

        Intent stopIntent = new Intent(this, ForegroundService.class)
                .setAction(ACTION_STOP_FOREGROUND_SERVICE);
        PendingIntent closePendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle(getString(R.string.app_name_long))
                .setContentText(getString(R.string.app_running))
                .setSmallIcon(R.drawable.ic_foreground_notif_icon)
                .setContentIntent(pendingIntent)
                .addAction(new NotificationCompat.Action(null, getString(R.string.quit), closePendingIntent));

        Notification notification = builder.build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);
        Toast.makeText(getApplicationContext(), "Foreground service started.", Toast.LENGTH_LONG).show();
    }

    private void stopForegroundService() {
        Log.d(MainActivity.LOG_TAG, "Stop foreground service.");
        stopForeground(true);
        fusedLocationClient.removeLocationUpdates(locationCallback);
        stopSelf();
        System.exit(0);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setContextLocale(newBase));
    }
}