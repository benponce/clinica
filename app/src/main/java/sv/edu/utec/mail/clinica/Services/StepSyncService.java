package sv.edu.utec.mail.clinica.Services;

import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.Red.ClienteRest;

public class StepSyncService extends JobService {
    Usuario mUsuario;
    int mPasos;
    String mFecha;

    public StepSyncService() {
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        mFecha = params.getExtras().getString("fecha");
        mPasos = params.getExtras().getInt("pasos");
        ((JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancelAll();
        String url = ClienteRest.getRegistroVitalesUrl();
        leerUsuario();
        if (mUsuario != null) {

            StringRequest stringRequest = new StringRequest
                    (Request.Method.POST, url, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            jobFinished(params, false);
                            reiniciarConteo();
                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            jobFinished(params, true);
                        }
                    }) {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("unidad", "Pasos");
                    params.put("codigo_pac", String.valueOf(mUsuario.paciente));
                    params.put("codigo_vitales", "7");
                    params.put("fecha", mFecha);
                    params.put("valor", String.valueOf(mPasos));
                    return params;
                }

                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("Content-Type", "application/x-www-form-urlencoded");
                    return params;
                }
            };

            ClienteRest.getInstance(this).addToRequestQueue(stringRequest);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }

    private void leerUsuario() {
        Gson gson = new Gson();
        SharedPreferences sp = getSharedPreferences("clinica", 0);
        mUsuario = gson.fromJson(sp.getString("Usuario", ""), Usuario.class);
    }

    private void reiniciarConteo() {
        Intent intent = new Intent(this, StepCounterService.class);
        intent.setAction(StepCounterService.ACTION_RESET_COUNTER);
        startService(intent);
    }
}
