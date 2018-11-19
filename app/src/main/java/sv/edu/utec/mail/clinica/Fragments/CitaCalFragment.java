package sv.edu.utec.mail.clinica.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.events.calendar.views.EventsCalendar;
import com.google.gson.Gson;

import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.POJO.Citas;
import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.R;
import sv.edu.utec.mail.clinica.Red.ClienteRest;

public class CitaCalFragment extends Fragment implements EventsCalendar.Callback {

    EventsCalendar mCalendario;
    Usuario usr;
    Citas[] arrCitas;
    private CitaCalListener mListener;

    public CitaCalFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_cita_cal, container, false);
        mCalendario = v.findViewById(R.id.eventsCalendar);
        usr = Control.getUsuario(getActivity());
        descargarCitas();
        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (CitaCalListener) context;
        } catch (Exception e) {
            Toast.makeText(getActivity(), "Este es el clavo.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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
                            Toast.makeText(getActivity(), "No tiene registro de citas.", Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getActivity(), "No tiene registro de citas.", Toast.LENGTH_LONG).show();
                    }
                });
        ClienteRest.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);
    }


    private void colocarCitas(Citas[] citas) {
        arrCitas = citas;
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        for (Citas cita : citas) {
            try {
                cal.setTime(df.parse(cita.fecha));
                mCalendario.addEvent(cal);
            } catch (Exception e) {
                Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onDaySelected(@Nullable Calendar calendar) {
        //TODO enviar la cita que corresponde al dia seleccionado
        mListener.onFechaSelecionada(new Citas());
    }

    @Override
    public void onMonthChanged(@Nullable Calendar calendar) {

    }

    @Override
    public void onDayLongPressed(@Nullable Calendar calendar) {

    }

    public interface CitaCalListener {
        void onFechaSelecionada(Citas citas);
    }
}
