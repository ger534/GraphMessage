package com.example.root.freeex;
import android.net.ParseException;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

/**
 * Created by root on 22/11/15.
 */
public class EntrantesJson {

    public static void Read(String Msg){
        System.out.println("Recibido en el json reader" +Msg);
        try{
            String json = Msg;
            JSONObject reader = new JSONObject(json);
            String Type = (String) reader.getString("Type");
            EntrantesJson toType = new EntrantesJson();
            toType.Action(Type, reader);

        }catch(ParseException e){
            System.out.println("Error en type no type type");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void Action(String action, JSONObject json){

        SalientesJson jsonRe = new SalientesJson();

        if(action.equals("newConection")){

        }

        if(action.equals("Difusion")){

        }

        if(action.equals("Desconexion")){

        }

        if(action.equals("1to1")){

        }

        if(action.equals("NoName")){

        }

        if(action.equals("RespuestaConection")){

        }

    }
}
