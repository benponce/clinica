package sv.edu.utec.mail.clinica.Red;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.HashMap;
import java.util.Map;

public class ClienteRest {

    private static Context ctx;
    private RequestQueue rqt;
    private static ClienteRest mInstance;

    //URL's
    public static String getLoginUrl() {
        return "https://apex.oracle.com/pls/apex/utec1759102013/CR/login/";
    }

    public static String getPasosUrl() {
        return "https://apex.oracle.com/pls/apex/utec1759102013/CR/Vitales_Pasos/";
    }

    public static String getCambioPwd() {
        //pass+id
        return "https://apex.oracle.com/pls/apex/utec1759102013/CR/cambiopass/";
    }

    public static String getCitasUrl() {
        return "https://apex.oracle.com/pls/apex/utec1759102013/CR/Citas/";
    }

    public static String getRegistroVitalesUrl() {
        //unidad, valor, codigo_vitales, codigo_pac
        return "https://apex.oracle.com/pls/apex/utec1759102013/CR/Vitales";
    }

    public static synchronized ClienteRest getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new ClienteRest(context);
        }
        return mInstance;
    }

    private ClienteRest(Context context) {
        ctx = context;
        rqt = getRequestQueue();
    }

    public RequestQueue getRequestQueue() {
        if (rqt == null) {
            rqt = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return rqt;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public static StringRequest subirDatos(final Context context, int method, String url, final String successMsj, final String errorMsj, final Map<String, String> map) {
        StringRequest stringRequest = new StringRequest
                (method, url, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Toast.makeText(context, successMsj, Toast.LENGTH_LONG).show();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, errorMsj, Toast.LENGTH_LONG).show();
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                return map;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        return stringRequest;
    }

}
