package sv.edu.utec.mail.clinica.Services;

import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import sv.edu.utec.mail.clinica.POJO.Lectura;
import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.Red.ClienteRest;

public class StepSyncService extends JobService {
    Usuario mUsuario;
    Lectura mPasos;

    public StepSyncService() {
    }

    @Override
    public boolean onStartJob(final JobParameters params) {
        ((JobScheduler) this.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancelAll();
        String url = ClienteRest.getRegistroVitalesUrl();
        leerConteo();
        if (mUsuario != null && mPasos != null) {

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
                    params.put("fecha", mPasos.fecha);
                    params.put("valor", String.valueOf(mPasos.valor));
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

    private void leerConteo() {
        Gson gson = new Gson();
        SharedPreferences sp = getSharedPreferences("clinica", 0);
        mPasos = gson.fromJson(sp.getString("PasosHoy", ""), Lectura.class);
        mUsuario = gson.fromJson(sp.getString("Usuario", ""), Usuario.class);
    }

    private void reiniciarConteo() {
        SharedPreferences sp = getSharedPreferences("clinica", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("PasosHoy", "");
        editor.commit();
        //
        String fechaHoy = new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime());
        if (!fechaHoy.equals(mPasos.fecha)) {
            editor.putString("SubidaProgramada", "0");
        } else {
            editor.putString("SubidaProgramada", "2");
        }
        editor.commit();
    }


}
