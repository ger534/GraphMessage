package com.example.root.freeex;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class Chat extends Cluster {

    private final BluetoothService servicio;
    private EditText texto;
    private String msg;

    public Chat(BluetoothService servicio) {
        this.servicio = servicio;
        Cluster.class.getResource("servicios");
    }

    public void clickButton(View button){
        texto = (EditText) findViewById(R.id.msg);
        switch (button.getId()){
            //case R.id.Chat:
            //    Chat chat = new Chat(servicio);

            case R.id.bSend:
                msg = texto.getText().toString();//.getBytes();
                texto.setText("");
                servicio.enviar(msg.getBytes());
                break;

        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        findViewById(R.id.bSend).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Chat.this, Nombre.class));
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
