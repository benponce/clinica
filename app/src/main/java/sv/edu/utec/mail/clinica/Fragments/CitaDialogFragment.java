package sv.edu.utec.mail.clinica.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import sv.edu.utec.mail.clinica.POJO.Citas;
import sv.edu.utec.mail.clinica.R;

public class CitaDialogFragment extends DialogFragment {
    public final int MY_CAL_WRITE_REQ = 1010;
    private TextView mMedico;
    private TextView mMotivo;
    private TextView mFecha;
    private Citas mCita;
    private CitaDlgListener mListener;

    public CitaDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View v = inflater.inflate(R.layout.fragment_cita_dialog, null);
        //Actualizar el detalle de la cita
        mCita = mListener.getCitaSelected();
        //Iniciar views
        mFecha = v.findViewById(R.id.txtDlgFecha);
        mMedico = v.findViewById(R.id.txtDlgMedico);
        mMotivo = v.findViewById(R.id.txtDlgDesc);
        refrescarDatos();
        //Generar la alerta
        builder.setTitle("Detalles de la cita médica")
                .setView(v)
                .setPositiveButton("Agendar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        agendar();
                    }
                }).setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                CitaDialogFragment.this.getDialog().cancel();
            }
        });
        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (CitaDlgListener) context;
        } catch (Exception e) {
            Log.d("Listener", e.getMessage());
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    public void refrescarDatos() {
        mFecha.setText(mCita.fecha);
        mMedico.setText(mCita.medico);
        mMotivo.setText(mCita.descripcion);
    }

    public void agendar() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_CALENDAR}, MY_CAL_WRITE_REQ);
        }
        ContentResolver cr = getActivity().getContentResolver();
        ContentValues contentValues = new ContentValues();
        TimeZone timeZone = TimeZone.getDefault();
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(df.parse(mCita.fecha));
        } catch (ParseException e) {
            Log.d("Conversión de Fecha", e.getMessage());
        }
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.DTSTART, cal.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, cal.getTimeInMillis());
        values.put(CalendarContract.Events.TITLE, "Cita médica - " + mCita.medico);
        values.put(CalendarContract.Events.DESCRIPTION, mCita.descripcion);
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.EVENT_LOCATION, "Clinica Parroquial Cristo Redentor");
        values.put(CalendarContract.Events.HAS_ALARM, 1);
        values.put(CalendarContract.EXTRA_EVENT_ALL_DAY, true);

        //Insertar evento
        Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, values);
        long eventID = Long.parseLong(uri.getLastPathSegment());
        //Recordatorios
        //Alarma
        ContentValues alarma = new ContentValues();
        alarma.put(CalendarContract.Reminders.EVENT_ID, eventID);
        alarma.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        alarma.put(CalendarContract.Reminders.MINUTES, 60 * 12);
        cr.insert(CalendarContract.Reminders.CONTENT_URI, alarma);
        //Correo
        ContentValues correo = new ContentValues();
        correo.put(CalendarContract.Reminders.EVENT_ID, eventID);
        correo.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_EMAIL);
        correo.put(CalendarContract.Reminders.MINUTES, 60 * 12);
        cr.insert(CalendarContract.Reminders.CONTENT_URI, correo);
        Toast.makeText(getActivity(), "Cita agendada con éxito.\nSe te notificará por correo el día previo a la cita para que no la olvides.", Toast.LENGTH_LONG).show();
    }

    public interface CitaDlgListener {
        Citas getCitaSelected();
    }
}
