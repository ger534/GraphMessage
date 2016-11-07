package com.example.root.freeex;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.widget.Toast;


/**
 * Created by root on 14/11/15.
 */
public class Ubication extends Service implements android.location.LocationListener {
    LocationManager locationManager;
    android.location.LocationListener listener;
    double longitude;
    double latitude;
    private final Context ctx;
    String provider;
    boolean gpsActivo;
    Location location;

    public Ubication() {
        super();
        this.ctx = this.getApplicationContext();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public Ubication(Context c) {
        super();
        this.ctx = c;
        getLocation();
    }

    public double[] Coordenadas() {
        double[] coordenadas = new double[2];
        coordenadas[0] = latitude;
        coordenadas[1] = longitude;
        return coordenadas;
    }

    public void getLocation() {
        locationManager = (LocationManager) this.ctx.getSystemService(Context.LOCATION_SERVICE);
        provider = "network";
        if (provider != null && !provider.equals("")) {
            // Get the location from the given provider
            Location location = locationManager.getLastKnownLocation(provider);
            locationManager.requestLocationUpdates(provider, 20000, 1, this);
            if(location!= null)
                onLocationChanged(location);
            else
                Toast.makeText(getBaseContext(), "Location can't be retrieved", Toast.LENGTH_SHORT).show();

        }else{
            Toast.makeText(getBaseContext(), "No Provider Found", Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    public void onLocationChanged(Location location) {
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }
}
