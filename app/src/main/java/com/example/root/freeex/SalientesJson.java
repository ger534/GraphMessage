package com.example.root.freeex;

import android.net.ParseException;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by root on 21/11/15.
 */
public class SalientesJson {
    JSONObject json = new JSONObject();

    /*public String Conexion(String NombreNodo, int Distancia) throws JSONException {
        json.put("Type", "newConection");
        json.put("Name", NombreNodo);
        //json.put("distancia", );
        return json.toString();

    }*/
    public String Conexion(String NombreNodo) throws JSONException {
        json.put("Type", "newConection");
        json.put("Name", NombreNodo);
        //json.put("distancia", );
        return json.toString();

    }

    public String RespuestaNoNombre() throws JSONException {
        json.put("Type", "NoName");
        return json.toString();
    }

    public String RespuestaConNombre() throws JSONException {
        json.put("Type", "RespuestaConection");
        //json.put("Grafo", no sé);
        return json.toString();
    }

    public String Difusion(int NumeroMensaje, int Tiempo, String Mensaje) throws JSONException{
        json.put("Type", "Difusion");
        json.put("NumeroMensaje", NumeroMensaje);
        json.put("Time", Tiempo);
        json.put("Msg", Mensaje);
        return json.toString();
    }

    public String OneToOne(String NombreNodo,int NumeroMensaje, int Tiempo, String Mensaje) throws JSONException{
        json.put("Type", "1to1");
        json.put("Name", NombreNodo);
        json.put("NumeroMensaje", NumeroMensaje);
        json.put("Time", Tiempo);
        json.put("Msg", Mensaje);
        return json.toString();
    }
}
