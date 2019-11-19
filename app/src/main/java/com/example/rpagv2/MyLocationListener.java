package com.example.rpagv2;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.widget.Toast;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class MyLocationListener implements LocationListener {

    MainActivity main;

    public MyLocationListener(MainActivity main) {

        this.main = main;
    }

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "Localizacion actualizada", Toast.LENGTH_SHORT).show();
        main.location = location;
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
        main.location = null;
    }
}
