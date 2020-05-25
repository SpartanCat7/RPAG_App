package com.example.rpagv2;

import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import static com.mapbox.mapboxsdk.Mapbox.getApplicationContext;

public class MyLocationListener implements LocationListener {

    public Location location;
    final static String ACTION_LOCATION_UPDATE = "ACTION_LOCATION_UPDATE";
    final static String LATITUDE_TAGNAME = "LATITUDE_TAGNAME";
    final static String LONGITUDE_TAGNAME = "LONGITUDE_TAGNAME";

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(getApplicationContext(), "Localizacion actualizada", Toast.LENGTH_SHORT).show();
        Log.i("RPAG-Log", "Location Updated: " + location.getLongitude() + " - " + location.getLatitude());
        this.location = location;
        Intent locationUpdateBroadcastIntent = new Intent();
        locationUpdateBroadcastIntent.setAction(ACTION_LOCATION_UPDATE);
        locationUpdateBroadcastIntent.putExtra(LATITUDE_TAGNAME, location.getLatitude());
        locationUpdateBroadcastIntent.putExtra(LONGITUDE_TAGNAME, location.getLongitude());
        getApplicationContext().sendBroadcast(locationUpdateBroadcastIntent);
        Log.i("RPAG-Log", "Location Broadcast Sent");
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
