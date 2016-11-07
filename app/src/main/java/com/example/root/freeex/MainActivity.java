package com.example.root.freeex;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.LocationManager;
import android.net.wifi.WifiManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.app.Activity;

public class MainActivity extends AppCompatActivity {

    private ImageView ImagenBlue, ImagenWifi;
    BluetoothAdapter adaptador_bluetooth;
    WifiManager administrador_wifi;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ImagenBlue = (ImageView) findViewById(R.id.imageView);
        adaptador_bluetooth= BluetoothAdapter.getDefaultAdapter();
        if(adaptador_bluetooth==null){
            ImagenBlue.setVisibility(View.GONE);
        }
        else{
            setImagenBluetooth(adaptador_bluetooth.isEnabled());
        }
        ImagenWifi = (ImageView) findViewById(R.id.imageView2);
        administrador_wifi= (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
        setImagenWifi(administrador_wifi.isWifiEnabled());
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, Cluster.class));
            }
        });
    }
    public void clickImagen(View view){
        switch (view.getId()){
            case R.id.imageView2:
                setEstadoWifi();
                break;
            case R.id.imageView:
                setEstadoBluetooth();
                break;
        }

    }
    public void setImagenWifi(boolean valor){
        if (valor) ImagenWifi.setImageResource(R.drawable.wifiacti);
        else ImagenWifi.setImageResource(R.drawable.wifidesa);
    }
    public void setImagenBluetooth(boolean valor){
        if (valor) ImagenBlue.setImageResource(R.drawable.blueacti);
        else ImagenBlue.setImageResource(R.drawable.bluedesa);
    }
    public void setEstadoWifi(){
        setImagenWifi(!administrador_wifi.isWifiEnabled());
        administrador_wifi.setWifiEnabled(!administrador_wifi.isWifiEnabled());
    }

    public void setEstadoBluetooth(){
        if (adaptador_bluetooth.isEnabled()){
            setImagenBluetooth(false);
            adaptador_bluetooth.disable();
        }
        else{
            setImagenBluetooth(true);
            adaptador_bluetooth.enable();
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
