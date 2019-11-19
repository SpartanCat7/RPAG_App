package com.example.rpagv2;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InstanciateMapbox();
        setContentView(R.layout.activity_main);
        InitializeVariables();
        InitializeLocation();
        InitializeUI();
        InitializeMapbox(savedInstanceState);

        actualizadorAlertas.post(actualizacion);
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
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
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

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults.length > 0) {
            InitializeLocation();
        }
        else {
            Toast.makeText(getApplicationContext(), "PERMISOS DENEGADOS!", Toast.LENGTH_SHORT).show();
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
                                Alerta alerta = getAlerta(symbol);
                                Toast.makeText(getApplicationContext(),alerta.claseAlerta.name, Toast.LENGTH_SHORT).show();
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

    void createAlert(int id, ClaseAlerta clase, double lat, double len) {

        Float[] offset = {0f, 2.5f};
        Symbol symbol = symbolManager.create(new SymbolOptions()
                .withLatLng(new LatLng(lat,len))
                .withIconImage(clase.icon_name)
                .withIconSize(0.10f)
                .withIconOffset(offset));

        Alerta alerta = new Alerta(id,lat,len,symbol,clase);
        listAlertasMostradas.add(alerta);
    }


    Alerta getAlerta(Symbol symbol){
        for (int i = 0; i < listAlertasMostradas.size(); i++){
            if (listAlertasMostradas.get(i).symbol == symbol) {
                return listAlertasMostradas.get(i);
            }
        }
        return null;
    }

    void EnviarNuevaAlerta(ClaseAlerta claseAlerta) {
        layoutAlertMenu.setVisibility(View.GONE);

        //Toast.makeText(this, claseAlerta.name, Toast.LENGTH_SHORT).show();
        DatosAlerta datosAlerta = new DatosAlerta(0,1,location.getLatitude(),location.getLongitude(),claseAlerta.id,new Date());

        //String loc = "Longitud: " + datosAlerta.latitud + " / latitud: " + datosAlerta.longitud;
        //Toast.makeText(this, loc, Toast.LENGTH_SHORT).show();

        EnviarAlerta enviarAlerta = new EnviarAlerta(datosAlerta,this,IP_SERVIDOR,PORT_SERVIDOR);
        try {
            enviarAlerta.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    void actualizarListaAlertas() {
        Log.v( "RPAG-Log","actualizarListaAlertas()");
        ActualizarAlertas actualizarAlertas = new ActualizarAlertas(IP_SERVIDOR, PORT_SERVIDOR, this);
        try {
            actualizarAlertas.join(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    Handler actualizadorAlertas = new Handler();
    Runnable actualizacion = new Runnable() {
        @Override
        public void run() {
            actualizarListaAlertas();
            actualizadorAlertas.postDelayed(this, 5000);
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
            ClaseAlerta claseAlerta = getClase(listDatosAlertas.get(i).clase_id);

            createAlert(id, claseAlerta, latitud, longitud);
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
