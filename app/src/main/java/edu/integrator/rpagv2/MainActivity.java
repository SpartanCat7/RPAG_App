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
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Objects;

import static android.content.pm.PackageManager.GET_META_DATA;

public class MainActivity extends AppCompatActivity implements
        AlertMenuDialog.AlertMenuDialogInterface,
        LoginDialog.LoginDialogInterface,
        RegisterDialog.RegisterDialogInterface,
        AlertOptionsDialog.AlertOptionsDialogInterface {

    int ID_Usuario = 0;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    public static final String LOG_TAG = "RPAG-Log";

    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_PERMISSION_CODE = 100;


    final static String IP_SERVIDOR = "192.168.0.6";
    final static int PORT_SERVIDOR = 6809;

    final static String CLASS_LIST_TAG = "CLASS_LIST_TAG";
    final static String ALERT_MENU_DIALOG_TAG = "ALERT_MENU_DIALOG_TAG";
    final static String LOGIN_DIALOG_TAG = "LOGIN_DIALOG_TAG";
    final static String REGISTER_DIALOG_TAG = "REGISTER_DIALOG_TAG";
    final static String ALERT_OPTIONS_DIALOG_TAG = "ALERT_OPTIONS_DIALOG_TAG";
    /*
    ClaseAlerta claseAccidente = new ClaseAlerta(
            1,  "Accidente", R.drawable.frontal_crash, "icon_accidente");
    ClaseAlerta claseIncendio = new ClaseAlerta(
            2,  "Incendio", R.drawable.fire, "icon_incendio");
    ClaseAlerta claseHerido = new ClaseAlerta(
            3,  "Persona Herida", R.drawable.band_aid, "icon_herido");
    ClaseAlerta claseBloqueo = new ClaseAlerta(
            4,  "Bloqueo", R.drawable.road_blockade, "icon_bloqueo");
    ClaseAlerta claseCongestionamiento = new ClaseAlerta(
            5,  "Congestionamiento", R.drawable.traffic_jam, "icon_congestionamiento");
    ClaseAlerta claseMarchas = new ClaseAlerta(
            6,  "Marchas", R.drawable.parade, "icon_marchas");
    ClaseAlerta claseCalleDanada = new ClaseAlerta(
            7,  "Calle Dañada", R.drawable.road, "icon_calle");
    ClaseAlerta claseCorte = new ClaseAlerta(
            8,  "Corte Electrico", R.drawable.flash, "icon_corte");
*/
    ArrayList<Alerta> listAlertasMostradas = new ArrayList<>();

    final static String MAPBOX_STYLE = "mapbox://styles/spartancat7/ckammphtj1rao1imlhr7sjtgy";
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

    final static ClaseAlerta[] listClasesAlertas = {
            new ClaseAlerta(ACCIDENTE_CLASS_ID, R.drawable.accident, "icon_accidente", R.string.accidente),
            new ClaseAlerta(INCENDIO_CLASS_ID, R.drawable.fire, "icon_incendio", R.string.incendio),
            new ClaseAlerta(HERIDO_CLASS_ID, R.drawable.wounded, "icon_herido", R.string.herido),
            new ClaseAlerta(BLOQUEO_CLASS_ID, R.drawable.blocked, "icon_bloqueo", R.string.bloqueo),
            new ClaseAlerta(CONGESTIONAMIENTO_CLASS_ID,  R.drawable.traffic, "icon_congestionamiento", R.string.congestionamiento),
            new ClaseAlerta(MARCHAS_CLASS_ID, R.drawable.marching, "icon_marchas", R.string.marchas),
            new ClaseAlerta(CALLE_DANADA_CLASS_ID, R.drawable.street_damage, "icon_calle", R.string.calle_danada),
            new ClaseAlerta(CORTE_ELECTRICO_CLASS_ID, R.drawable.power_cut, "icon_corte", R.string.corte_electrico)
    };

    ArrayList<DatosAlerta> listDatosAlertas = new ArrayList<>();
    ArrayList<Confirmacion> listConfirmaciones = new ArrayList<>();
    ArrayList<Reporte> listReportes = new ArrayList<>();
    ArrayList<Comentario> listComentarios = new ArrayList<>();
    ArrayList<Imagen> listImagenes = new ArrayList<>();

    PackDatosUpdateReceiver packDatosUpdateReceiver;
    LocationUpdateReceiver locationUpdateReceiver;
    AdminNumEmergencias adminNumEmergencias;
    Alerta alertaSeleccionada;
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

        SendPackDatosUpdateRequest();
    }

    private void InitializeBackgroundService() {
        Log.i( "RPAG-Log","InitializeBackgroundService()");
        Intent backgroundService = new Intent(this, BackgroundService.class);
        backgroundService.putExtra(CLASS_LIST_TAG, listClasesAlertas);
        startService(backgroundService);
    }

    private void RegisterBroadcastReceivers() {
        Log.i( "RPAG-Log","RegisterBroadcastReceivers()");
        IntentFilter packDatosUpdateFilter = new IntentFilter(BackgroundService.ACTION_PACKDATOS_RESPONSE);
        packDatosUpdateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        packDatosUpdateReceiver = new PackDatosUpdateReceiver();

        IntentFilter locationUpdateFilter = new IntentFilter(MyLocationListener.ACTION_LOCATION_UPDATE);
        locationUpdateFilter.addCategory(Intent.CATEGORY_DEFAULT);
        locationUpdateReceiver = new LocationUpdateReceiver();

        try {
            registerReceiver(packDatosUpdateReceiver, packDatosUpdateFilter);
            registerReceiver(locationUpdateReceiver, locationUpdateFilter);
        } catch (Exception e) {
            Log.e("RPAG-Log", Objects.requireNonNull(e.getMessage()));
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
    }

    public double locationLatitude, locationLongitude;

    /**
     * Antigua inicializacion de LocationManager y LocationListener
     */
    /*
    void InitializeLocation() {
        locationListener = new MyLocationListener(this);
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            int requestResponse = 0;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    requestResponse);
            Toast.makeText(getApplicationContext(), "Sin permisos", Toast.LENGTH_SHORT).show();
        }
        else {
            Toast.makeText(this, "Localizacion Inicializada", Toast.LENGTH_SHORT).show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);
        }
    }
    */

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
            Toast.makeText(this, "No phone call permissions!", Toast.LENGTH_SHORT);
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
        }
        else {
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
        if (onboardingState == ONBOARDING_NOT_USED) {

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

    /**
     * Alert Options
     */
    /*
    LinearLayout layoutAlertOptions;
    Button btnConfirmar, btnReportar, btnComentar;
    TextView
            txtTipoAlerta,
            txtConfirmaciones,
            txtReportes,
            txtLatitud,
            txtLongitud,
            txtComentarios;
    EditText txtComentar;

    ImageView imgImagenAlerta;
    */
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

        /*
        layoutAlertOptions = findViewById(R.id.layoutAlertOptions);
        btnConfirmar = findViewById(R.id.btnConfirmar);
        btnReportar = findViewById(R.id.btnReportar);
        btnComentar = findViewById(R.id.btnComentar);
        txtTipoAlerta = findViewById(R.id.txtTipoAlerta);
        txtConfirmaciones = findViewById(R.id.txtConfirmaciones);
        txtReportes = findViewById(R.id.txtReportes);
        txtLatitud = findViewById(R.id.txtLatitud);
        txtLongitud = findViewById(R.id.txtLongitud);

        txtComentar = findViewById(R.id.txtComentar);
        txtComentarios = findViewById(R.id.txtComentarios);

        btnConfirmar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnviarNuevaConfirmacion(alertaSeleccionada);
                layoutAlertOptions.setVisibility(View.GONE);
            }
        });
        btnReportar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnviarNuevoReporte(alertaSeleccionada);
                layoutAlertOptions.setVisibility(View.GONE);
            }
        });
        btnComentar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnviarNuevoComentario(alertaSeleccionada, txtComentar.getText().toString());
                txtComentar.setText("");
                layoutAlertOptions.setVisibility(View.GONE);
            }
        });

        imgImagenAlerta = findViewById(R.id.imgImagenAlerta);
        */
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
                                Alerta alerta = getAlertaBySymbol(symbol);
                                //Toast.makeText(getApplicationContext(),alerta.claseAlerta.name, Toast.LENGTH_SHORT).show();
                                if (alerta != null) {
                                    AbrirMenuAlerta(alerta);
                                }
                            }
                        });

                        for (int i = 0; i < listClasesAlertas.length; i++) {
                            style.addImage(
                                    listClasesAlertas[i].icon_name,
                                    BitmapFactory.decodeResource(
                                            getResources(),
                                            listClasesAlertas[i].icon
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
                        Log.i("RPAG-Log", "mapboxLoaded = " + mapboxLoaded);
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
            LoginDialog loginDialog = dialog;
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

    void createVisibleAlert(int id, ClaseAlerta clase, double lat, double len, Date fecha) {

        Float[] offset = {0f, 2.5f};
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(lat,len))
                .withIconImage(clase.icon_name)
                .withIconSize(0.30f)
                .withIconOffset(offset));

        Alerta alerta = new Alerta(id,lat,len, fecha,symbol,clase);
        listAlertasMostradas.add(alerta);
    }

    void updateLocationSymbol() {
        Log.i("RPAG-Log", "updateLocationSymbol()");
        if(myLocationSymbol != null) {
            symbolManager.delete(myLocationSymbol);
            myLocationSymbol = symbolManager.create(new SymbolOptions()
                    .withLatLng(new LatLng(locationLatitude, locationLongitude))
                    .withIconImage(myLocationSymbolName)
                    .withIconSize(0.05f));
            Log.i("RPAG-Log", "Updated Self Location Symbol: " + locationLatitude + " - " + locationLongitude);
        } else {
            myLocationSymbol = symbolManager.create(new SymbolOptions()
                    .withLatLng(new LatLng(locationLatitude, locationLongitude))
                    .withIconImage(myLocationSymbolName)
                    .withIconSize(0.05f));
            Log.i("RPAG-Log", "New Self Location Symbol: " + locationLatitude + " - " + locationLongitude);
        }

    }

    Alerta getAlertaBySymbol(Symbol symbol){
        for (int i = 0; i < listAlertasMostradas.size(); i++){
            if (listAlertasMostradas.get(i).symbol == symbol) {
                return listAlertasMostradas.get(i);
            }
        }
        return null;
    }
    void AbrirMenuAlerta(Alerta alerta){

        int confirmaciones = getConfirmationsCount(alerta);
        int reportes = getReportsCount(alerta);
        Bitmap imagen = getImageBitmap(alerta);

        AlertOptionsDialog alertOptionsDialog = new AlertOptionsDialog(alerta, confirmaciones, reportes, imagen);
        alertOptionsDialog.show(getSupportFragmentManager(), ALERT_OPTIONS_DIALOG_TAG);
        /*
        layoutAlertOptions.setVisibility(View.VISIBLE);
        alertaSeleccionada = alerta;

        int contador_confirmaciones = 0;
        int contador_reportes = 0;
        btnConfirmar.setEnabled(true);
        for (int i=0; i<listConfirmaciones.size(); i++){
            if (listConfirmaciones.get(i).id_usuario == ID_Usuario && listConfirmaciones.get(i).id_alerta == alertaSeleccionada.id){
                btnConfirmar.setEnabled(false);
            }
            if (listConfirmaciones.get(i).id_alerta == alertaSeleccionada.id) {
                contador_confirmaciones++;
            }
        }
        btnReportar.setEnabled(true);
        for (int i=0; i<listReportes.size(); i++){
            if (listReportes.get(i).id_usuario == ID_Usuario && listReportes.get(i).id_alerta == alertaSeleccionada.id){
                btnReportar.setEnabled(false);
            }
            if (listReportes.get(i).id_alerta == alertaSeleccionada.id) {
                contador_reportes++;
            }
        }

        txtTipoAlerta.setText("Tipo: " + getString(alerta.claseAlerta.name_string_ID));
        txtConfirmaciones.setText("Confirmaciones: " + contador_confirmaciones);
        txtReportes.setText("Reportes: " + contador_reportes);
        txtLatitud.setText("Latitud: " + alerta.lat);
        txtLongitud.setText("Longitud: " + alerta.len);

        imgImagenAlerta.setImageBitmap(null);
        for (int i=0; i<listImagenes.size(); i++){
            Log.i( "RPAG-Log","Comparando alerta " + alertaSeleccionada.id + " y imagen de " + listImagenes.get(i).id_alerta);
            if (listImagenes.get(i).id_alerta == alertaSeleccionada.id){
                Log.i( "RPAG-Log","Imagen encontrada");

                String path = getCacheDir() + "/TempPics";
                File dir = new File(path);
                if(dir.mkdirs()){
                    Log.i( "RPAG-Log","Directorio en cache creado");
                }

                File file = new File(path, listImagenes.get(i).nombre);
                Log.i( "RPAG-Log","Temp file dir: " + file.getAbsolutePath());
                try {
                    if(!file.exists()){
                        file.createNewFile();
                        Log.i( "RPAG-Log","Archivo creado");
                        FileUtils.writeByteArrayToFile(file,listImagenes.get(i).bitmap);
                        Log.i( "RPAG-Log","Archivo escrito");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Bitmap bitmap = BitmapFactory.decodeFile(file.getPath());
                imgImagenAlerta.setImageBitmap(bitmap);
                break;
            }
        }

        txtComentarios.setText("");
        for (int i=0; i<listComentarios.size(); i++){
            if(listComentarios.get(i).id_alerta == alertaSeleccionada.id){
                txtComentarios.append(System.lineSeparator());
                txtComentarios.append(listComentarios.get(i).texto);
            }
        }
        */

    }

    ArrayList<Comentario> getAlertComments(Alerta alerta) {
        ArrayList<Comentario> res = new ArrayList<>();
        for (int i=0; i<listComentarios.size(); i++){
            if(listComentarios.get(i).id_alerta == alerta.id){
                res.add(listComentarios.get(i));
            }
        }
        return res;
    }
    int getConfirmationsCount(Alerta alerta) {
        int res = 0;
        for (int i=0; i < listConfirmaciones.size(); i++){
            if (listConfirmaciones.get(i).id_alerta == alerta.id) {
                res++;
            }
        }
        return res;
    }
    int getReportsCount(Alerta alerta) {
        int res = 0;
        for (int i=0; i < listReportes.size(); i++){
            if (listReportes.get(i).id_alerta == alerta.id) {
                res++;
            }
        }
        return res;
    }

    Bitmap getImageBitmap(Alerta alerta) {
        Bitmap res = null;
        for (int i=0; i < listImagenes.size(); i++){
            //Log.i( LOG_TAG,"Comparando alerta " + alerta.id + " y imagen de " + listImagenes.get(i).id_alerta);
            if (listImagenes.get(i).id_alerta == alerta.id){
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

    @Override
    public void sendAlertConfirmation(Alerta alerta) {
        EnviarNuevaConfirmacion(alerta);
    }

    private void EnviarNuevaConfirmacion(Alerta alerta) {
        Confirmacion nuevaConfirmacion = new Confirmacion();
        nuevaConfirmacion.id_alerta = alerta.id;
        nuevaConfirmacion.id_usuario = ID_Usuario;
        nuevaConfirmacion.fecha = new Date();

        EnviarConfirmacion enviarAlerta = new EnviarConfirmacion(nuevaConfirmacion,this,IP_SERVIDOR,PORT_SERVIDOR);
        try {
            enviarAlerta.join(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SendPackDatosUpdateRequest();
    }

    @Override
    public void sendAlertReport(Alerta alerta) {
        EnviarNuevoReporte(alerta);
    }

    private void EnviarNuevoReporte(Alerta alerta) {
        Reporte nuevoReporte = new Reporte();
        nuevoReporte.id_alerta = alerta.id;
        nuevoReporte.id_usuario = ID_Usuario;
        nuevoReporte.fecha = new Date();

        EnviarReporte enviarReporte = new EnviarReporte(nuevoReporte,this,IP_SERVIDOR,PORT_SERVIDOR);
        try {
            enviarReporte.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SendPackDatosUpdateRequest();
    }

    @Override
    public void sendAlertComment(Alerta alerta, String comentario) {
        EnviarNuevoComentario(alerta, comentario);
    }

    private void EnviarNuevoComentario(Alerta alerta, String texto) {
        Comentario nuevoComentario = new Comentario();
        nuevoComentario.id_alerta = alerta.id;
        nuevoComentario.id_usuario = ID_Usuario;
        nuevoComentario.fecha = new Date();
        nuevoComentario.texto = texto;

        EnviarComentario enviarComentario = new EnviarComentario(nuevoComentario,this,IP_SERVIDOR,PORT_SERVIDOR);
        try {
            enviarComentario.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SendPackDatosUpdateRequest();
    }

    /**
     * Completar esto!!!
     *
     * @param alerta
     */
    @Override
    public void openComments(Alerta alerta) {

    }

    @Override
    public void onAlertClicked(int class_id, boolean includePic) {

        EnviarNuevaAlerta(getClase(class_id), includePic);
    }

    void EnviarNuevaAlerta(ClaseAlerta claseAlerta, boolean includePic) {

        DatosAlerta datosAlerta = new DatosAlerta(
                0,
                ID_Usuario,
                locationLatitude,
                locationLongitude,
                claseAlerta.id,
                new Date()
        );

        if(includePic){
           EnviarNuevaAlertaConImagen(datosAlerta);
        }
        else {
            EnviarAlerta enviarAlerta = new EnviarAlerta(datosAlerta,this,IP_SERVIDOR,PORT_SERVIDOR);
            try {
                enviarAlerta.join(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        int numeroEmergencia = adminNumEmergencias.getEmergencyNumber(claseAlerta.id);
        adminNumEmergencias.dialogEmergencyCall(this, numeroEmergencia);
        SendPackDatosUpdateRequest();
    }
    void SendPackDatosUpdateRequest() {
        Log.i( "RPAG-Log","SendPackDatosUpdateRequest()");

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction(BackgroundService.ACTION_UPDATE_REQUEST);
        sendBroadcast(broadcastIntent);
    }

    void mostrarAlertas() {
        for (int i = 0; i < listAlertasMostradas.size(); i++)
        {
            symbolManager.delete(listAlertasMostradas.get(i).symbol);
        }
        listAlertasMostradas = new ArrayList<>();

        for (int i = 0; i < listDatosAlertas.size(); i++)
        {
            int id = listDatosAlertas.get(i).id;
            double latitud = listDatosAlertas.get(i).latitud;
            double longitud = listDatosAlertas.get(i).longitud;
            Date fecha = listDatosAlertas.get(i).fecha;
            ClaseAlerta claseAlerta = getClase(listDatosAlertas.get(i).clase_id);

            createVisibleAlert(id, claseAlerta, latitud, longitud, fecha);
        }
    }

    DatosAlerta datosAlerta_EnvioImagen;
    ClaseAlerta claseAlerta_EnvioImagen;
    void EnviarNuevaAlertaConImagen(DatosAlerta datosAlerta){

        datosAlerta_EnvioImagen = datosAlerta;
        claseAlerta_EnvioImagen = getClase(datosAlerta.clase_id);
        Log.i( "RPAG-Log","datosAlerta.id = " + datosAlerta_EnvioImagen.id);
        Log.i( "RPAG-Log","claseAlerta.name = " + getString(claseAlerta_EnvioImagen.name_string_ID));
        if (ActivityCompat.checkSelfPermission(this,Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_CODE);
        }
        else
        {
            Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAMERA_REQUEST);
        }
    }

     ClaseAlerta getClase(int clase_id) {
        for (int i = 0; i < listClasesAlertas.length; i++) {
            if (clase_id == listClasesAlertas[i].id) {
                return listClasesAlertas[i];
            }
        }
        return null;
    }

    static File GuardarBitmapComoPNG(Bitmap bitmap, String path, String filename){

        File file = new File(path, filename);
        File directory = new File(path);

        Log.v("RPAG-Log", "File: " + file.getAbsolutePath());
        Log.v("RPAG-Log", "Directory: " + directory.getAbsolutePath());
        if(directory.mkdirs()){
            Log.v("RPAG-Log", "Nuevo directorio creado");
        }
        try {
            if(file.createNewFile()){
                Log.v("RPAG-Log", "Nuevo archivo creado");
                bitmap.compress(Bitmap.CompressFormat.PNG,75,new FileOutputStream(file));
            }
            else {
                Log.v("RPAG-Log", "Nuevo archivo no pudo ser creado");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return file;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST && resultCode == Activity.RESULT_OK) {
            Log.v( "RPAG-Log","Enviando datosAlerta.id = " + datosAlerta_EnvioImagen.id + ", fecha = " + datosAlerta_EnvioImagen.fecha.toString());
            Log.v( "RPAG-Log","Enviando claseAlerta.name = " + getString(claseAlerta_EnvioImagen.name_string_ID));

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Log.v("RPAG-Log", "Bitmap capturado: " + photo.getByteCount() + " bytes");

            String nombre_imagen = new Date().getTime() + "_" + getString(claseAlerta_EnvioImagen.name_string_ID) + ".png";
            File imgFile = GuardarBitmapComoPNG(
                    photo,
                    Environment.getExternalStorageDirectory().getAbsolutePath() + "/RPAG_Pics",nombre_imagen);

            Imagen imagen = new Imagen();
            try {
                if(imgFile.exists()){
                    imagen.bitmap = FileUtils.readFileToByteArray(imgFile);
                }
                else {
                    Log.v("RPAG-Log", "Archivo no existe");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            imagen.id_alerta = 0;
            imagen.fecha = new Date();
            imagen.nombre = nombre_imagen;
            imagen.id_usuario = datosAlerta_EnvioImagen.id_usuario;

            imagen.alerta = datosAlerta_EnvioImagen;

            EnviarImagen enviarImagen = new EnviarImagen(imagen, this, IP_SERVIDOR, PORT_SERVIDOR);
            try {
                enviarImagen.join(4000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SendPackDatosUpdateRequest();
        }

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



    private class PackDatosUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("RPAG-Log", "PackDatos update received");
            PackDatos packDatos = (PackDatos) intent.getSerializableExtra(BackgroundService.PACKDATOS_UPDATE_TAGNAME);
            if (packDatos != null) {
                listDatosAlertas = packDatos.listaDatosAlertas;
                listConfirmaciones = packDatos.listaConfirmaciones;
                listReportes = packDatos.listaReportes;
                listComentarios = packDatos.listaComentarios;
                listImagenes = packDatos.listaImagenes;

                if (mapboxLoaded) {
                    mostrarAlertas();
                }
            }
            else {
                Log.i("RPAG-Log", "PackDatos is NULL!");
            }
        }
    }

    private class LocationUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i("RPAG-Log", "Location update received from Main");

            locationLatitude = intent.getExtras().getDouble(MyLocationListener.LATITUDE_TAGNAME);
            locationLongitude = intent.getExtras().getDouble(MyLocationListener.LONGITUDE_TAGNAME);
            if(mapboxLoaded){
                updateLocationSymbol();
            }

        }
    }

    void openAboutScreen() {
        Intent aboutIntent = new Intent(this, About.class);
        startActivity(aboutIntent);
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
