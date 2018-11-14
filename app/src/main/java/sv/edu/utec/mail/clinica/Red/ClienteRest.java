package sv.edu.utec.mail.clinica.Red;


import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

public class ClienteRest {

    private static Context ctx;
    private RequestQueue rqt;
    private static ClienteRest mInstance;
    //URL's
    public static String getLoginUrl(){
        return "https://apex.oracle.com/pls/apex/utec1759102013/CR/login/";
    }
    public static String getPasosUrl() {
        return "https://apex.oracle.com/pls/apex/utec1759102013/CR/Vitales_Pasos/";
    }
    public static String getLecturasUrl(){
        return "https://apex.oracle.com/pls/apex/utec1759102013/CR/Vitales/";
    }

    public static String getCambioPwd() {
        //pass+id
        return "https://apex.oracle.com/pls/apex/utec1759102013/CR/cambiopass/";
    }

    public static String getCitasUrl() {
        return "https://apex.oracle.com/pls/apex/utec1759102013/CR/Citas/";
    }

    public static String getRegistroVitalesUrl() {
        /*
         * unidad, valor, Codigo_vitales, codigo_pac
         */
        return "https://apex.oracle.com/pls/apex/utec1759102013/CR/Citas/";
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
}
