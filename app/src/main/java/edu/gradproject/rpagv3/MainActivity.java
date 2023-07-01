package edu.gradproject.rpagv3;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.mapbox.geojson.Point;
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
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.search.MapboxSearchSdk;
import com.mapbox.search.ResponseInfo;
import com.mapbox.search.ReverseGeoOptions;
import com.mapbox.search.ReverseGeocodingSearchEngine;
import com.mapbox.search.SearchCallback;
import com.mapbox.search.SearchRequestTask;
import com.mapbox.search.location.DefaultLocationProvider;
import com.mapbox.search.result.SearchResult;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import edu.gradproject.rpagv3.Dialogs.AlertDataDialog;
import edu.gradproject.rpagv3.Dialogs.AlertTypesDialog;
import edu.gradproject.rpagv3.Dialogs.LoginDialog;
import edu.gradproject.rpagv3.Dialogs.RegisterDialog;
import edu.gradproject.rpagv3.Models.AlertData;
import edu.gradproject.rpagv3.Models.AlertType;
import edu.gradproject.rpagv3.Models.AlertSymbolBundle;
import edu.gradproject.rpagv3.Models.User;
import edu.gradproject.rpagv3.Providers.AlertTypeProvider;
import edu.gradproject.rpagv3.Providers.CommentProvider;
import edu.gradproject.rpagv3.Providers.UserProvider;
import edu.gradproject.rpagv3.Utils.LocaleManager;

import static android.content.pm.PackageManager.GET_META_DATA;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private CommentProvider mCommentProvider;
    private UserProvider mUserProvider;
    private AlertTypeProvider mAlertTypeProvider;


    MapView mapView;
    FloatingActionButton btnAlertMenu;
    FloatingActionButton btnPanToDeviceLocation;
    Menu optionsMenu;

    public static final String LOG_TAG = "RPAG-Log";

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
            2,  "Incendio", R.drawable.icon_type_fire, "icon_incendio");
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
    ArrayList<AlertSymbolBundle> alertSymbols = new ArrayList<>();

    //final static String MAPBOX_STYLE = "mapbox://styles/spartancat7/ckammphtj1rao1imlhr7sjtgy";
//    String myLocationSymbolName = "MyLocationIcon";
//    int myLocationSymbolDrawable = R.drawable.pin;
//    Symbol myLocationSymbol;

//    final static int CUSTOM_CLASS_ID = 0;
//    final static int ACCIDENTE_CLASS_ID = 1;
//    final static int INCENDIO_CLASS_ID = 2;
//    final static int HERIDO_CLASS_ID = 3;
//    final static int BLOQUEO_CLASS_ID = 4;
//    final static int CONGESTIONAMIENTO_CLASS_ID = 5;
//    final static int MARCHAS_CLASS_ID = 6;
//    final static int CALLE_DANADA_CLASS_ID = 7;
//    final static int CORTE_ELECTRICO_CLASS_ID = 8;

    private ArrayList<AlertType> alertTypeList = new ArrayList<>();
    private boolean alertTypeListFullyLoaded = false;
    private ArrayList<AlertType> pendingTypesToLoadToStyle = new ArrayList<>();

    private String pendingNewAlertActivityTypeId;
    private boolean pendingAlertTypesDialog = false;

    private ArrayList<AlertData> pendingAlertsToAddToMap = new ArrayList<>();

    public double locationLatitude, locationLongitude;
    private String address;

    /*
    final static AlertClass[] listClasesAlertas = {
            new AlertClass(CUSTOM_CLASS_ID, R.drawable.custom_alert_icon_orange, "icon_custom_alert", R.string.custom_alert, null),
            new AlertClass(ACCIDENTE_CLASS_ID, R.drawable.icon_type_accident, "icon_accidente", R.string.accidente, "paramedics"),
            new AlertClass(INCENDIO_CLASS_ID, R.drawable.icon_type_fire, "icon_incendio", R.string.incendio, "firefighters"),
            new AlertClass(HERIDO_CLASS_ID, R.drawable.icon_type_wounded, "icon_herido", R.string.herido, "paramedics"),
            new AlertClass(BLOQUEO_CLASS_ID, R.drawable.icon_type_blocked, "icon_bloqueo", R.string.bloqueo, "police"),
            new AlertClass(CONGESTIONAMIENTO_CLASS_ID,  R.drawable.icon_type_traffic, "icon_congestionamiento", R.string.congestionamiento, null),
            new AlertClass(MARCHAS_CLASS_ID, R.drawable.icon_type_marching, "icon_marchas", R.string.marchas, null),
            new AlertClass(CALLE_DANADA_CLASS_ID, R.drawable.icon_type_street_damage, "icon_calle", R.string.calle_danada, null),
            new AlertClass(CORTE_ELECTRICO_CLASS_ID, R.drawable.icon_type_power_cut, "icon_corte", R.string.corte_electrico, null)
    };*/

    ArrayList<AlertData> listAlertData = new ArrayList<>();

    ListAlertasUpdateReceiver listAlertsDataUpdateReceiver;
    LocationUpdateReceiver locationUpdateReceiver;
//    AdminNumEmergencias adminNumEmergencias;
    //Alerta alertaSeleccionada;
    //boolean mapboxLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.println(Log.ASSERT,LOG_TAG,"onCreate()");

        //LoadLanguaje();
        InstanciateMapbox();

        setContentView(R.layout.activity_main);
        firstOnboarding();
        InitializeUI();
//        InitializeVariables();
        InitializeFirebase();

        if (!alertTypeListFullyLoaded) {
            LoadAlertTypes();
        }

        InitializeMapbox(savedInstanceState);

        RegisterBroadcastReceivers();
        InitializeBackgroundService();

        GetPermissions();

        resetTitles();
    }

    private void RegisterBroadcastReceivers() {
        IntentFilter listAlertasUpdateFilter = new IntentFilter(ForegroundService.ACTION_LISTALERTAS_UPDATE);
        listAlertasUpdateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        listAlertsDataUpdateReceiver = new ListAlertasUpdateReceiver();

        IntentFilter locationUpdateFilter = new IntentFilter(ForegroundService.ACTION_LOCATION_UPDATE);
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
        Intent backgroundService = new Intent(this, ForegroundService.class);
        //backgroundService.putExtra(CLASS_LIST_TAG, listClasesAlertas);
        backgroundService.setAction(ForegroundService.ACTION_START_FOREGROUND_SERVICE);
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

//    void InitializeVariables() {
//        adminNumEmergencias = new AdminNumEmergencias((AdminNumEmergencias.getSystemCountry(this)));
//    }

    private void InitializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        updateOptionsMenu(currentUser);
        
        mAlertTypeProvider = new AlertTypeProvider();
        mCommentProvider = new CommentProvider();
        mUserProvider = new UserProvider();
    }
    
    private void LoadAlertTypes() {
        mAlertTypeProvider.getActiveAlertTypes().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                QuerySnapshot queryDocumentSnapshots = task.getResult();
                processAlertTypes(queryDocumentSnapshots);
            } else {
                Toast.makeText(MainActivity.this, "Error cargando tipos de alertas", Toast.LENGTH_SHORT).show();
                System.exit(0);
            }
        });
    }

    private void processAlertTypes(QuerySnapshot queryDocumentSnapshots) {
        ArrayList<AlertType> newAlertTypeList = new ArrayList<>();
        alertTypeListFullyLoaded = false;
        for (DocumentSnapshot snap : queryDocumentSnapshots.getDocuments()) {
            AlertType type = new AlertType(snap);
            mAlertTypeProvider.getTypeIconFile(MainActivity.this, type.getIcon(), new AlertTypeProvider.getTypeIconFileCallback() {
                @Override
                public void onSuccess(byte[] bytes) {
                    type.setIconBitmap(BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                }

                @Override
                public void onFailure() {
                    Log.e(LOG_TAG, "COULD NOT GET AN ICON FILE FOR TYPE: " + type.getName() + " (" + type.getId() + ")");
                    type.setIconBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.custom_alert_icon_orange));
                }

                @Override
                public void Finally() {
                    newAlertTypeList.add(type);
                    AlertTypeProvider.updateAlertTypeList(type, alertTypeList); // alertTypeList is updated until it is time to replace it entirely

                    if (myMapboxMap.getStyle() != null && myMapboxMap.getStyle().isFullyLoaded()) {
                        myMapboxMap.getStyle().addImage(type.getId(), type.getIconBitmap());
                        ArrayList<AlertData> newList = new ArrayList<>(pendingAlertsToAddToMap);
                        for (AlertData data : pendingAlertsToAddToMap) {
                            if (Objects.equals(data.getTypeId(), type.getId())) {
                                addAlertToMap(data, type);
                                newList.remove(data);
                            }
                        }
                        pendingAlertsToAddToMap = newList;
                    } else {
                        pendingTypesToLoadToStyle.add(type);
                    }

                    if (newAlertTypeList.size() == queryDocumentSnapshots.getDocuments().size()) { // check if newAlertTypeList is ready to become the new alertTypeList
                        alertTypeList = newAlertTypeList;
                        alertTypeListFullyLoaded = true;
                        if (pendingAlertTypesDialog) {
                            showNewAlertTypeDialog();
                            pendingAlertTypesDialog = false;
                        }
                    }
                }
            });
        }
    }

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
                    broadcastIntent.setAction(ForegroundService.ACTION_LOCATION_PERMISSIONS_GRANTED);
                    sendBroadcast(broadcastIntent);
                    if (myMapboxMap.getStyle() != null && myMapboxMap.getStyle().isFullyLoaded()) {
                        enableMapboxLocationComponent(myMapboxMap.getStyle());
                    }
                }
            }
        }
    }

    private void firstOnboarding() {
//        getSharedPreferences(OnboardingActivity.onboardingPreferenceName, MODE_PRIVATE)
//                .edit().putString(OnboardingActivity.ONBOARDING_USED_PREFKEY, OnboardingActivity.ONBOARDING_NOT_USED).apply();

        SharedPreferences preferences = getSharedPreferences(OnboardingActivity.onboardingPreferenceName, MODE_PRIVATE);
        String onboardingState = preferences.getString(OnboardingActivity.ONBOARDING_USED_PREFKEY, OnboardingActivity.ONBOARDING_NOT_USED);
        if (onboardingState.equals(OnboardingActivity.ONBOARDING_NOT_USED)) {
            showOnboarding();
        }
    }

    void showNewAlertActivity(String typeId) {
        Intent newAlertActivityIntent = new Intent(this, NewAlertActivity.class);
        newAlertActivityIntent.putExtra(NewAlertActivity.ALERT_TYPE_ID_EXTRA, typeId);
        newAlertActivityIntent.putExtra(NewAlertActivity.DEVICE_LAT_EXTRA, locationLatitude);
        newAlertActivityIntent.putExtra(NewAlertActivity.DEVICE_LNG_EXTRA, locationLongitude);
        newAlertActivityIntent.putExtra(NewAlertActivity.DEVICE_ADDRESS_EXTRA, address);
        startActivity(newAlertActivityIntent);
    }

    void showOnboarding() {
        Intent onboardingIntent = new Intent(this, OnboardingActivity.class);
        startActivity(onboardingIntent);
    }

    void showNewAlertTypeDialog() {
        AlertTypesDialog alertTypesDialog = new AlertTypesDialog(new AlertTypesDialog.AlertTypesDialogInterface() {
            @Override
            public void onAlertTypeClicked(String typeId) {
                if (locationLatitude != 0 && locationLongitude != 0) {
                    showNewAlertActivity(typeId);
                } else {
                    pendingNewAlertActivityTypeId = typeId;
                }
            }

            @Override
            public ArrayList<AlertType> getAlertTypes() {
                return alertTypeList;
            }
        });
        alertTypesDialog.show(getSupportFragmentManager(), ALERT_MENU_DIALOG_TAG);
    }

    void InitializeUI() {
        mapView = findViewById(R.id.mapView);

        btnAlertMenu = findViewById(R.id.btnAlertMenu);

        btnAlertMenu.setOnClickListener(v -> {

//                if (currentUser != null) {
//                    AlertMenuDialog alertMenuDialog = new AlertMenuDialog();
//                    alertMenuDialog.show(getSupportFragmentManager(), ALERT_MENU_DIALOG_TAG);
//                } else {
//                    Toast.makeText(MainActivity.this, "Please log in to send alerts", Toast.LENGTH_SHORT).show();
//                }
            if (currentUser != null) {
                if (alertTypeListFullyLoaded) {
                    showNewAlertTypeDialog();
                } else {
                    pendingAlertTypesDialog = true;
                }
            } else {
                Toast.makeText(MainActivity.this, "Please log in to send alerts", Toast.LENGTH_SHORT).show();
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
        mapView.getMapAsync(mapboxMap -> {
            myMapboxMap = mapboxMap;
            mapboxMap.setStyle(Style.DARK, style -> {
                // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                symbolManager = new SymbolManager(mapView, myMapboxMap, style);
                symbolManager.addClickListener(symbol -> {
                    AlertSymbolBundle alert = getAlertBundleBySymbol(symbol);
                    //Toast.makeText(getApplicationContext(),alert.alertClass.name, Toast.LENGTH_SHORT).show();
                    if (alert != null) {
                        OpenAlertDataDialog(alert);
                    }
                });

                ArrayList<AlertType> newList = new ArrayList<>(pendingTypesToLoadToStyle);
                for (AlertType alertType : pendingTypesToLoadToStyle) {
                    style.addImage(alertType.getId(), alertType.getIconBitmap());
                    newList.remove(alertType);
                }
                pendingTypesToLoadToStyle = newList;
//                        for (AlertClass listAlertClasses : listClasesAlertas) {
//                            style.addImage(
//                                    listAlertClasses.icon_name,
//                                    BitmapFactory.decodeResource(
//                                            getResources(),
//                                            listAlertClasses.icon
//                                    )
//                            );
//                        }


                // No longer used. Mapbox has its own
//                        style.addImage(myLocationSymbolName,
//                                BitmapFactory.decodeResource(
//                                        getResources(),
//                                        myLocationSymbolDrawable
//                                )
//                        );

                //mapboxLoaded = true;
                Log.d(LOG_TAG, "Mapbox loaded");

                if (listAlertData.size() > 0) {
                    //clearCurrentAlertSymbols();
                    addAllAlertsSymbolsToMap();
                    if (ACTION_SHOW_ALERT.equals(getIntent().getAction()) && !actionShowAlertStarted) {
                        focusCameraOnAlert(getIntent().getStringExtra(EXTRA_SHOW_ALERT_ID_TAG));
                    }
                }

                enableMapboxLocationComponent(style);
            });
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
        LoginDialog loginDialog = new LoginDialog(new LoginDialog.LoginDialogInterface() {
            @Override
            public void loginButtonClicked(String email, String password, LoginDialog dialog) {
                mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        try {
                            dialog.loginSuccessful();
                            currentUser = mAuth.getCurrentUser();
                            updateOptionsMenu(currentUser);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            dialog.loginFailed();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public FirebaseAuth getAuth() {
                return mAuth;
            }

            @Override
            public void loginWithPhoneAuthCredentials(PhoneAuthCredential credential, LoginDialog dialog) {
                mAuth.signInWithCredential(credential).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        try {
                            dialog.loginSuccessful();
                            currentUser = mAuth.getCurrentUser();
                            updateOptionsMenu(currentUser);
                            mUserProvider.getUserById(currentUser.getUid()).addOnCompleteListener(task1 -> {
                                if (task1.isSuccessful()) {
                                    if (task1.getResult().exists()) {
                                        User user = new User(task1.getResult());
                                        Toast.makeText(MainActivity.this, "Welcome " + user.getUsername(), Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(MainActivity.this, "New user!", Toast.LENGTH_SHORT).show();

                                        mUserProvider.createEmptyProfileWithTelf(new User(currentUser.getUid(), currentUser.getPhoneNumber())).addOnCompleteListener(createProfileTask -> {
                                            if (createProfileTask.isSuccessful()) {
                                                Intent newUserProfile = new Intent(MainActivity.this, ProfileSettingsActivity.class);
                                                newUserProfile.setAction(ProfileSettingsActivity.NEW_USER_PROFILE_ACTION);
                                                newUserProfile.putExtra(ProfileSettingsActivity.EXTRA_KEY_USER_ID, task1.getResult().getId());
                                                startActivity(newUserProfile);
                                            } else {
                                                Toast.makeText(MainActivity.this, "ERROR: could not create empty profile", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "ERROR: could not retrieve user data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    } else {
                        try {
                            dialog.loginFailed();
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
        loginDialog.show(getSupportFragmentManager(), LOGIN_DIALOG_TAG);
    }

    void openRegisterDialog() {
        RegisterDialog registerDialog;
        registerDialog = new RegisterDialog((username, email, password, dialog) -> mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(MainActivity.this, task -> {
            if(task.isSuccessful()) {
                try {
                    mUserProvider.create(Objects.requireNonNull(task.getResult().getUser()).getUid(), username)
                            .addOnSuccessListener(aVoid -> Log.i(LOG_TAG, "Username " + username + " correctly registered with " + task.getResult().getUser().getUid()));
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
        }));
        registerDialog.show(getSupportFragmentManager(), REGISTER_DIALOG_TAG);
    }

    void LogOut() {
        mAuth.signOut();
        currentUser = mAuth.getCurrentUser();
        updateOptionsMenu(currentUser);
    }

    void OpenAlertDataDialog(AlertSymbolBundle alert){
        AlertType type = AlertTypeProvider.getAlertType(alert.getAlertData().getTypeId(), alertTypeList);
        AlertDataDialog alertDataDialog = new AlertDataDialog(alertDataDialogInterface, alert, type);
        alertDataDialog.show(getSupportFragmentManager(), ALERT_OPTIONS_DIALOG_TAG);
    }

    AlertDataDialog.AlertDataDialogInterface alertDataDialogInterface = new AlertDataDialog.AlertDataDialogInterface() {

        @Override
        public void openComments(AlertSymbolBundle alert) {
            Intent intent = new Intent(getApplicationContext(), CommentActivity.class);
            intent.putExtra("alertId", alert.getAlertData().getId());
            startActivity(intent);
        }

        @Override
        public String getCurrentUserId() {
            if (currentUser != null) {
                return currentUser.getUid();
            } else {
                return null;
            }
        }
    };

    void clearCurrentAlertSymbols() {
        for (int i = 0; i < alertSymbols.size(); i++) {
            symbolManager.delete(alertSymbols.get(i).getSymbol());
        }
        alertSymbols = new ArrayList<>();
    }

    void addAllAlertsSymbolsToMap() {
        for (AlertData data : listAlertData) {
            AlertType type = AlertTypeProvider.getAlertType(data.getTypeId(), alertTypeList);
            if (type != null) {
                addAlertToMap(data, type);
            } else {
                pendingAlertsToAddToMap.add(data);
            }
        }
    }

    void addAlertToMap(AlertData data, AlertType type) {
        if (data.getDate().getTime() < new Date().getTime() - ((long) type.getLifetime() * 60 * 1000)) return;

        Float[] offset = {0f, 2.5f};
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(data.getLatitude(), data.getLongitude()))
                .withIconImage(type.getId())
                .withIconSize(0.6f)
                .withIconOffset(offset));

        AlertSymbolBundle alertSymbolBundle = new AlertSymbolBundle(data, symbol);
        alertSymbols.add(alertSymbolBundle);
    }

    AlertSymbolBundle getAlertBundleBySymbol(Symbol symbol){
        for (int i = 0; i < alertSymbols.size(); i++){
            if (alertSymbols.get(i).getSymbol() == symbol) {
                return alertSymbols.get(i);
            }
        }
        return null;
    }

//    private void LoadLanguaje() {
//
//    }

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
                updateListAlertas = (ArrayList<AlertData>) intent.getExtras().get(ForegroundService.LISTALERTAS_UPDATE_TAGNAME);
            } catch (Exception e) {
                Toast.makeText(context, "Update Unsuccessful", Toast.LENGTH_SHORT).show();
            }

            if (updateListAlertas != null) {
                pendingAlertsToAddToMap = new ArrayList<>();
                listAlertData = updateListAlertas;
                if (myMapboxMap != null && myMapboxMap.getStyle() != null && myMapboxMap.getStyle().isFullyLoaded()) {
                    clearCurrentAlertSymbols();
                    addAllAlertsSymbolsToMap();
                    if (ACTION_SHOW_ALERT.equals(getIntent().getAction()) && !actionShowAlertStarted) {
                        focusCameraOnAlert(getIntent().getStringExtra(EXTRA_SHOW_ALERT_ID_TAG));
                    }
                }
            }
        }
    }

    private void updateAddress() {
        ReverseGeocodingSearchEngine reverseGeocoding = MapboxSearchSdk.createReverseGeocodingSearchEngine();
        ReverseGeoOptions options = new ReverseGeoOptions.Builder(Point.fromLngLat(locationLongitude, locationLatitude))
                .limit(1)
                .build();
        reverseGeocoding.search(options, new SearchCallback() {
            @Override
            public void onResults(@NonNull List<? extends SearchResult> list, @NonNull ResponseInfo responseInfo) {
                address = list.get(0).getAddress().formattedAddress();
            }

            @Override
            public void onError(@NonNull Exception e) {
                e.printStackTrace();
            }
        });
    }

    private class LocationUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "Location update received from Main");

            locationLatitude = intent.getExtras().getDouble(ForegroundService.LATITUDE_TAGNAME);
            locationLongitude = intent.getExtras().getDouble(ForegroundService.LONGITUDE_TAGNAME);
            updateAddress();

            if (pendingNewAlertActivityTypeId != null) {
                showNewAlertActivity(pendingNewAlertActivityTypeId);
                pendingNewAlertActivityTypeId = null;
            }
        }
    }

    void openAboutScreen() {
        Intent aboutIntent = new Intent(this, AboutActivity.class);
        startActivity(aboutIntent);
    }

    void OpenHelpNumbersScreen() {
        Intent helpNumbersIntent = new Intent(this, HelpNumbersActivity.class);
        startActivity(helpNumbersIntent);
    }

    void OpenProfileSettingsScreen() {
        Intent newUserProfile = new Intent(this, ProfileSettingsActivity.class);
        newUserProfile.putExtra(ProfileSettingsActivity.EXTRA_KEY_USER_ID, mAuth.getUid());
        startActivity(newUserProfile);
    }

    /**
     * Update for firebase user change
     */
    /*void updateUI(FirebaseUser firebaseUser) {

    }*/
    void updateOptionsMenu(FirebaseUser firebaseUser) {
        if(optionsMenu != null) {
            if(firebaseUser != null) {
                optionsMenu.findItem(R.id.btnLoginOption).setVisible(false);
                optionsMenu.findItem(R.id.btnRegisterOption).setVisible(false);
                optionsMenu.findItem(R.id.btnProfileSettingsOption).setVisible(true);
                optionsMenu.findItem(R.id.btnLogOut).setVisible(true);
            } else {
                optionsMenu.findItem(R.id.btnLoginOption).setVisible(true);
                optionsMenu.findItem(R.id.btnRegisterOption).setVisible(false);
                optionsMenu.findItem(R.id.btnProfileSettingsOption).setVisible(false);
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
        else if (item.getItemId() == R.id.btnProfileSettingsOption)
            OpenProfileSettingsScreen();
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
    protected void onSaveInstanceState(@NonNull Bundle outState) {
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
