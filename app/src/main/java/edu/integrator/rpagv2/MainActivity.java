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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.storage.UploadTask;
import com.integrator.rpagv2.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

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

    public static final String LOG_TAG = "RPAG-Log";

    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_PERMISSION_CODE = 100;

    final static String CLASS_LIST_TAG = "CLASS_LIST_TAG";
    final static String ALERT_MENU_DIALOG_TAG = "ALERT_MENU_DIALOG_TAG";
    final static String LOGIN_DIALOG_TAG = "LOGIN_DIALOG_TAG";
    final static String REGISTER_DIALOG_TAG = "REGISTER_DIALOG_TAG";
    final static String ALERT_OPTIONS_DIALOG_TAG = "ALERT_OPTIONS_DIALOG_TAG";
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
    int myLocationSymbolDrawable = R.drawable.circle_blue_overlay;
    Symbol myLocationSymbol;

    final static int ACCIDENTE_CLASS_ID = 1;
    final static int INCENDIO_CLASS_ID = 2;
    final static int HERIDO_CLASS_ID = 3;
    final static int BLOQUEO_CLASS_ID = 4;
    final static int CONGESTIONAMIENTO_CLASS_ID = 5;
    final static int MARCHAS_CLASS_ID = 6;
    final static int CALLE_DANADA_CLASS_ID = 7;
    final static int CORTE_ELECTRICO_CLASS_ID = 8;

    final static AlertClass[] listClasesAlertas = {
            new AlertClass(ACCIDENTE_CLASS_ID, R.drawable.accident, "icon_accidente", R.string.accidente),
            new AlertClass(INCENDIO_CLASS_ID, R.drawable.fire, "icon_incendio", R.string.incendio),
            new AlertClass(HERIDO_CLASS_ID, R.drawable.wounded, "icon_herido", R.string.herido),
            new AlertClass(BLOQUEO_CLASS_ID, R.drawable.blocked, "icon_bloqueo", R.string.bloqueo),
            new AlertClass(CONGESTIONAMIENTO_CLASS_ID,  R.drawable.traffic, "icon_congestionamiento", R.string.congestionamiento),
            new AlertClass(MARCHAS_CLASS_ID, R.drawable.marching, "icon_marchas", R.string.marchas),
            new AlertClass(CALLE_DANADA_CLASS_ID, R.drawable.street_damage, "icon_calle", R.string.calle_danada),
            new AlertClass(CORTE_ELECTRICO_CLASS_ID, R.drawable.power_cut, "icon_corte", R.string.corte_electrico)
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
        LoadLanguaje();
        InstanciateMapbox();

        setContentView(R.layout.activity_main);
        showOnboarding();
        InitializeUI();
        InitializeVariables();
        InitializeFirebase();
        InitializeMapbox(savedInstanceState);
        InitializeBackgroundService();
        RegisterBroadcastReceivers();

        GetPermissions();

        resetTitles();
    }

    private void InitializeBackgroundService() {
        Log.i( LOG_TAG,"InitializeBackgroundService()");
        Intent backgroundService = new Intent(this, BackgroundService.class);
        backgroundService.putExtra(CLASS_LIST_TAG, listClasesAlertas);
        startService(backgroundService);
    }

    private void RegisterBroadcastReceivers() {
        Log.i( LOG_TAG,"RegisterBroadcastReceivers()");

        IntentFilter listAlertasUpdateFilter = new IntentFilter(BackgroundService.ACTION_LISTALERTAS_UPDATE);
        listAlertasUpdateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        listAlertsDataUpdateReceiver = new ListAlertasUpdateReceiver();

        IntentFilter locationUpdateFilter = new IntentFilter(BackgroundService.MyLocationListener.ACTION_LOCATION_UPDATE);
        locationUpdateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        locationUpdateReceiver = new LocationUpdateReceiver();

        try {
            registerReceiver(locationUpdateReceiver, locationUpdateFilter);
            registerReceiver(listAlertsDataUpdateReceiver, listAlertasUpdateFilter);
        } catch (Exception e) {
            Log.e(LOG_TAG, Objects.requireNonNull(e.getMessage()));
        }
    }

    String accessToken = "pk.eyJ1Ijoic3BhcnRhbmNhdDciLCJhIjoiY2p2ZzVkOWRrMDQ1ejQxcmc2bjgxc3JtYSJ9.Nn4-Xa4AaeoVe3p3z67I7g";

    void InstanciateMapbox(){
        Mapbox.getInstance(this, accessToken);
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
    }

    public double locationLatitude, locationLongitude;

    void GetPermissions(){
        ArrayList<String> permissionsToGetList = new ArrayList<>();

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
            permissionsToGetList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            permissionsToGetList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "No phone call permissions!", Toast.LENGTH_SHORT).show();
            permissionsToGetList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        String[] permissionsToGet = new String[permissionsToGetList.size()];
        if(permissionsToGetList.size() > 0) {
            for (int i = 0; i < permissionsToGetList.size(); i++) {
                permissionsToGet[i] = permissionsToGetList.get(i);
            }
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
                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Intent broadcastIntent = new Intent();
                    broadcastIntent.setAction(BackgroundService.ACTION_LOCATION_PERMISSIONS_GRANTED);
                    sendBroadcast(broadcastIntent);
                }
            }
        }
    }

    final static String onboardingPreferenceName = "Onboarding";
    final static String ONBOARDING_USED_PREFKEY = "ONBOARDING_USED_PREFKEY";
    final static String ONBOARDING_USED = "ONBOARDING_USED";
    final static String ONBOARDING_NOT_USED = "ONBOARDING_NOT_USED";

    /**
     *
     * Nota: terminar esto!
     *
     */
    void showOnboarding() {
        SharedPreferences preferences = getSharedPreferences(onboardingPreferenceName, MODE_PRIVATE);
        String onboardingState = preferences.getString(ONBOARDING_USED_PREFKEY, ONBOARDING_NOT_USED);
        if (onboardingState.equals(ONBOARDING_NOT_USED)) {

            Intent onboardingIntent = new Intent(this, Onboarding.class);
            startActivity(onboardingIntent);
            preferences.edit().putString(ONBOARDING_USED_PREFKEY, ONBOARDING_USED).apply();

            Log.i(LOG_TAG, "Launching Onboarding");
        }
        /*
        Intent onboardingIntent = new Intent(this, Onboarding.class);
        startActivity(onboardingIntent);*/
    }

    MapView mapView;
    ImageButton btnAlertMenu;

    Menu optionsMenu;

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
                        Log.i(LOG_TAG, "mapboxLoaded = " + mapboxLoaded);

                        if (alertDataProvided) {
                            showUiAlerts();
                        }
                        if (locationProvided) {
                            updateLocationSymbol();
                        }
                    }
                });
            }
        });
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
    public void registerButtonClicked(String email, String password, final RegisterDialog dialog) {

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    try {
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

    void createVisibleAlert(String id, AlertClass clase, double lat, double len, Date fecha) {

        Float[] offset = {0f, 2.5f};
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(lat,len))
                .withIconImage(clase.icon_name)
                .withIconSize(0.30f)
                .withIconOffset(offset));

        UIAlert alert = new UIAlert(id,lat,len, fecha,symbol,clase);
        uiAlertList.add(alert);
    }

    void updateLocationSymbol() {
        Log.i(LOG_TAG, "updateLocationSymbol()");
        if(myLocationSymbol != null) {
            symbolManager.delete(myLocationSymbol);
            myLocationSymbol = symbolManager.create(new SymbolOptions()
                    .withLatLng(new LatLng(locationLatitude, locationLongitude))
                    .withIconImage(myLocationSymbolName)
                    .withIconSize(0.05f));
            Log.i(LOG_TAG, "Updated Self Location Symbol: " + locationLatitude + " - " + locationLongitude);
        } else {
            myLocationSymbol = symbolManager.create(new SymbolOptions()
                    .withLatLng(new LatLng(locationLatitude, locationLongitude))
                    .withIconImage(myLocationSymbolName)
                    .withIconSize(0.05f));
            Log.i(LOG_TAG, "New Self Location Symbol: " + locationLatitude + " - " + locationLongitude);
        }

    }

    UIAlert getAlertaBySymbol(Symbol symbol){
        for (int i = 0; i < uiAlertList.size(); i++){
            if (uiAlertList.get(i).symbol == symbol) {
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
    public void sendAlertComment(UIAlert alert, String comment) {
        sendNewComment(alert, comment);
    }

    private void sendNewComment(UIAlert alert, String text) {
        Comment newComment = new Comment(null, alert.id, currentUser.getUid(), new Date(), text);

        mCommentProvider.create(newComment).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "Comment Sent", Toast.LENGTH_SHORT).show();
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
        intent.putExtra("alertId", alert.id);
        startActivity(intent);
    }

    @Override
    public String getCurrentUserId() {
        return currentUser.getUid();
    }

    @Override
    public void onAlertClicked(int class_id, boolean includePic) {
        SendNewAlert(getClass(class_id), includePic);
    }

    void SendNewAlert(AlertClass alertClass, boolean includePic) {

        DocumentReference newDoc = mAlertProvider.getNewDocument();

        AlertData alertData = new AlertData(
                newDoc.getId(),
                currentUser.getUid(),
                locationLatitude,
                locationLongitude,
                alertClass.id,
                new Date()
        );

        mAlertProvider.create(alertData, newDoc).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(MainActivity.this, "Alerta Enviada", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Alerta no pudo ser enviada", Toast.LENGTH_SHORT).show();
            }
        });

        if (includePic) {
           takeImage(alertData);
        }

        int numeroEmergencia = adminNumEmergencias.getEmergencyNumber(alertClass.id);
        adminNumEmergencias.dialogEmergencyCall(this, numeroEmergencia);
    }

    void showUiAlerts() {
        for (int i = 0; i < uiAlertList.size(); i++) {
            symbolManager.delete(uiAlertList.get(i).symbol);
        }
        uiAlertList = new ArrayList<>();

        for (int i = 0; i < listAlertData.size(); i++) {
            String id = listAlertData.get(i).getId();
            double latitud = listAlertData.get(i).getLatitude();
            double longitud = listAlertData.get(i).getLongitude();
            Date fecha = listAlertData.get(i).getDate();
            //Date fecha = listAlertData.get(i).getFecha().toDate();
            AlertClass alertClass = getClass(listAlertData.get(i).getClassId());

            createVisibleAlert(id, alertClass, latitud, longitud, fecha);
        }
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

    private boolean alertDataProvided = false;
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
                }

                alertDataProvided = true;
            } else {
                Log.i(LOG_TAG, "PackDatos is NULL!");
            }
        }
    }

    private boolean locationProvided = false;
    private class LocationUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(LOG_TAG, "Location update received from Main");

            locationLatitude = intent.getExtras().getDouble(BackgroundService.MyLocationListener.LATITUDE_TAGNAME);
            locationLongitude = intent.getExtras().getDouble(BackgroundService.MyLocationListener.LONGITUDE_TAGNAME);
            if(mapboxLoaded) {
                updateLocationSymbol();
            }

            locationProvided = true;
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
        if (item.getItemId() == R.id.btnEnglish)
            setNewLocale(MainActivity.this, LocaleManager.ENGLISH);
        if (item.getItemId() == R.id.btnAutomaticLang)
            setNewLocale(MainActivity.this, LocaleManager.AUTO);
        if (item.getItemId() == R.id.btnLoginOption)
            openLoginDialog();
        if (item.getItemId() == R.id.btnRegisterOption)
            openRegisterDialog();
        if (item.getItemId() == R.id.btnAbout)
            openAboutScreen();
        if (item.getItemId() == R.id.btnLogOut)
            LogOut();
        if (item.getItemId() == R.id.btnHelpNumbers)
            OpenHelpNumbersScreen();

        switch (item.getItemId()) {
            case R.id.btnSpanish:
                setNewLocale(MainActivity.this, LocaleManager.SPANISH);
                return true;
            case R.id.btnEnglish:
                setNewLocale(MainActivity.this, LocaleManager.ENGLISH);
                return true;
            case R.id.btnAutomaticLang:
                setNewLocale(MainActivity.this, LocaleManager.AUTO);
                return true;
            case R.id.btnLoginOption:
                openLoginDialog();
                return true;
            case R.id.btnRegisterOption:
                openRegisterDialog();
                return true;
            case R.id.btnAbout:
                openAboutScreen();
                return true;
            case R.id.btnLogOut:
                LogOut();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.println(Log.ASSERT,"MapboxTestLog","onStart()");
        mapView.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleManager.setLocale(newBase));
    }

    @Override
    public void onBackPressed() {
        getSharedPreferences(onboardingPreferenceName, MODE_PRIVATE).edit().putString(ONBOARDING_USED_PREFKEY, ONBOARDING_NOT_USED).apply();
        super.onBackPressed();
    }
}
