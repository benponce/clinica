package sv.edu.utec.mail.clinica;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Fragments.CitaDialogFragment;
import sv.edu.utec.mail.clinica.POJO.Citas;
import sv.edu.utec.mail.clinica.Red.Sincro;

public class CitaActivity extends AppCompatActivity implements CitaDialogFragment.CitaDlgListener {

    CalendarView mCalendario;
    Citas mCitaSelected;
    SwipeRefreshLayout swipeRefreshLayout;
    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cita);

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent);
        mHandler = new Handler();

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
        if (Control.usrCitas != null) {
            colocarCitas();
        } else {
            Toast.makeText(this, "No tienes citas programadas.", Toast.LENGTH_LONG).show();
        }

        final Sincro.SincroCallback sincroCallback = new Sincro.SincroCallback() {
            @Override
            public void onSincronizado() {
                if (Control.usrCitas != null) {
                    colocarCitas();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        };

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Sincro.getInstance(CitaActivity.this).downloadCitas(sincroCallback);
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 20000);
            }
        });


    }

    private void colocarCitas() {
        List<EventDay> events = new ArrayList<>();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        Calendar[] cal = new Calendar[Control.usrCitas.length];
        int i = 0;
        for (Citas cita : Control.usrCitas) {
            try {
                cal[i] = Calendar.getInstance();
                cal[i].setTime(df.parse(cita.fecha));
                events.add(new EventDay(cal[i], R.drawable.ic_citas_marca));
            } catch (Exception e) {
                Log.d("Agregar eventos", e.getMessage());
            }
            i++;
        }
        mCalendario.setEvents(events);
    }

    public void verCita(EventDay eventDay) {
        if (Control.usrCitas != null) {
            //Leer la fecha seleccionada
            Date fecha = eventDay.getCalendar().getTime();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            String strFecha = df.format(fecha);
            //Buscar la cita que corresponde a la fecha, si la hay
            for (Citas cita : Control.usrCitas) {
                if (strFecha.equals(cita.fecha)) {
                    mCitaSelected = cita;
                    CitaDialogFragment citaDlg = new CitaDialogFragment();
                    citaDlg.show(getSupportFragmentManager(), "citaDlg");
                    break;
                }
            }
        }
    }

    @Override
    public Citas getCitaSelected() {
        return mCitaSelected;
    }
}
