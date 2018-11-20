package sv.edu.utec.mail.clinica;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.applandeo.materialcalendarview.CalendarView;
import com.applandeo.materialcalendarview.EventDay;
import com.applandeo.materialcalendarview.listeners.OnDayClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Fragments.CitaDialogFragment;
import sv.edu.utec.mail.clinica.POJO.Citas;

public class CitaActivity extends AppCompatActivity implements CitaDialogFragment.CitaDlgListener {

    CalendarView mCalendario;
    Citas mCitaSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cita);
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
            mCalendario.setEvents(colocarCitas(Control.usrCitas));
        } else {
            Toast.makeText(this, "No tienes citas programadas.", Toast.LENGTH_LONG).show();
        }
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
        for (Citas cita : Control.usrCitas) {
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
