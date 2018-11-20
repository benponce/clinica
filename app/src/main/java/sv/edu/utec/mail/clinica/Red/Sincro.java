package sv.edu.utec.mail.clinica.Red;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.POJO.Citas;
import sv.edu.utec.mail.clinica.POJO.Lectura;

public class Sincro {
    private static Sincro mInstance;
    private static Context ctx;
    private SharedPreferences sp;

    //Constructor privado
    private Sincro(@NonNull Context context) {
        ctx = context;
        sp = context.getSharedPreferences("clinica", 0);
    }

    public static synchronized Sincro getInstance(@NonNull Context context) {
        if (mInstance == null) {
            mInstance = new Sincro(context);
        }
        return mInstance;
    }

    public void downloadPasos() {
        String url = ClienteRest.getPasosUrl() + Control.sysUsr.paciente;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Gson gson = new Gson();
                            String strPasos = response.getJSONArray("items").toString();
                            Control.usrPasos = gson.fromJson(strPasos, Lectura[].class);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("Pasos", strPasos);
                            editor.commit();
                        } catch (Exception e) {
                            Log.d("Descarga de Pasos", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            Gson gson = new Gson();
                            SharedPreferences settings = ctx.getSharedPreferences("clinica", 0);
                            Control.usrPasos = gson.fromJson(settings.getString("Pasos", ""), Lectura[].class);
                        } catch (Exception e) {
                            Log.d("Lectura de Pasos", error.getMessage());
                        }
                        Log.d("Descarga de Pasos", error.getMessage());
                    }
                });
        ClienteRest.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

    public void downloadCitas() {
        String url = ClienteRest.getCitasUrl() + Control.sysUsr.paciente;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        try {
                            String strCitas = response.getJSONArray("items").toString();
                            if (!strCitas.equals("[]")) {
                                Control.usrCitas = gson.fromJson(strCitas, Citas[].class);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putString("Citas", strCitas);
                                editor.commit();
                            }
                        } catch (Exception e) {
                            Log.d("Descarga de Citas", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        try {
                            Gson gson = new Gson();
                            SharedPreferences settings = ctx.getSharedPreferences("clinica", 0);
                            Control.usrCitas = gson.fromJson(settings.getString("Citas", ""), Citas[].class);
                        } catch (Exception e) {
                            Log.d("Lectura de Citas", error.getMessage());
                        }
                        Log.d("Descarga de Citas", error.getMessage());
                    }
                });
        ClienteRest.getInstance(ctx).addToRequestQueue(jsonObjectRequest);
    }

}
