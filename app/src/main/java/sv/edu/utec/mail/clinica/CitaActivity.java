package sv.edu.utec.mail.clinica;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;
import com.google.gson.Gson;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Fragments.CitaDialogFragment;
import sv.edu.utec.mail.clinica.POJO.Citas;
import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.Red.ClienteRest;

public class CitaActivity extends AppCompatActivity implements CitaDialogFragment.CitaDlgListener {

    CalendarView mCalendario;
    Usuario usr;
    Citas[] arrCitas;
    Citas mCitaSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cita);
        usr = Control.getUsuario(this);
        mCalendario = findViewById(R.id.calendarView);
        Calendar min = Calendar.getInstance();
        min.add(Calendar.MONTH, -1);
        Calendar max = Calendar.getInstance();
        max.add(Calendar.MONTH, 12);
        mCalendario.setMinimumDate(min);
        mCalendario.setMaximumDate(max);
        mCalendario.setOnDayClickListener(new OnDayClickListener() {
            @Override
            public void onDayClick(EventDay eventDay) {
                verCita(eventDay);
            }
        });
        arrCitas = Control.getCitas(this);
        if (arrCitas != null) {
            colocarCitas(arrCitas);
        }
        descargarCitas();
    }

    private void descargarCitas() {

        //http://www.zoftino.com/how-to-read-and-write-calendar-data-in-android
        String url = ClienteRest.getCitasUrl() + usr.paciente;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        try {
                            String str_JSON = response.getJSONArray("items").toString();
                            if (!str_JSON.equals("[]")) {
                                arrCitas = gson.fromJson(str_JSON, Citas[].class);
                                mCalendario.setEvents(colocarCitas(arrCitas));
                                Control.escribirCitas(CitaActivity.this, str_JSON);
                            } else {
                                Toast.makeText(CitaActivity.this, "No tiene Citas programadas.", Toast.LENGTH_LONG).show();
                            }
                        } catch (Exception e) {
                            Toast.makeText(CitaActivity.this, "No tiene registro de citas.", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(CitaActivity.this, "No tiene registro de citas.", Toast.LENGTH_LONG).show();
                    }
                });
        ClienteRest.getInstance(CitaActivity.this).addToRequestQueue(jsonObjectRequest);
    }

    private List<EventDay> colocarCitas(Citas[] citas) {
        List<EventDay> events = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        Calendar[] cal = new Calendar[citas.length];
        int i = 0;
        for (Citas cita : citas) {
            try {
                cal[i] = Calendar.getInstance();
                cal[i].setTime(df.parse(cita.fecha));
                events.add(new EventDay(cal[i], R.drawable.ic_citas_marca));
            } catch (Exception e) {
                Log.d("Agregar eventos", e.getMessage());
            }
            i++;
        }
        return events;
    }

    public void verCita(EventDay eventDay) {
        Calendar cal = eventDay.getCalendar();
        //El calendario se recibe con defase de un mes
        cal.add(Calendar.MONTH, 1);
        String strFecha = cal.get(Calendar.YEAR) + "" + cal.get(Calendar.MONTH) + "" + cal.get(Calendar.DATE);
        for (Citas cita : arrCitas) {
            if (strFecha.equals(cita.fecha)) {
                mCitaSelected = cita;
                CitaDialogFragment citaDlg = new CitaDialogFragment();
                citaDlg.show(getSupportFragmentManager(), "citaDlg");
                break;
            }
        }
    }

    @Override
    public Citas getCitaSelected() {
        return mCitaSelected;
    }
}
