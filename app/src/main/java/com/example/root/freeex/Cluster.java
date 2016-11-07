package com.example.root.freeex;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView;
import java.util.ArrayList;
import java.util.Set;
import android.os.Handler;
import android.os.Message;

import android.app.AlertDialog;
public class Cluster extends AppCompatActivity {

    private BluetoothService servicio;
    private BluetoothDevice		ultimoDispositivo;
    private ListView lvDispositivos;
    private static final int 	REQUEST_ENABLE_BT 	= 1;
    private BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
    Ubication ubication;
    private ArrayList<BluetoothDevice> arrayDevices;
    private ArrayAdapter arrayAdapter;
    private int lastRssi;
    private EditText texto;
    private String mensaje;
    private TextView TextoMensaje;
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver()
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            final String action = intent.getAction();
            if (BluetoothDevice.ACTION_FOUND.equals(action))
            {
                // Acciones a realizar al descubrir un nuevo dispositivo
                // Si el array no ha sido aun inicializado, lo instanciamos
                if(arrayDevices == null)
                    arrayDevices = new ArrayList<BluetoothDevice>();
                BluetoothDevice dispositivo = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                arrayDevices.add(dispositivo);
                String descripcionDispositivo = dispositivo.getName() + " [" + dispositivo.getAddress() + "]";
                Toast.makeText(getBaseContext(), "Detectado Dispositivo" + ": " + descripcionDispositivo, Toast.LENGTH_SHORT).show();
                short rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                lastRssi = (int) intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
                System.out.println(lastRssi);
                System.out.println(rssi);
                //int TxPower = findCodeInBuffer(scanRecord, AssignedNumbers.TXPOWER);

            }

            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action))
            {
                // Acciones a realizar al finalizar el proceso de descubrimiento
                arrayAdapter = new BluetoothDeviceArrayAdapter(getBaseContext(), android.R.layout.simple_list_item_2, arrayDevices);
                lvDispositivos = (ListView)findViewById(R.id.lvDispositivos);
                lvDispositivos.setAdapter(arrayAdapter);
                Toast.makeText(getBaseContext(), "Fin Busqueda", Toast.LENGTH_SHORT).show();
                configurarListaDispositivos();
            }
        }
    };
    // Handler que obtendrá informacion de BluetoothService
    private final Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg)
        {
            byte[] buffer 	= null;
            String mensaje 	= null;
            TextoMensaje= (TextView) findViewById(R.id.texto);
            // Atendemos al tipo de mensaje
            switch(msg.what)
            {

                // Mensaje de lectura: se mostrara en el TextView
                case BluetoothService.MSG_LEER:
                {
                    buffer = (byte[])msg.obj;
                    mensaje = new String(buffer, 0, msg.arg1);

                    System.out.println("ESTE ES EL MENSAJE: "+mensaje);

                    TextoMensaje.append("\n"+mensaje);

                    //tvMensaje.setText(mensaje);



                    break;
                }

                // Mensaje de escritura: se mostrara en el Toast
                case BluetoothService.MSG_ESCRIBIR:
                {
                    buffer = (byte[])msg.obj;
                    mensaje = new String(buffer);

                    mensaje = "EnviandoMensaje" + ": " + mensaje;

                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                    break;
                }

                // Mensaje de cambio de estado
                case BluetoothService.MSG_CAMBIO_ESTADO:
                {
                    switch(msg.arg1)
                    {
                        case BluetoothService.ESTADO_ATENDIENDO_PETICIONES:
                            break;

                        // CONECTADO: Se muestra el dispositivo al que se ha conectado y se activa el boton de enviar
                        case BluetoothService.ESTADO_CONECTADO:
                        {
                            mensaje = "Conexion Actual" + " " + servicio.getNombreDispositivo();
                            Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                            //tvConexion.setText(mensaje);
                            //btnEnviar.setEnabled(true);
                            //AQUI SE ENVIA COORDENADAS
                            servicio.enviar(ubication.Coordenadas().toString().getBytes());
                            System.out.println(ubication.Coordenadas()[0]);
                            break;

                            //btnEnviar.setEnabled(true);

                        }

                        // REALIZANDO CONEXION: Se muestra el dispositivo al que se esta conectando
                        case BluetoothService.ESTADO_REALIZANDO_CONEXION:
                        {
                            mensaje = "Conectando A" + " " + ultimoDispositivo.getName() + " [" + ultimoDispositivo.getAddress() + "]";
                            Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();



                            //btnEnviar.setEnabled(false);


                            break;
                        }

                        // NINGUNO: Mensaje por defecto. Desactivacion del boton de enviar
                        case BluetoothService.ESTADO_NINGUNO:
                        {
                            mensaje = "Sin Conexion";
                            Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();

                            //tvConexion.setText(mensaje);
                            //btnEnviar.setEnabled(false);

                            break;
                        }
                        default:
                            break;
                    }
                    break;
                }

                // Mensaje de alerta: se mostrara en el Toast
                case BluetoothService.MSG_ALERTA:
                {
                    mensaje = "ALERTA";
                    Toast.makeText(getApplicationContext(), mensaje, Toast.LENGTH_SHORT).show();
                    break;
                }

                default:
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cluster);
        ubication = new Ubication(getApplicationContext());
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        servicio = new BluetoothService(this, handler, bAdapter);

    }
    private void registrarEventosBluetooth()
    {
        // Registramos el BroadcastReceiver que instanciamos previamente para
        // detectar los distintos eventos que queremos recibir
        IntentFilter filtro = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filtro.addAction(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(bluetoothReceiver, filtro);
    }
    private void configurarListaDispositivos()
    {

        lvDispositivos.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView adapter, View view, int position, long arg) {
                // El ListView tiene un adaptador de tipo BluetoothDeviceArrayAdapter.
                // Invocamos el metodo getItem() del adaptador para recibir el dispositivo
                // bluetooth y realizar la conexion.
                BluetoothDevice dispositivo = (BluetoothDevice) lvDispositivos.getAdapter().getItem(position);

                AlertDialog dialog = crearDialogoConexion("Conectar",
                        "Desea conectarse al dispositivo" + " " + dispositivo.getName() + "?",
                        dispositivo.getAddress());


                System.out.println("desea");


                dialog.show();
            }
        });
    }
    private AlertDialog crearDialogoConexion(String titulo, String mensaje, final String direccion)
    {
        // Instanciamos un nuevo AlertDialog Builder y le asociamos titulo y mensaje
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(titulo);
        alertDialogBuilder.setMessage(mensaje);

        // Creamos un nuevo OnClickListener para el boton OK que realice la conexion
        DialogInterface.OnClickListener listenerOk = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                conectarDispositivo(direccion);


                System.out.println("hola");


            }
        };

        // Creamos un nuevo OnClickListener para el boton Cancelar
        DialogInterface.OnClickListener listenerCancelar = new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                return;
            }
        };

        // Asignamos los botones positivo y negativo a sus respectivos listeners
        alertDialogBuilder.setPositiveButton("Conectar", listenerOk);
        alertDialogBuilder.setNegativeButton("Cancelar", listenerCancelar);

        return alertDialogBuilder.create();
    }

    public void conectarDispositivo(String direccion) {

        System.out.println("conectar antes de todo");

        Toast.makeText(this, "Conectando a " + direccion, Toast.LENGTH_LONG).show();

        BluetoothDevice dispositivoRemoto = bAdapter.getRemoteDevice(direccion);

        servicio.solicitarConexion(dispositivoRemoto);

        this.ultimoDispositivo = dispositivoRemoto;

    }

    public void clickButton(View button){
        texto = (EditText) findViewById(R.id.msg);
        switch (button.getId()){
            case R.id.bSend:
                servicio.enviar(texto.getText().toString().getBytes());
                texto.setText("");
            //    Chat chat = new Chat(servicio);
                break;
            case R.id.button2:
                servicio.iniciarServicio();
                Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
                startActivity(discoverableIntent);
                break;

            case R.id.button3:
                registrarEventosBluetooth();
                if(arrayDevices != null)
                    arrayDevices.clear();
                // Comprobamos si existe un descubrimiento en curso. En caso afirmativo, se
                // cancela.
                if(bAdapter.isDiscovering())
                    bAdapter.cancelDiscovery();
                // Iniciamos la busqueda de dispositivos
                if(bAdapter.startDiscovery())
                    // Mostramos el mensaje de que el proceso ha comenzado
                    //registrarEventosBluetooth();
                    Toast.makeText(this, "Iniciando Descubrimiento", Toast.LENGTH_SHORT).show();
                    //registrarEventosBluetooth();
                else
                    Toast.makeText(this, "Error Iniciando Descubrimiento", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cluster, menu);
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
    /**
     * Handler del evento desencadenado al retornar de una actividad. En este caso, se utiliza
     * para comprobar el valor de retorno al lanzar la actividad que activara el Bluetooth.
     * En caso de que el usuario acepte, resultCode sera RESULT_OK
     * En caso de que el usuario no acepte, resultCode valdra RESULT_CANCELED
     */
    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data) {
        switch(requestCode)
        {
            case REQUEST_ENABLE_BT:
            {
                if(resultCode == RESULT_OK)
                {
                    if(servicio != null)
                    {
                        servicio.finalizarServicio();
                        servicio.iniciarServicio();
                    }
                    else
                        servicio = new BluetoothService(this, handler, bAdapter);
                }
                break;
            }
            default:
                break;
        }
    }
}
