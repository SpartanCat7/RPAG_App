package com.example.rpagv2;

import android.Manifest;
import android.app.IntentService;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BackgroundService extends Service {

    final static String ACTION_PACKDATOS_RESPONSE = "ACTION_PACKDATOS_RESPONSE";
    final static String PACKDATOS_UPDATE_TAGNAME = "PACKDATOS_UPDATE_TAGNAME";

    final static String ACTION_UPDATE_REQUEST = "ACTION_UPDATE_REQUEST";
    final static String ACTION_LOCATION_PERMISSIONS_GRANTED = "ACTION_UPDATE_REQUEST";

    NotificationManager notificationManager;
    ArrayList<Integer> alreadyNotifiedAlarms = new ArrayList<>();

    ArrayList<ClaseAlerta> listClasesAlertas = new ArrayList<>();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    String IP_SERVIDOR = "192.168.137.1";
    int PORT_SERVIDOR = 6809;
    final static String CHANNEL_ID = "RPAG_CHANNEL_ID";

    PackDatos packDatos;

    LocationManager locationManager;
    MyLocationListener locationListener;

    UpdateRequestReceiver updateRequestReceiver;
    LocationPermissionGrantedReceiver locationPermissionGrantedReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("RPAG-Log", "Background Service Initialized");

        nextNotifID = 1000;
        IP_SERVIDOR = MainActivity.IP_SERVIDOR;
        PORT_SERVIDOR = MainActivity.PORT_SERVIDOR;
        Log.i("RPAG-Log", "Server data updated: " + IP_SERVIDOR + ":" + PORT_SERVIDOR);
        RegisterBroadcastReceivers();
        createNotificationChannel();
        InitializeLocation();
        actualizacion.run();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            listClasesAlertas = (ArrayList<ClaseAlerta>)intent.getSerializableExtra(MainActivity.CLASS_LIST_TAG);
        } catch (ClassCastException e) {
            Log.e("RPAG-Log", "ClassCastException: " + e.getMessage());
        } catch (NullPointerException e) {
            Log.e("RPAG-Log", "NullPointerException: " + e.getMessage());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    void RegisterBroadcastReceivers() {
        IntentFilter updateRequestFilter = new IntentFilter(ACTION_UPDATE_REQUEST);
        updateRequestFilter.addCategory(Intent.CATEGORY_DEFAULT);
        updateRequestReceiver = new UpdateRequestReceiver();

        IntentFilter locationPermissionGrantedFilter = new IntentFilter(ACTION_LOCATION_PERMISSIONS_GRANTED);
        locationPermissionGrantedFilter.addCategory(Intent.CATEGORY_DEFAULT);
        locationPermissionGrantedReceiver = new LocationPermissionGrantedReceiver();



        try {
            registerReceiver(updateRequestReceiver, updateRequestFilter);
            registerReceiver(locationPermissionGrantedReceiver, locationPermissionGrantedFilter);
        } catch (Exception e) {
            Log.e("RPAG-Log", Objects.requireNonNull(e.getMessage()));
        }

    }

    Handler actualizadorAlertas = new Handler();
    Runnable actualizacion = new Runnable() {
        @Override
        public void run() {
            actualizadorAlertas.postDelayed(this, 60000);
            actualizarListaAlertas();
            NotifyNearbyAlerts(.5,.5);
            enviarListaAlertas(packDatos);
        }
    };

    void enviarListaAlertas(PackDatos pack) {
        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(ACTION_PACKDATOS_RESPONSE);
        broadcastIntent.putExtra(PACKDATOS_UPDATE_TAGNAME, pack);
        sendBroadcast(broadcastIntent);
    }

    void actualizarListaAlertas() {
        Log.i("RPAG-Log", "actualizarListaAlertas()");

        Thread actualizarListas = new Thread() {

            InputStream in = null;
            OutputStream out = null;
            ObjectInputStream objectInputStream;
            ObjectOutputStream objectOutputStream;
            PrintWriter printWriter;

            @Override
            public void run() {
                Socket socket = null;
                PackDatos packDatosServer;
                try {
                    Log.i("RPAG-Log", "Actualizando Alertas");

                    socket = new Socket(IP_SERVIDOR, PORT_SERVIDOR);
                    Log.i("RPAG-Log", "Conexion establecida");
                    in = socket.getInputStream();
                    out = socket.getOutputStream();

                    printWriter = new PrintWriter(socket.getOutputStream(), true);

                    printWriter.println("Actualizar");
                    Log.i("RPAG-Log", "Mensaje Enviado");

                    objectOutputStream = new ObjectOutputStream(out);
                    Log.i("RPAG-Log", "objectOutputStream establecido");
                    objectInputStream = new ObjectInputStream(in);
                    Log.i("RPAG-Log", "objectInputStream establecido");

                    Log.i("RPAG-Log", "Esperando Respuesta...");

                    packDatosServer = (PackDatos) objectInputStream.readObject();
                    Log.i("RPAG-Log", "Objeto Recibido...");

                    if (packDatosServer != null) {
                        packDatos = packDatosServer;
                        Log.i("RPAG-Log", "----- Lista Recuperada -----");
                        Log.i("RPAG-Log", "Alertas: " + packDatos.listaDatosAlertas.size());
                        Log.i("RPAG-Log", "Confirmaciones: " + packDatos.listaConfirmaciones.size());
                        Log.i("RPAG-Log", "Reportes: " + packDatos.listaReportes.size());
                        Log.i("RPAG-Log", "Comentarios: " + packDatos.listaComentarios.size());
                        Log.i("RPAG-Log", "Imagenes: " + packDatos.listaImagenes.size());
                    }


                } catch (UnknownHostException e) {
                    Log.e("RPAG-Log", "Unknown host: " + IP_SERVIDOR);
                } catch (ConnectException ce) {
                    Log.e("RPAG-Log", "ConnectException: " + ce.getMessage());
                } catch (IOException | ClassNotFoundException ex) {
                    Logger.getLogger(BackgroundService.class.getName()).log(Level.SEVERE, ex.getMessage(), ex);
                } finally {
                    try {
                        if (socket != null) {
                            if (!socket.isClosed()) {
                                printWriter.close();
                                objectInputStream.close();
                                objectOutputStream.close();
                                in.close();
                                out.close();
                                socket.close();
                            }
                        }
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    } catch (Exception e) {
                        Log.e("RPAG-Log", "Exception: " + e.getMessage());
                    }
                }
            }
        };

        actualizarListas.start();
        try {
            actualizarListas.join(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }



    private class UpdateRequestReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("RPAG-Log", "UpdateRequestReceiver()");
            actualizarListaAlertas();
            packDatos.latitude = locationListener.location.getLatitude();
            packDatos.longitude = locationListener.location.getLongitude();
            enviarListaAlertas(packDatos);
        }
    }

    void InitializeLocation() {
        locationListener = new MyLocationListener();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getApplicationContext(), "Permisos no concedidos", Toast.LENGTH_SHORT).show();
            return;
        }
        else {
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

    void NotifyNearbyAlerts(double maxLatitudeDelta, double maxLongitudeDelta) {
        Log.i("RPAG-Log", "NotifyNearbyAlerts()");

        if(packDatos != null && locationListener.location != null) {
            double latitude = locationListener.location.getLatitude();
            double longitude = locationListener.location.getLongitude();

            for (int i = 0; i < packDatos.listaDatosAlertas.size(); i++) {
                if(!alreadyNotifiedAlarms.contains(packDatos.listaDatosAlertas.get(i).id)){
                    if(isAlertNear(latitude, longitude, packDatos.listaDatosAlertas.get(i), maxLatitudeDelta, maxLongitudeDelta)){
                        Log.i("RPAG-Log", "Alert " + packDatos.listaDatosAlertas.get(i).id + " Notifying");
                        NotifyAlert(packDatos.listaDatosAlertas.get(i));
                        alreadyNotifiedAlarms.add(packDatos.listaDatosAlertas.get(i).id);
                    } else {
                        Log.i("RPAG-Log", "Alert " + packDatos.listaDatosAlertas.get(i).id + "not near");
                    }
                } else {
                    Log.i("RPAG-Log", "Alert " + packDatos.listaDatosAlertas.get(i).id + " already notified");
                }
            }
        }
    }


    private void NotifyAlert(DatosAlerta alerta) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(getClase(alerta.clase_id).icon)
                .setContentTitle("Alerta cercana")
                .setContentText("Una alerta de " + getClase(alerta.clase_id).name + " ha sido enviada cerca de usted")
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(getNewNotificationID(), builder.build());
    }

    ClaseAlerta getClase(int clase_id) {
        for (int i = 0; i < listClasesAlertas.size(); i++) {
            if (clase_id == listClasesAlertas.get(i).id) {
                return listClasesAlertas.get(i);
            }
        }
        return null;
    }

    boolean isAlertNear(double latitude, double longitude, DatosAlerta alerta , double maxLatitudeDelta, double maxLongitudeDelta) {
        if (alerta.latitud < latitude + maxLatitudeDelta && alerta.latitud > latitude - maxLatitudeDelta){
            if (alerta.longitud < longitude + maxLongitudeDelta && alerta.longitud > longitude - maxLongitudeDelta) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    int nextNotifID = 1000;
    int getNewNotificationID() {
        int res = nextNotifID;
        nextNotifID += 1;
        return res;
    }
}