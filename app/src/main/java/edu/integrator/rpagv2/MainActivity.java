package edu.integrator.rpagv2;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.UploadTask;
import com.integrator.rpagv2.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.OnCameraTrackingChangedListener;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.location.DefaultLocationProvider;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import edu.integrator.rpagv2.Models.AlertData;
import edu.integrator.rpagv2.Models.ImageData;
import edu.integrator.rpagv2.Models.UIAlert;
import edu.integrator.rpagv2.Models.AlertClass;
import edu.integrator.rpagv2.Models.Comment;
import edu.integrator.rpagv2.Providers.AlertProvider;
import edu.integrator.rpagv2.Providers.CommentProvider;
import edu.integrator.rpagv2.Providers.ImageProvider;
import edu.integrator.rpagv2.Providers.UserProvider;

import static android.content.pm.PackageManager.GET_META_DATA;

public class MainActivity extends AppCompatActivity implements
        AlertMenuDialog.AlertMenuDialogInterface,
        LoginDialog.LoginDialogInterface,
        RegisterDialog.RegisterDialogInterface,
        AlertOptionsDialog.AlertOptionsDialogInterface {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private AlertProvider mAlertProvider;
    private CommentProvider mCommentProvider;
    private ImageProvider mImageProvider;
    private UserProvider mUserProvider;


    MapView mapView;
    FloatingActionButton btnAlertMenu;
    FloatingActionButton btnPanToDeviceLocation;
    Menu optionsMenu;

    public static final String LOG_TAG = "RPAG-Log";

    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_PERMISSION_CODE = 100;

    //final static String CLASS_LIST_TAG = "CLASS_LIST_TAG";
    final static String ALERT_MENU_DIALOG_TAG = "ALERT_MENU_DIALOG_TAG";
    final static String LOGIN_DIALOG_TAG = "LOGIN_DIALOG_TAG";
    final static String REGISTER_DIALOG_TAG = "REGISTER_DIALOG_TAG";
    final static String ALERT_OPTIONS_DIALOG_TAG = "ALERT_OPTIONS_DIALOG_TAG";

    final public static String ACTION_SHOW_ALERT = "ACTION_SHOW_ALERT";
    final public static String EXTRA_SHOW_ALERT_ID_TAG = "EXTRA_SHOW_ALERT_ID_TAG";
    private boolean actionShowAlertStarted = false;

    /*
    AlertClass claseAccidente = new AlertClass(
            1,  "Accidente", R.drawable.frontal_crash, "icon_accidente");
    AlertClass claseIncendio = new AlertClass(
            2,  "Incendio", R.drawable.fire, "icon_incendio");
    AlertClass claseHerido = new AlertClass(
            3,  "Persona Herida", R.drawable.band_aid, "icon_herido");
    AlertClass claseBloqueo = new AlertClass(
            4,  "Bloqueo", R.drawable.road_blockade, "icon_bloqueo");
    AlertClass claseCongestionamiento = new AlertClass(
            5,  "Congestionamiento", R.drawable.traffic_jam, "icon_congestionamiento");
    AlertClass claseMarchas = new AlertClass(
            6,  "Marchas", R.drawable.parade, "icon_marchas");
    AlertClass claseCalleDanada = new AlertClass(
            7,  "Calle Dañada", R.drawable.road, "icon_calle");
    AlertClass claseCorte = new AlertClass(
            8,  "Corte Electrico", R.drawable.flash, "icon_corte");
*/
    ArrayList<UIAlert> uiAlertList = new ArrayList<>();

    //final static String MAPBOX_STYLE = "mapbox://styles/spartancat7/ckammphtj1rao1imlhr7sjtgy";
    String myLocationSymbolName = "MyLocationIcon";
    int myLocationSymbolDrawable = R.drawable.pin;
    Symbol myLocationSymbol;

    final static int CUSTOM_CLASS_ID = 0;
    final static int ACCIDENTE_CLASS_ID = 1;
    final static int INCENDIO_CLASS_ID = 2;
    final static int HERIDO_CLASS_ID = 3;
    final static int BLOQUEO_CLASS_ID = 4;
    final static int CONGESTIONAMIENTO_CLASS_ID = 5;
    final static int MARCHAS_CLASS_ID = 6;
    final static int CALLE_DANADA_CLASS_ID = 7;
    final static int CORTE_ELECTRICO_CLASS_ID = 8;

    final static AlertClass[] listClasesAlertas = {
            new AlertClass(CUSTOM_CLASS_ID, R.drawable.custom_alert_icon_orange, "icon_custom_alert", R.string.custom_alert, null),
            new AlertClass(ACCIDENTE_CLASS_ID, R.drawable.accident, "icon_accidente", R.string.accidente, "paramedics"),
            new AlertClass(INCENDIO_CLASS_ID, R.drawable.fire, "icon_incendio", R.string.incendio, "firefighters"),
            new AlertClass(HERIDO_CLASS_ID, R.drawable.wounded, "icon_herido", R.string.herido, "paramedics"),
            new AlertClass(BLOQUEO_CLASS_ID, R.drawable.blocked, "icon_bloqueo", R.string.bloqueo, "police"),
            new AlertClass(CONGESTIONAMIENTO_CLASS_ID,  R.drawable.traffic, "icon_congestionamiento", R.string.congestionamiento, null),
            new AlertClass(MARCHAS_CLASS_ID, R.drawable.marching, "icon_marchas", R.string.marchas, null),
            new AlertClass(CALLE_DANADA_CLASS_ID, R.drawable.street_damage, "icon_calle", R.string.calle_danada, null),
            new AlertClass(CORTE_ELECTRICO_CLASS_ID, R.drawable.power_cut, "icon_corte", R.string.corte_electrico, null)
    };

    ArrayList<AlertData> listAlertData = new ArrayList<>();

    ListAlertasUpdateReceiver listAlertsDataUpdateReceiver;
    LocationUpdateReceiver locationUpdateReceiver;
    AdminNumEmergencias adminNumEmergencias;
    //Alerta alertaSeleccionada;
    boolean mapboxLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.println(Log.ASSERT,LOG_TAG,"onCreate()");

        LoadLanguaje();
        InstanciateMapbox();

        setContentView(R.layout.activity_main);
        firstOnboarding();
        InitializeUI();
        InitializeVariables();
        InitializeFirebase();
        InitializeMapbox(savedInstanceState);

        RegisterBroadcastReceivers();
        InitializeBackgroundService();

        GetPermissions();

        resetTitles();
    }

    private void RegisterBroadcastReceivers() {
        IntentFilter listAlertasUpdateFilter = new IntentFilter(BackgroundService.ACTION_LISTALERTAS_UPDATE);
        listAlertasUpdateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        listAlertsDataUpdateReceiver = new ListAlertasUpdateReceiver();

        IntentFilter locationUpdateFilter = new IntentFilter(BackgroundService.ACTION_LOCATION_UPDATE);
        locationUpdateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        locationUpdateReceiver = new LocationUpdateReceiver();

        try {
            registerReceiver(locationUpdateReceiver, locationUpdateFilter);
            registerReceiver(listAlertsDataUpdateReceiver, listAlertasUpdateFilter);
        } catch (Exception e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    private void InitializeBackgroundService() {
        Intent backgroundService = new Intent(this, BackgroundService.class);
        //backgroundService.putExtra(CLASS_LIST_TAG, listClasesAlertas);
        backgroundService.setAction(BackgroundService.ACTION_START_FOREGROUND_SERVICE);
        //startService(backgroundService);
        startForegroundService(backgroundService);
    }

    String accessToken = "pk.eyJ1Ijoic3BhcnRhbmNhdDciLCJhIjoiY2p2ZzVkOWRrMDQ1ejQxcmc2bjgxc3JtYSJ9.Nn4-Xa4AaeoVe3p3z67I7g";

    void InstanciateMapbox(){
        Mapbox.getInstance(this, accessToken);
        Log.d(LOG_TAG, "Initializing Mapbox with Token: " + getString(R.string.mapbox_access_token));
        try {
            MapboxSearchSdk.initialize(getApplication(), getString(R.string.mapbox_access_token), new DefaultLocationProvider(getApplication()));
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    void InitializeVariables() {
        adminNumEmergencias = new AdminNumEmergencias((AdminNumEmergencias.getSystemCountry(this)));
    }

    private void InitializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        updateOptionsMenu(currentUser);

        mAlertProvider = new AlertProvider();
        mCommentProvider = new CommentProvider();
        mImageProvider = new ImageProvider();
        mUserProvider = new UserProvider();
    }

    public double locationLatitude, locationLongitude;

    private void GetPermissions(){
        ArrayList<String> permissionsToGetList = new ArrayList<>();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED){
            permissionsToGetList.add(Manifest.permission.FOREGROUND_SERVICE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionsToGetList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            permissionsToGetList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionsToGetList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            permissionsToGetList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissionsToGetList.add(Manifest.permission.CAMERA);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "No phone call permissions!", Toast.LENGTH_SHORT).show();
            permissionsToGetList.add(Manifest.permission.CALL_PHONE);
        }

        String[] permissionsToGet = new String[permissionsToGetList.size()];
        if(permissionsToGetList.size() > 0) {
            for (int i = 0; i < permissionsToGetList.size(); i++) {
                permissionsToGet[i] = permissionsToGetList.get(i);
            }
            Log.d(LOG_TAG, "Getting " + permissionsToGet.length + " permissions");
            int requestResponse = 0;
            ActivityCompat.requestPermissions(this, permissionsToGet, requestResponse);
        }
    }

    /**
     * Falta refinar manejo de permisos...
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length == 0) {
            Toast.makeText(getApplicationContext(), "PERMISOS DENEGADOS!", Toast.LENGTH_SHORT).show();
            finish();
            System.exit(0);
        } else {
            for (String permission : permissions) {
                Log.d(LOG_TAG, "Permission acquired: " + permission);
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(BackgroundService.ACTION_LOCATION_PERMISSIONS_GRANTED);
                    sendBroadcast(broadcastIntent);

                    enableMapboxLocationComponent(myMapboxMap.getStyle());
                }
            }
        }
    }

    private void firstOnboarding() {
//        getSharedPreferences(Onboarding.onboardingPreferenceName, MODE_PRIVATE)
//                .edit().putString(Onboarding.ONBOARDING_USED_PREFKEY, Onboarding.ONBOARDING_NOT_USED).apply();

        SharedPreferences preferences = getSharedPreferences(Onboarding.onboardingPreferenceName, MODE_PRIVATE);
        String onboardingState = preferences.getString(Onboarding.ONBOARDING_USED_PREFKEY, Onboarding.ONBOARDING_NOT_USED);
        if (onboardingState.equals(Onboarding.ONBOARDING_NOT_USED)) {
            showOnboarding();
        }
    }

    void showOnboarding() {
        Intent onboardingIntent = new Intent(this, Onboarding.class);
        startActivity(onboardingIntent);
    }

    void InitializeUI() {
        mapView = findViewById(R.id.mapView);

        btnAlertMenu = findViewById(R.id.btnAlertMenu);

        btnAlertMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentUser != null) {
                    AlertMenuDialog alertMenuDialog = new AlertMenuDialog();
                    alertMenuDialog.show(getSupportFragmentManager(), ALERT_MENU_DIALOG_TAG);
                } else {
                    Toast.makeText(MainActivity.this, "Please log in to send alerts", Toast.LENGTH_SHORT).show();
                }

            }
        });

        btnPanToDeviceLocation = findViewById(R.id.btnPanToDeviceLocation);

        btnPanToDeviceLocation.setOnClickListener(v -> {
            LocationComponent locationComponent = myMapboxMap.getLocationComponent();
            if (locationComponent.isLocationComponentActivated()) {
                myMapboxMap.getLocationComponent().setCameraMode(CameraMode.TRACKING);
            }
        });
    }

    MapboxMap myMapboxMap;
    SymbolManager symbolManager;

    void InitializeMapbox(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                myMapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.DARK, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                        symbolManager = new SymbolManager(mapView, myMapboxMap, style);
                        symbolManager.addClickListener(new OnSymbolClickListener() {
                            @Override
                            public void onAnnotationClick(Symbol symbol) {
                                UIAlert alert = getAlertaBySymbol(symbol);
                                //Toast.makeText(getApplicationContext(),alert.alertClass.name, Toast.LENGTH_SHORT).show();
                                if (alert != null) {
                                    AbrirMenuAlerta(alert);
                                }
                            }
                        });

                        for (AlertClass listAlertClasses : listClasesAlertas) {
                            style.addImage(
                                    listAlertClasses.icon_name,
                                    BitmapFactory.decodeResource(
                                            getResources(),
                                            listAlertClasses.icon
                                    )
                            );
                        }
                        style.addImage(myLocationSymbolName,
                                BitmapFactory.decodeResource(
                                        getResources(),
                                        myLocationSymbolDrawable
                                )
                        );

                        mapboxLoaded = true;
                        Log.d(LOG_TAG, "Mapbox loaded");

                        if (listAlertData.size() > 0) {
                            showUiAlerts();
                            if (ACTION_SHOW_ALERT.equals(getIntent().getAction()) && !actionShowAlertStarted) {
                                focusCameraOnAlert(getIntent().getStringExtra(EXTRA_SHOW_ALERT_ID_TAG));
                            }
                        }


                        enableMapboxLocationComponent(style);


                    }
                });
            }
        });
    }

    private void enableMapboxLocationComponent(Style style) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            Log.d(LOG_TAG, "No permissions for LocationComponent yet");
        } else {
            LocationComponent locationComponent = myMapboxMap.getLocationComponent();
            if (!locationComponent.isLocationComponentActivated()) {
                locationComponent.activateLocationComponent(new LocationComponentActivationOptions.Builder(this, style).build());
                locationComponent.setLocationComponentEnabled(true);
                locationComponent.setRenderMode(RenderMode.COMPASS);

                locationComponent.addOnCameraTrackingChangedListener(new OnCameraTrackingChangedListener() {
                    @Override
                    public void onCameraTrackingDismissed() {

                    }

                    @Override
                    public void onCameraTrackingChanged(int currentMode) {
                        if (currentMode == CameraMode.TRACKING) {
                            locationComponent.zoomWhileTracking(16, 3000);
                        }
                    }
                });

                if (!ACTION_SHOW_ALERT.equals(getIntent().getAction())) {
                    locationComponent.setCameraMode(CameraMode.TRACKING);
                }
            }
        }
    }

    private void focusCameraOnAlert(String alertId) {
        if (alertId != null) {
            for (int i = 0; i < listAlertData.size(); i++) {
                if (alertId.equals(listAlertData.get(i).getId())) {
                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(listAlertData.get(i).getLatitude(), listAlertData.get(i).getLongitude()))
                            .zoom(18f)
                            .build();
                    myMapboxMap.easeCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), 2500);

                    actionShowAlertStarted = true;
                    break;
                }
            }
        }

    }

    void openLoginDialog() {
        LoginDialog loginDialog = new LoginDialog();
        loginDialog.show(getSupportFragmentManager(), LOGIN_DIALOG_TAG);
    }

    @Override
    public void loginButtonClicked(String email, String password, final LoginDialog dialog) {

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            final LoginDialog loginDialog = dialog;
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    try {
                        loginDialog.loginSuccessful();
                        currentUser = mAuth.getCurrentUser();
                        updateOptionsMenu(currentUser);
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        loginDialog.loginFailed();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    void openRegisterDialog() {
        RegisterDialog registerDialog = new RegisterDialog();
        registerDialog.show(getSupportFragmentManager(), REGISTER_DIALOG_TAG);
    }

    @Override
    public void registerButtonClicked(String username, String email, String password, final RegisterDialog dialog) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    try {
                        mUserProvider.create(task.getResult().getUser().getUid(), username).addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.i(LOG_TAG, "Username " + username + " correctly registered with " + task.getResult().getUser().getUid());
                            }
                        });
                        dialog.registerSuccessful();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                } else {
                    try {
                        dialog.registerSuccessful();
                    } catch (NullPointerException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    void LogOut() {
        mAuth.signOut();
        currentUser = mAuth.getCurrentUser();
        updateOptionsMenu(currentUser);
    }

    void createVisibleAlert(AlertData data) {

        AlertClass alertClass = getClass(data.getClassId());

        Float[] offset = {0f, 2.5f};
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(data.getLatitude(), data.getLongitude()))
                .withIconImage(alertClass.icon_name)
                .withIconSize(0.30f)
                .withIconOffset(offset));

        UIAlert alert = new UIAlert(data, alertClass, symbol);
        uiAlertList.add(alert);
    }

    UIAlert getAlertaBySymbol(Symbol symbol){
        for (int i = 0; i < uiAlertList.size(); i++){
            if (uiAlertList.get(i).getSymbol() == symbol) {
                return uiAlertList.get(i);
            }
        }
        return null;
    }
    void AbrirMenuAlerta(UIAlert alert){

        AlertOptionsDialog alertOptionsDialog = new AlertOptionsDialog(alert);
        alertOptionsDialog.show(getSupportFragmentManager(), ALERT_OPTIONS_DIALOG_TAG);

    }

    /*
    Bitmap getImageBitmap(UIAlert alert) {
        Bitmap res = null;
        for (int i=0; i < listImagenes.size(); i++){
            //Log.i( LOG_TAG,"Comparando alert " + alert.id + " y imagen de " + listImagenes.get(i).id_alerta);
            if (listImagenes.get(i).id_alerta == alert.id){
                Log.i( LOG_TAG, "Imagen encontrada");

                String path = getCacheDir() + "/TempPics";
                File dir = new File(path);
                if(dir.mkdirs()){
                    Log.i( LOG_TAG, "Directorio en cache creado");
                }

                File file = new File(path, listImagenes.get(i).nombre);
                Log.i( LOG_TAG, "Temp file dir: " + file.getAbsolutePath());
                try {
                    if(!file.exists()){
                        file.createNewFile();
                        Log.i( LOG_TAG, "Archivo creado");
                        FileUtils.writeByteArrayToFile(file, listImagenes.get(i).bitmap);
                        Log.i( LOG_TAG, "Archivo escrito");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                res = BitmapFactory.decodeFile(file.getPath());
                break;
            }
        }
        return  res;
    }
    */

    @Override
    public void sendAlertComment(UIAlert alert, String comment, AlertOptionsDialog dialog) {
        sendNewComment(alert, comment, dialog);
    }

    private void sendNewComment(UIAlert alert, String text, AlertOptionsDialog dialog) {
        Comment newComment = new Comment(null, alert.getAlertData().getId(), currentUser.getUid(), new Date(), text);

        mCommentProvider.create(newComment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "Comment Sent", Toast.LENGTH_SHORT).show();
                dialog.clearCommentTextBox();
            }
        });
    }

    /**
     * Completar esto...
     *
     * @param alert
     */
    @Override
    public void openComments(UIAlert alert) {
        Intent intent = new Intent(this, CommentActivity.class);
        intent.putExtra("alertId", alert.getAlertData().getId());
        startActivity(intent);
    }

    @Override
    public Context getActContext() {
        return getApplicationContext();
    }

    @Override
    public String getCurrentUserId() {
        if (currentUser != null) {
            return currentUser.getUid();
        } else {
            return null;
        }

    }

    @Override
    public void onAlertClicked(int class_id, boolean includePic) {
        SendNewAlert(getClass(class_id), null, includePic);
    }

    @Override
    public void onCustomAlertClicked(String customName, boolean includePic) {
        Toast.makeText(this, "send alert! (" + customName + ")", Toast.LENGTH_SHORT).show();
        SendNewAlert(getClass(CUSTOM_CLASS_ID), customName, includePic);
    }

    void SendNewAlert(AlertClass alertClass, String customName, boolean includePic) {

        DocumentReference newDoc = mAlertProvider.getNewDocument();

        AlertData alertData = new AlertData(
                newDoc.getId(),
                currentUser.getUid(),
                locationLatitude,
                locationLongitude,
                alertClass.id,
                new Date()
        );

        if (alertClass.id == CUSTOM_CLASS_ID) {
            alertData.setCustomName(customName);
        }

        mAlertProvider.create(alertData, newDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, getText(R.string.alert_sent), Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, getText(R.string.alert_not_sent), Toast.LENGTH_SHORT).show();
            }
        });

        if (includePic) {
           takeImage(alertData);
        }

        if (alertClass.help_service != null) {
            String numeroEmergencia = adminNumEmergencias.getEmergencyNumber(alertClass);
            if (numeroEmergencia != null) {
                adminNumEmergencias.dialogEmergencyCall(this, numeroEmergencia);
            }
        }
    }

    void showUiAlerts() {
        for (int i = 0; i < uiAlertList.size(); i++) {
            symbolManager.delete(uiAlertList.get(i).getSymbol());
        }
        uiAlertList = new ArrayList<>();

        for (int i = 0; i < listAlertData.size(); i++) {
            String id = listAlertData.get(i).getId();
            double latitud = listAlertData.get(i).getLatitude();
            double longitud = listAlertData.get(i).getLongitude();
            Date fecha = listAlertData.get(i).getDate();
            //Date fecha = listAlertData.get(i).getFecha().toDate();
            AlertClass alertClass = getClass(listAlertData.get(i).getClassId());

            createVisibleAlert(listAlertData.get(i));
        }
        Log.d(LOG_TAG, "Alerts displayed: " + uiAlertList.toString());
    }

    public static AlertClass getClass(int classId) {
        for (AlertClass alertClass : listClasesAlertas) {
            if (classId == alertClass.id) {
                return alertClass;
            }
        }
        return null;
    }

    void takeImage(AlertData alertData) {
        getPreferences(Context.MODE_PRIVATE).edit().putString("NewPhotoAlertId", alertData.getId()).apply();

        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        else
        {
            dispatchTakePictureIntent();
        }
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        String TEMP_IMAGE_PATH = getExternalFilesDir(Environment.DIRECTORY_PICTURES).getAbsolutePath() + "/Temp";
        File tempImageDirectory = new File(TEMP_IMAGE_PATH);
        String tempImageName = "TempPic_" + new Date().getTime();
        String tempImageSuffix = ".jpg";

        if (tempImageDirectory.mkdirs()) {
            Log.d(LOG_TAG, "Temporary images directory created");
        }

        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null && tempImageDirectory.exists()) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = File.createTempFile(
                        tempImageName,
                        tempImageSuffix,
                        tempImageDirectory
                );
            } catch (IOException ex) {
                ex.fillInStackTrace();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this,
                        "edu.integrator.rpagv2.fileprovider",
                        photoFile);

                // Save a file: path for use with ACTION_VIEW intents
                getPreferences(Context.MODE_PRIVATE).edit().putString("NewPhotoFilePath", photoFile.getAbsolutePath()).apply();

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, CAMERA_REQUEST);
            }
        }
    }

    static File SaveBitmapAsJPEG(Bitmap bitmap, int quality, File directory, String filename){

        File file = new File(directory, filename);

        if(directory.mkdirs()){
            Log.d(LOG_TAG, "Nuevo directorio creado");
        }

        if (directory.exists()) {
            Log.d(LOG_TAG, "Directorio existente, creando archivo");
            try {
                if(file.createNewFile()){
                    Log.d(LOG_TAG, "Nuevo archivo creado");
                    bitmap.compress(Bitmap.CompressFormat.JPEG, quality, new FileOutputStream(file));
                } else {
                    Log.d(LOG_TAG, "Nuevo archivo no pudo ser creado");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return file;
        } else {
            Log.d(LOG_TAG, "Directorio aun no existente, retornando null");
            return null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {

            SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);

            String tempFilePath = sharedPref.getString("NewPhotoFilePath", null);
            final String alertId = sharedPref.getString("NewPhotoAlertId", null);

            SharedPreferences.Editor editor = sharedPref.edit();
            editor.remove("NewPhotoFilePath");
            editor.remove("NewPhotoAlertId");
            editor.apply();

            Bitmap savedBitmap = BitmapFactory.decodeFile(tempFilePath);
            Bitmap scaledBitmap = RescaleBitmap(savedBitmap, 1280);

            final String imageName = alertId + "_" + new Date().getTime() + ".jpg";
            final File savedImageFile = SaveBitmapAsJPEG(scaledBitmap, 75, getExternalFilesDir(Environment.DIRECTORY_PICTURES), imageName);

            if (savedImageFile != null) {
                mImageProvider.uploadStorageFile(Uri.fromFile(savedImageFile)).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        ImageData imageData = new ImageData(null, savedImageFile.getName(), alertId, currentUser.getUid(), new Date(), null);
                        mImageProvider.create(imageData).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(MainActivity.this, "Successful Image Upload", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Image Registration Failed", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Image Upload Failed", Toast.LENGTH_SHORT).show();
                        Log.e(LOG_TAG, "Image Upload Failed");
                        e.fillInStackTrace();
                    }
                });
            } else {
                Toast.makeText(this, "Image could not be saved", Toast.LENGTH_SHORT).show();
            }

            File tempImage = new File(tempFilePath);
            if (tempImage.delete()) {
                Log.d(LOG_TAG, "Temporary image file deleted");
            }

        }
    }

    private Bitmap RescaleBitmap(Bitmap bitmap, int biggestDimension) {
        float scale;
        if (bitmap.getHeight() > bitmap.getWidth()) {
            scale = (float) biggestDimension / (float) bitmap.getHeight();
        } else {
            scale = (float) biggestDimension / (float) bitmap.getWidth();
        }
        return Bitmap.createScaledBitmap(bitmap, Math.round(bitmap.getWidth() * scale), Math.round(bitmap.getHeight() * scale), true);
    }

    private void LoadLanguaje() {

    }

    protected void resetTitles() {
        try {
            ActivityInfo info = getPackageManager().getActivityInfo(getComponentName(), GET_META_DATA);
            if (info.labelRes != 0) {
                setTitle(info.labelRes);
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void setNewLocale(AppCompatActivity context, @LocaleManager.LocaleDef String language) {
        LocaleManager.setNewLocale(this, language);
        Intent intent = context.getIntent();
        startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK));
    }

    // private boolean alertDataProvided = false;
    private class ListAlertasUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "PackDatos update received");
            ArrayList<AlertData> updateListAlertas = null;
            try {
                updateListAlertas = (ArrayList<AlertData>) intent.getExtras().get(BackgroundService.LISTALERTAS_UPDATE_TAGNAME);
            } catch (Exception e) {
                Toast.makeText(context, "Update Unsuccessful", Toast.LENGTH_SHORT).show();
            }

            if (updateListAlertas != null) {

                listAlertData = updateListAlertas;

                if (mapboxLoaded) {
                    showUiAlerts();
                    if (ACTION_SHOW_ALERT.equals(getIntent().getAction()) && !actionShowAlertStarted) {
                        focusCameraOnAlert(getIntent().getStringExtra(EXTRA_SHOW_ALERT_ID_TAG));
                    }
                }

                // alertDataProvided = true;
            } else {
                Log.i(LOG_TAG, "PackDatos is NULL!");
            }
        }
    }

    private class LocationUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "Location update received from Main");

            locationLatitude = intent.getExtras().getDouble(BackgroundService.LATITUDE_TAGNAME);
            locationLongitude = intent.getExtras().getDouble(BackgroundService.LONGITUDE_TAGNAME);
        }
    }

    void openAboutScreen() {
        Intent aboutIntent = new Intent(this, About.class);
        startActivity(aboutIntent);
    }

    void OpenHelpNumbersScreen() {
        Intent helpNumbersIntent = new Intent(this, HelpNumbersActivity.class);
        startActivity(helpNumbersIntent);
    }

    /**
     * Methods to update the UI
     */
    /*
    void updateUI() {

    }
    */
    /**
     * Update for firebase user change
     * @param firebaseUser
     */
    void updateUI(FirebaseUser firebaseUser) {

    }
    void updateOptionsMenu(FirebaseUser firebaseUser) {
        if(optionsMenu != null) {
            if(firebaseUser != null) {
                optionsMenu.findItem(R.id.btnLoginOption).setVisible(false);
                optionsMenu.findItem(R.id.btnRegisterOption).setVisible(false);
                optionsMenu.findItem(R.id.btnLogOut).setVisible(true);
            } else {
                optionsMenu.findItem(R.id.btnLoginOption).setVisible(true);
                optionsMenu.findItem(R.id.btnRegisterOption).setVisible(true);
                optionsMenu.findItem(R.id.btnLogOut).setVisible(false);
            }
        } else {
            Log.e(LOG_TAG, "optionsMenu = null");
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.options_menu, menu);
        optionsMenu = menu;

        updateOptionsMenu(currentUser); // Updates menu items for signed in user or no user
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.btnSpanish)
            setNewLocale(MainActivity.this, LocaleManager.SPANISH);
        else if (item.getItemId() == R.id.btnEnglish)
            setNewLocale(MainActivity.this, LocaleManager.ENGLISH);
        else if (item.getItemId() == R.id.btnAutomaticLang)
            setNewLocale(MainActivity.this, LocaleManager.AUTO);
        else if (item.getItemId() == R.id.btnLoginOption)
            openLoginDialog();
        else if (item.getItemId() == R.id.btnRegisterOption)
            openRegisterDialog();
        else if (item.getItemId() == R.id.btnAbout)
            openAboutScreen();
        else if (item.getItemId() == R.id.btnLogOut)
            LogOut();
        else if (item.getItemId() == R.id.btnHelpNumbers)
            OpenHelpNumbersScreen();
        else if (item.getItemId() == R.id.btnShowOnboarding)
            showOnboarding();
        else
            return super.onOptionsItemSelected(item);
        return true;
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.println(Log.ASSERT,LOG_TAG,"onStart()");
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.println(Log.ASSERT,LOG_TAG,"onResume()");
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.println(Log.ASSERT,LOG_TAG,"onPause()");
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.println(Log.ASSERT,LOG_TAG,"onStop()");
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.println(Log.ASSERT,LOG_TAG,"onLowMemory()");
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.println(Log.ASSERT,LOG_TAG,"onDestroy()");
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setContextLocale(newBase));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}
