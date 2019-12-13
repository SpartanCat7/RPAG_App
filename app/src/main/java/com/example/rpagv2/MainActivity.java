package com.example.rpagv2;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class MainActivity extends AppCompatActivity {

    int ID_Usuario = 0;

    private static final int CAMERA_REQUEST = 1888;
    private static final int CAMERA_PERMISSION_CODE = 100;

    String IP_SERVIDOR = "192.168.1.205";
    int PORT_SERVIDOR = 6809;

    ClaseAlerta claseAccidente = new ClaseAlerta(1, "Accidente", R.drawable.frontal_crash, "icon_accidente");
    ClaseAlerta claseIncendio = new ClaseAlerta(2, "Incendio", R.drawable.fire, "icon_incendio");
    ClaseAlerta claseHerido = new ClaseAlerta(3, "Persona Herida", R.drawable.band_aid, "icon_herido");
    ClaseAlerta claseBloqueo = new ClaseAlerta(4, "Bloqueo", R.drawable.road_blockade, "icon_bloqueo");
    ClaseAlerta claseCongestionamiento = new ClaseAlerta(5, "Congestionamiento", R.drawable.traffic_jam, "icon_congestionamiento");
    ClaseAlerta claseMarchas = new ClaseAlerta(6, "Marchas", R.drawable.parade, "icon_marchas");
    ClaseAlerta claseCalleDanada = new ClaseAlerta(7, "Calle Dañada", R.drawable.road, "icon_calle");
    ClaseAlerta claseCorte = new ClaseAlerta(8, "Corte Electrico", R.drawable.flash, "icon_corte");

    ArrayList<Alerta> listAlertasMostradas = new ArrayList<>();

    ArrayList<DatosAlerta> listDatosAlertas = new ArrayList<>();
    ArrayList<ClaseAlerta> listClasesAlertas = new ArrayList<>();
    ArrayList<Confirmacion> listConfirmaciones = new ArrayList<>();
    ArrayList<Reporte> listReportes = new ArrayList<>();
    ArrayList<Comentario> listComentarios = new ArrayList<>();
    ArrayList<Imagen> listImagenes = new ArrayList<>();

    Alerta alertaSeleccionada;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InstanciateMapbox();
        setContentView(R.layout.activity_main);
        InitializeVariables();
        InitializeLocation();
        GetPermissions();
        InitializeUI();
        InitializeMapbox(savedInstanceState);

        actualizadorAlertas.postDelayed(actualizacion, 5000);
    }

    String accessToken = "pk.eyJ1Ijoic3BhcnRhbmNhdDciLCJhIjoiY2p2ZzVkOWRrMDQ1ejQxcmc2bjgxc3JtYSJ9.Nn4-Xa4AaeoVe3p3z67I7g";

    void InstanciateMapbox(){
        Mapbox.getInstance(this, accessToken);
    }

    void InitializeVariables() {
        listClasesAlertas.add(claseAccidente);
        listClasesAlertas.add(claseIncendio);
        listClasesAlertas.add(claseHerido);
        listClasesAlertas.add(claseBloqueo);
        listClasesAlertas.add(claseCongestionamiento);
        listClasesAlertas.add(claseMarchas);
        listClasesAlertas.add(claseCalleDanada);
        listClasesAlertas.add(claseCorte);
    }

    LocationManager locationManager;
    LocationListener locationListener;

    public Location location;

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
            return;
        }
        else {
            Toast.makeText(this, "Localizacion Inicializada", Toast.LENGTH_SHORT).show();
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 100, locationListener);
        }

    }
    void GetPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            int requestResponse = 0;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    requestResponse);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            int requestResponse = 0;
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    requestResponse);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length == 0) {
            Toast.makeText(getApplicationContext(), "PERMISOS DENEGADOS!", Toast.LENGTH_SHORT).show();
            GetPermissions();
        }
        else {
            InitializeLocation();
        }
    }

    MapView mapView;
    Button btnAlertMenu;
    LinearLayout layoutAlertMenu;
    LinearLayout
            btnAlertAccidente,
            BtnAlertIncendio,
            BtnAlertHerido,
            BtnAlertBloqueo,
            BtnAlertCongestionamiento,
            BtnAlertMarchas,
            BtnAlertCalleDanada,
            BtnAlertCorte;
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
    CheckBox chkIncluidePic;
    ImageView imgImagenAlerta;

    void InitializeUI() {
        mapView = findViewById(R.id.mapView);

        btnAlertMenu = findViewById(R.id.btnAlertMenu);
        layoutAlertMenu = findViewById(R.id.layoutAlertMenu);

        btnAlertMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutAlertMenu.getVisibility() == View.VISIBLE) {
                    layoutAlertMenu.setVisibility(View.GONE);
                } else if (layoutAlertMenu.getVisibility() == View.GONE) {
                    layoutAlertMenu.setVisibility(View.VISIBLE);
                }
            }
        });

        btnAlertAccidente = findViewById(R.id.btnAccidente);
        BtnAlertIncendio = findViewById(R.id.btnIncendio);
        BtnAlertHerido = findViewById(R.id.btnHerido);
        BtnAlertBloqueo = findViewById(R.id.btnBloqueo);
        BtnAlertCongestionamiento = findViewById(R.id.btnCongestionamiento);
        BtnAlertMarchas = findViewById(R.id.btnMarchas);
        BtnAlertCalleDanada = findViewById(R.id.btnCalleDanada);
        BtnAlertCorte = findViewById(R.id.btnCorte);

        View.OnClickListener alertBtnListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();
                switch (id){
                    case R.id.btnAccidente:
                        EnviarNuevaAlerta(claseAccidente);
                        break;
                    case R.id.btnIncendio:
                        EnviarNuevaAlerta(claseIncendio);
                        break;
                    case R.id.btnHerido:
                        EnviarNuevaAlerta(claseHerido);
                        break;
                    case R.id.btnBloqueo:
                        EnviarNuevaAlerta(claseBloqueo);
                        break;
                    case R.id.btnCongestionamiento:
                        EnviarNuevaAlerta(claseCongestionamiento);
                        break;
                    case R.id.btnMarchas:
                        EnviarNuevaAlerta(claseMarchas);
                        break;
                    case R.id.btnCalleDanada:
                        EnviarNuevaAlerta(claseCalleDanada);
                        break;
                    case R.id.btnCorte:
                        EnviarNuevaAlerta(claseCorte);
                        break;
                }
                Log.v( "RPAG-Log","Envio exitoso");
                layoutAlertMenu.setVisibility(View.GONE);
                actualizarListaAlertas();
                mostrarAlertas();
            }

        };

        btnAlertAccidente.setOnClickListener(alertBtnListener);
        BtnAlertIncendio.setOnClickListener(alertBtnListener);
        BtnAlertHerido.setOnClickListener(alertBtnListener);
        BtnAlertBloqueo.setOnClickListener(alertBtnListener);
        BtnAlertCongestionamiento.setOnClickListener(alertBtnListener);
        BtnAlertMarchas.setOnClickListener(alertBtnListener);
        BtnAlertCalleDanada.setOnClickListener(alertBtnListener);
        BtnAlertCorte.setOnClickListener(alertBtnListener);

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

        chkIncluidePic = findViewById(R.id.chkIncluidePic);
        imgImagenAlerta = findViewById(R.id.imgImagenAlerta);
    }



    MapboxMap myMapboxMap;
    SymbolManager symbolManager;

    void InitializeMapbox(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull MapboxMap mapboxMap) {
                myMapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.LIGHT, new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        // Map is set up and the style has loaded. Now you can add data or make other map adjustments
                        symbolManager = new SymbolManager(mapView, myMapboxMap, style);
                        symbolManager.addClickListener(new OnSymbolClickListener() {
                            @Override
                            public void onAnnotationClick(Symbol symbol) {
                                Alerta alerta = getAlertaBySymbol(symbol);
                                //Toast.makeText(getApplicationContext(),alerta.claseAlerta.name, Toast.LENGTH_SHORT).show();
                                AbrirMenuAlerta(alerta);

                            }
                        });

                        for (int i = 0; i < listClasesAlertas.size(); i++) {
                            style.addImage(
                                    listClasesAlertas.get(i).icon_name,
                                    BitmapFactory.decodeResource(
                                            getResources(),
                                            listClasesAlertas.get(i).icon
                                    )
                            );
                        }

                        actualizarListaAlertas();
                        mostrarAlertas();
                    }
                });

            }
        });
    }

    void createAlert(int id, ClaseAlerta clase, double lat, double len, Date fecha) {

        Float[] offset = {0f, 2.5f};
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(lat,len))
                .withIconImage(clase.icon_name)
                .withIconSize(0.10f)
                .withIconOffset(offset));

        Alerta alerta = new Alerta(id,lat,len, fecha,symbol,clase);
        listAlertasMostradas.add(alerta);
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

        txtTipoAlerta.setText("Tipo: " + alerta.claseAlerta.name);
        txtConfirmaciones.setText("Confirmaciones: " + contador_confirmaciones);
        txtReportes.setText("Reportes: " + contador_reportes);
        txtLatitud.setText("Latitud: " + alerta.lat);
        txtLongitud.setText("Longitud: " + alerta.len);

        imgImagenAlerta.setImageBitmap(null);
        for (int i=0; i<listImagenes.size(); i++){
            Log.v( "RPAG-Log","Comparando alerta " + alertaSeleccionada.id + " y imagen de " + listImagenes.get(i).id_alerta);
            if (listImagenes.get(i).id_alerta == alertaSeleccionada.id){
                Log.v( "RPAG-Log","Imagen encontrada");

                String path = getCacheDir() + "/TempPics";
                File dir = new File(path);
                if(dir.mkdirs()){
                    Log.v( "RPAG-Log","Directorio en cache creado");
                }

                File file = new File(path, listImagenes.get(i).nombre);
                Log.v( "RPAG-Log","Temp file dir: " + file.getAbsolutePath());
                try {
                    if(!file.exists()){
                        file.createNewFile();
                        Log.v( "RPAG-Log","Archivo creado");
                        FileUtils.writeByteArrayToFile(file,listImagenes.get(i).bitmap);
                        Log.v( "RPAG-Log","Archivo escrito");
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
    }

    void EnviarNuevaAlerta(ClaseAlerta claseAlerta) {

        DatosAlerta datosAlerta = new DatosAlerta(0,ID_Usuario,location.getLatitude(),location.getLongitude(),claseAlerta.id,new Date());

        if(chkIncluidePic.isChecked()){
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
    }

    void actualizarListaAlertas() {
        Log.v( "RPAG-Log","actualizarListaAlertas()");
        ActualizarAlertas actualizarAlertas = new ActualizarAlertas(IP_SERVIDOR, PORT_SERVIDOR, this);
        try {
            actualizarAlertas.join(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        mostrarAlertas();
    }

    Handler actualizadorAlertas = new Handler();
    Runnable actualizacion = new Runnable() {
        @Override
        public void run() {
            actualizarListaAlertas();
            actualizadorAlertas.postDelayed(this, 30000);
        }
    };

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

            createAlert(id, claseAlerta, latitud, longitud, fecha);
        }
    }

    DatosAlerta datosAlerta_EnvioImagen;
    ClaseAlerta claseAlerta_EnvioImagen;
    void EnviarNuevaAlertaConImagen(DatosAlerta datosAlerta){

        datosAlerta_EnvioImagen = datosAlerta;
        claseAlerta_EnvioImagen = getClase(datosAlerta.clase_id);
        Log.v( "RPAG-Log","datosAlerta.id = " + datosAlerta_EnvioImagen.id);
        Log.v( "RPAG-Log","claseAlerta.name = " + claseAlerta_EnvioImagen.name);
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

    @Override
    public void onBackPressed() {
        if(layoutAlertOptions.getVisibility() == View.VISIBLE){
            layoutAlertOptions.setVisibility(View.GONE);
        }
        else if(layoutAlertMenu.getVisibility() == View.VISIBLE) {
            layoutAlertMenu.setVisibility(View.GONE);
        }
        else {
            super.onBackPressed();
        }
    }

    ClaseAlerta getClase(int clase_id) {
        for (int i = 0; i < listClasesAlertas.size(); i++) {
            if (clase_id == listClasesAlertas.get(i).id) {
                return listClasesAlertas.get(i);
            }
        }
        return null;
    }

    static File GuardarBitmapComoJPG(Bitmap bitmap, String path, String filename){

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
            Log.v( "RPAG-Log","Enviando claseAlerta.name = " + claseAlerta_EnvioImagen.name);

            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Log.v("RPAG-Log", "Bitmap capturado: " + photo.getByteCount() + " bytes");

            String nombre_imagen = new Date().getTime() + "_" + claseAlerta_EnvioImagen.name + ".jpg";
            File imgFile = GuardarBitmapComoJPG(photo,Environment.getExternalStorageDirectory().getAbsolutePath() + "/RPAG_Pics",nombre_imagen);

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

}
