package sv.edu.utec.mail.clinica;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.events.calendar.views.EventsCalendar;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Fragments.CitaDialogFragment;
import sv.edu.utec.mail.clinica.POJO.Citas;
import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.Red.ClienteRest;


public class CitasActivity extends AppCompatActivity implements EventsCalendar.Callback, CitaDialogFragment.CitaDialogListener {

    EventsCalendar calendario;
    Usuario usr;
    Citas[] arrCitas;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_citas);
        calendario = findViewById(R.id.eventsCalendar);
        usr = Control.getUsuario(this);
        calendario.setToday(Calendar.getInstance());
        descargarCitas();
    }

    private void descargarCitas() {
        String url = ClienteRest.getCitasUrl() + usr.paciente;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        Gson gson = new Gson();
                        try {
                            colocarCitas(gson.fromJson(response.getJSONArray("items").toString(), Citas[].class));
                        } catch (JSONException e) {
                            Toast.makeText(getApplicationContext(), "No tiene registro de citas.", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "No tiene registro de citas.", Toast.LENGTH_LONG).show();
                    }
                });
        ClienteRest.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    //TODO Borrar esta activity, hacer una Tabbed con un Fragment para el calendario y otro para el detalle
    private void colocarCitas(Citas[] citas) {
        arrCitas = citas;
        String msj = "";
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        for (Citas cita : citas) {
            try {
                cal.setTime(df.parse(citas[0].fecha));
                //calendario.addEvent(cal);
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
            msj += cal.get(Calendar.DATE) + "\n";
        }
        Toast.makeText(getApplicationContext(), "Citas " + msj, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDaySelected(Calendar calendar) {
        Toast.makeText(getApplicationContext(), "dia seleccionado", Toast.LENGTH_LONG).show();
        DialogFragment d = new CitaDialogFragment();
        d.show(getSupportFragmentManager(), "dialogo_citas");
    }

    @Override
    public void onMonthChanged(Calendar calendar) {

    }

    @Override
    public void onDialogPositiveClick(DialogFragment dialog) {

    }

    @Override
    public void onDialogNegativeClick(DialogFragment dialog) {

    }
}
