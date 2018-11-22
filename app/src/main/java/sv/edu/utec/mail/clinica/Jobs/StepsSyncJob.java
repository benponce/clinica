package sv.edu.utec.mail.clinica.Jobs;

import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.toolbox.RequestFuture;
import com.android.volley.toolbox.StringRequest;
import com.evernote.android.job.Job;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import sv.edu.utec.mail.clinica.Red.ClienteRest;

import static sv.edu.utec.mail.clinica.AppControl.Control.sysUsr;
import static sv.edu.utec.mail.clinica.AppControl.Control.usrPasosHoy;

public class StepsSyncJob extends Job {
    public static final String TAG = "steps_sync_tag";

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        return subirPasos() ? Result.SUCCESS : Result.FAILURE;
    }

    private boolean subirPasos() {
        String url = ClienteRest.getRegistroVitalesUrl();

        RequestFuture<String> future = RequestFuture.newFuture();
        StringRequest stringRequest = new StringRequest(Request.Method.POST, url, future, future) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("unidad", "Pasos");
                params.put("codigo_pac", String.valueOf(sysUsr.paciente));
                params.put("codigo_vitales", "7");
                params.put("fecha", new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime()));
                params.put("valor", String.valueOf(usrPasosHoy.valor));
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("Content-Type", "application/x-www-form-urlencoded");
                return params;
            }
        };
        ClienteRest.getInstance(getContext()).addToRequestQueue(stringRequest);
        try {
            String r = future.get();
            Log.d("Guardados", r.substring(0));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

}
