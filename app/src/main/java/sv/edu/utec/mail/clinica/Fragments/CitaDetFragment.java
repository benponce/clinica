package sv.edu.utec.mail.clinica.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import sv.edu.utec.mail.clinica.POJO.Citas;
import sv.edu.utec.mail.clinica.R;

public class CitaDetFragment extends Fragment {

    private TextView mMedico;
    private TextView mMotivo;
    private TextView mFecha;
    private Button mRegresar;
    private Button mAgendar;
    private CitaDetListener mListener;
    private Citas mCita;

    public CitaDetFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cita_det, container, false);
        //Iniciar views
        mFecha = v.findViewById(R.id.txtDlgFecha);
        mMedico = v.findViewById(R.id.txtDlgMedico);
        mMotivo = v.findViewById(R.id.txtDlgDesc);
        mAgendar = v.findViewById(R.id.btnCitaAgendar);
        mRegresar = v.findViewById(R.id.btnCitaRegresar);
        Bundle args = getArguments();
        mCita = (Citas) args.getSerializable("CitaSelec");
        refrescarDatos(mCita);
        //Colocar valores y acciones en las view
        mRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRegresar();
            }
        });
        mAgendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agendar();
            }
        });
        //Retorno por defecto del método
        return v;

    }

    public void refrescarDatos(Citas cita) {
        mFecha.setText(mCita.fecha);
        mMedico.setText(mCita.medico);
        mMotivo.setText(mCita.descripcion);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (CitaDetListener) context;
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void agendar() {/*
        Context ctx = getContext();
        ContentResolver cr = ctx.getContentResolver();
        ContentValues vals = new ContentValues();
        vals.put(CalendarContract.Events.DTSTART, mCita.fecha);
        vals.put(CalendarContract.Events.TITLE, "Cita médica: " + mCita.medico);
        vals.put(CalendarContract.Events.DESCRIPTION, mCita.descripcion);

        TimeZone timeZone = TimeZone.getDefault();
        vals.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        vals.put(CalendarContract.Events.CALENDAR_ID, 1);
        vals.put(CalendarContract.Events.RRULE, "COUNT=1");

        vals.put(CalendarContract.Events.HAS_ALARM, 1);
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, vals);

        long eventID = Long.parseLong(uri.getLastPathSegment());
        ContentValues recordatorio = new ContentValues();
        recordatorio.put(CalendarContract.Reminders.EVENT_ID, eventID);
        recordatorio.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        recordatorio.put(CalendarContract.Reminders.MINUTES, 60*24);
        uri = cr.insert(CalendarContract.Reminders.CONTENT_URI, recordatorio);*/
    }

    public interface CitaDetListener {
        void onRegresar();
    }
}
