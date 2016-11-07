package com.example.root.freeex;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Handler;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;

public class BluetoothService {

    private static final String TAG = "BluetoothService";
    private final Handler handler;
    private final Context context;
    private final BluetoothAdapter bAdapter;

    public static final String NOMBRE_SEGURO = "BluetoothServiceSecure";
    public static final String NOMBRE_INSEGURO = "BluetoothServiceInsecure";
    public static UUID UUID_SEGURO;
    public static UUID UUID_INSEGURO;

    public static final int	ESTADO_NINGUNO				= 0;
    public static final int	ESTADO_CONECTADO			= 1;
    public static final int	ESTADO_REALIZANDO_CONEXION	= 2;
    public static final int	ESTADO_ATENDIENDO_PETICIONES= 3;

    public static final int MSG_CAMBIO_ESTADO = 10;
    public static final int MSG_LEER = 11;
    public static final int MSG_ESCRIBIR = 12;
    public static final int MSG_ATENDER_PETICIONES = 13;
    public static final int MSG_ALERTA = 14;

    private int 			estado;
    private HiloServidor	hiloServidor	= null;
    private HiloCliente		hiloCliente		= null;
    private HiloConexion	hiloConexion	= null;

    public BluetoothService(Context context, Handler handler, BluetoothAdapter adapter)
    {
        //debug("BluetoothService()", "Iniciando metodo");

        this.context	= context;
        this.handler 	= handler;
        this.bAdapter 	= adapter;
        this.estado 	= ESTADO_NINGUNO;

        UUID_SEGURO = generarUUID();
        UUID_INSEGURO = generarUUID();
    }

    private synchronized void setEstado(int estado)
    {
        this.estado = estado;
        handler.obtainMessage(MSG_CAMBIO_ESTADO, estado, -1).sendToTarget();
    }

    public synchronized int getEstado()
    {
        return estado;
    }

    public String getNombreDispositivo()
    {
        String nombre = "";
        if(estado == ESTADO_CONECTADO)
        {
            if(hiloConexion != null)
                nombre = hiloConexion.getName();
        }

        return nombre;
    }

    // Inicia el servicio, creando un HiloServidor que se dedicara a atender las peticiones
    // de conexion.
    public synchronized void iniciarServicio()
    {
        //debug("iniciarServicio()", "Iniciando metodo");

        // Si se esta intentando realizar una conexion mediante un hilo cliente,
        // se cancela la conexion
        /*if(hiloCliente != null)
        {
            hiloCliente.cancelarConexion();
            hiloCliente = null;
        }

        // Si existe una conexion previa, se cancela
        if(hiloConexion != null)
        {
            hiloConexion.cancelarConexion();
            hiloConexion = null;
        }

        // Arrancamos el hilo servidor para que empiece a recibir peticiones
        // de conexion
        if(hiloServidor == null)
        {
            hiloServidor = new HiloServidor();
            hiloServidor.start();
        }*/
        hiloServidor = new HiloServidor();
        hiloServidor.start();

    }

    public void finalizarServicio()
    {
        if(hiloCliente != null)
            hiloCliente.cancelarConexion();
        if(hiloConexion != null)
            hiloConexion.cancelarConexion();
        if(hiloServidor != null)
            hiloServidor.cancelarConexion();

        hiloCliente = null;
        hiloConexion = null;
        hiloServidor = null;

        setEstado(ESTADO_NINGUNO);

    }

    // Instancia un hilo conector
    public synchronized void solicitarConexion(BluetoothDevice dispositivo)
    {
        //debug("solicitarConexion()", "Iniciando metodo");
        // Comprobamos si existia un intento de conexion en curso.
        // Si es el caso, se cancela y se vuelve a iniciar el proceso
        /*if(estado == ESTADO_REALIZANDO_CONEXION)
        {
            if(hiloCliente != null)
            {
                hiloCliente.cancelarConexion();
                hiloCliente = null;
            }
        }

        // Si existia una conexion abierta, se cierra y se inicia una nueva
        if(hiloConexion != null)
        {
            hiloConexion.cancelarConexion();
            hiloConexion = null;
        }*/

        // Se instancia un nuevo hilo conector, encargado de solicitar una conexion
        // al servidor, que sera la otra parte.
        hiloCliente = new HiloCliente(dispositivo);
        hiloCliente.start();

        //setEstado(ESTADO_REALIZANDO_CONEXION);
    }

    public synchronized void realizarConexion(BluetoothSocket socket, BluetoothDevice dispositivo)
    {
        hiloConexion = new HiloConexion(socket);
        hiloConexion.start();
    }

    // Sincroniza el objeto con el hilo HiloConexion e invoca a su metodo escribir()
    // para enviar el mensaje a traves del flujo de salida del socket.
    public int enviar(byte[] buffer)
    {
        HiloConexion tmpConexion;

        synchronized(this) {
            if(estado != ESTADO_CONECTADO)
                return -1;
            tmpConexion = hiloConexion;

        }

        tmpConexion.escribir(buffer);

        System.out.println("Se está enviando un mensaje");

        return buffer.length;

    }

    private class HiloServidor extends Thread
    {
        private final BluetoothServerSocket serverSocket;

        public HiloServidor()
        {
            BluetoothServerSocket tmpServerSocket = null;

            try {

                tmpServerSocket = bAdapter.listenUsingRfcommWithServiceRecord(NOMBRE_SEGURO, UUID_SEGURO);

            } catch(IOException e) {
                Log.e(TAG, "HiloServidor(): Error al abrir el socket servidor", e);
            }

            serverSocket = tmpServerSocket;
        }

        public void run()
        {
            BluetoothSocket socket = null;

            setName("HiloServidor");
            setEstado(ESTADO_ATENDIENDO_PETICIONES);

            while(true)
            {
                try {
                    socket = serverSocket.accept();
                }
                catch(IOException e) {
                    Log.e(TAG, "HiloServidor.run(): Error al aceptar conexiones entrantes", e);
                    break;
                }

                // Si el socket tiene valor sera porque un cliente ha solicitado la conexion
                if(socket != null)
                {
                    // Realizamos un lock del objeto
                    synchronized(BluetoothService.this)
                    {
                        switch(estado)
                        {
                            case ESTADO_ATENDIENDO_PETICIONES:
                            case ESTADO_REALIZANDO_CONEXION:
                            {
                                // Estado esperado, se crea el hilo de conexion que recibira
                                // y enviara los mensajes
                                realizarConexion(socket, socket.getRemoteDevice());
                                break;
                            }
                            case ESTADO_NINGUNO:
                            case ESTADO_CONECTADO:
                            {
                                try {
                                    socket.close();
                                }
                                catch(IOException e) {
                                    Log.e(TAG, "HiloServidor.run(): socket.close(). Error al cerrar el socket.", e);
                                }
                                break;
                            }
                            default:
                                break;
                        }
                    }
                }

            }
        }

        public void cancelarConexion()
        {
            try {
                serverSocket.close();
            }
            catch(IOException e) {
                Log.e(TAG, "HiloServidor.cancelarConexion(): Error al cerrar el socket", e);
            }
        }
    }

    private class HiloCliente extends Thread
    {
        private final BluetoothDevice dispositivo;
        private final BluetoothSocket socket;

        public HiloCliente(BluetoothDevice dispositivo)
        {
            BluetoothSocket tmpSocket = null;
            this.dispositivo = dispositivo;

            try {
                tmpSocket = dispositivo.createRfcommSocketToServiceRecord(UUID_SEGURO);
            }
            catch(IOException e) {
                Log.e(TAG, "HiloCliente.HiloCliente(): Error al abrir el socket", e);
            }

            socket = tmpSocket;
        }

        public void run()
        {
            setName("HiloCliente");
            if(bAdapter.isDiscovering())
                bAdapter.cancelDiscovery();

            try {
                socket.connect();
                setEstado(ESTADO_REALIZANDO_CONEXION);
            }
            catch(IOException e) {
                Log.e(TAG, "HiloCliente.run(): socket.connect(): Error realizando la conexion", e);
                try {
                    socket.close();
                }
                catch(IOException inner) {
                    Log.e(TAG, "HiloCliente.run(): Error cerrando el socket", inner);
                }
                setEstado(ESTADO_NINGUNO);
            }

            // Reiniciamos el hilo cliente, ya que no lo necesitaremos mas
            synchronized(BluetoothService.this)
            {
                hiloCliente = null;
            }
            realizarConexion(socket, dispositivo);
        }

        public void cancelarConexion()
        {
            try {
                socket.close();
            }
            catch(IOException e) {
                Log.e(TAG, "HiloCliente.cancelarConexion(): Error al cerrar el socket", e);
            }
            setEstado(ESTADO_NINGUNO);
        }
    }

    private class HiloConexion extends Thread
    {
        private final BluetoothSocket 	socket;
        private final InputStream		inputStream;
        private final OutputStream		outputStream;

        public HiloConexion(BluetoothSocket socket)
        {
            this.socket = socket;

            setName(socket.getRemoteDevice().getName() + " [" + socket.getRemoteDevice().getAddress() + "]");
            InputStream tmpInputStream = null;
            OutputStream tmpOutputStream = null;

            try {
                tmpInputStream = socket.getInputStream();
                tmpOutputStream = socket.getOutputStream();
            }
            catch(IOException e){
                Log.e(TAG, "HiloConexion(): Error al obtener flujos de E/S", e);
            }

            inputStream = tmpInputStream;
            outputStream = tmpOutputStream;
        }
        public void run()
        {
            byte[] buffer = new byte[1024];
            int bytes;
            setEstado(ESTADO_CONECTADO);
            while(true)
            {

                try {
                    bytes = inputStream.read(buffer);
                    handler.obtainMessage(MSG_LEER, bytes, -1, buffer).sendToTarget();
                    sleep(500);
                }
                catch(IOException e) {
                    Log.e(TAG, "HiloConexion.run(): Error al realizar la lectura", e);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void escribir(byte[] buffer) {

            try {
                outputStream.write(buffer);
                handler.obtainMessage(MSG_ESCRIBIR, -1, -1, buffer).sendToTarget();
            }
            catch(IOException e) {
                Log.e(TAG, "HiloConexion.escribir(): Error al realizar la escritura", e);

            }
        }

        public void cancelarConexion()
        {
            try {
                socket.close();
                setEstado(ESTADO_NINGUNO);
            }
            catch(IOException e) {
                Log.e(TAG, "HiloConexion.cerrarConexion(): Error al cerrar la conexion", e);
            }
        }

    }


    private UUID generarUUID()
    {
        ContentResolver appResolver = context.getApplicationContext().getContentResolver();
        String id = Secure.getString(appResolver, Secure.ANDROID_ID);
        final TelephonyManager tManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
        final String deviceId = String.valueOf(tManager.getDeviceId());
        final String simSerialNumber = String.valueOf(tManager.getSimSerialNumber());
        final String androidId	= android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
        UUID uuid = new UUID(androidId.hashCode(), ((long)deviceId.hashCode() << 32) | simSerialNumber.hashCode());
        uuid = new UUID((long)1000, (long)23);
        return uuid;
    }
}