package sv.edu.utec.mail.clinica.Fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
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
    public final int CALENDAR_ACCESS_REQUEST = 1010;
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


    private void refrescarDatos() {
        mFecha.setText(mCita.fecha.substring(6, 8) + "/" + mCita.fecha.substring(4, 6) + "/" + mCita.fecha.substring(0, 4));
        mMedico.setText(mCita.medico);
        mMotivo.setText(mCita.descripcion);
    }

    private void agendar() {
        String[] permisos = new String[]{Manifest.permission.READ_CALENDAR, Manifest.permission.WRITE_CALENDAR};
        try {
            if (ActivityCompat.checkSelfPermission(getActivity(), permisos[0]) != PackageManager.PERMISSION_GRANTED ||
                    ActivityCompat.checkSelfPermission(getActivity(), permisos[1]) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(getActivity(), permisos, CALENDAR_ACCESS_REQUEST);
            } else {
                TimeZone timeZone = TimeZone.getDefault();
                SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
                Calendar cal = Calendar.getInstance();
                cal.setTime(df.parse(mCita.fecha));
                ContentResolver cr = getActivity().getContentResolver();
                //Validar evento
                if (queryEvento(cr, String.valueOf(cal.getTimeInMillis())).moveToNext()) {
                    Toast.makeText(getActivity(), "Ya posee agendada una cita médica para esta fecha.", Toast.LENGTH_LONG).show();
                } else {
                    ContentValues evento = prepararEvento(cr, cal, timeZone);
                    //Insertar evento
                    Uri uri = cr.insert(CalendarContract.Events.CONTENT_URI, evento);
                    long eventID = Long.parseLong(uri.getLastPathSegment());
                    //Recordatorios
                    //Alarma
                    ContentValues alarma = prepararAlarma(cr, eventID);
                    cr.insert(CalendarContract.Reminders.CONTENT_URI, alarma);
                    //Correo
                    ContentValues correo = prepararCorreo(cr, eventID);
                    cr.insert(CalendarContract.Reminders.CONTENT_URI, correo);
                    //Informar al usuario
                    Toast.makeText(getActivity(), "Cita agendada con éxito.\nSe te notificará por correo el día previo a la cita para que no la olvides.", Toast.LENGTH_LONG).show();
                }
            }
        } catch (ParseException e) {
            Toast.makeText(getActivity(), "No fue posible agendar la cita médica.", Toast.LENGTH_LONG).show();
            Log.d("Agendar cita", e.getMessage());
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CALENDAR_ACCESS_REQUEST) {
            boolean allGranted = false;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    allGranted = true;
                } else {
                    allGranted = false;
                    break;
                }
            }
            if (allGranted) {
                agendar();
            } else {
                Toast.makeText(getActivity(), "No fue posible agendar la cita médica.\nPosiblemente no nos concediste los permisos necesarios.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private Cursor queryEvento(ContentResolver cr, String dtStart) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CALENDAR}, CALENDAR_ACCESS_REQUEST);
            return null;
        } else {

            String[] mProjection =
                    {
                            "_id",
                            CalendarContract.Events.EVENT_LOCATION,
                            CalendarContract.Events.DTSTART,
                    };

            Uri uri = CalendarContract.Events.CONTENT_URI;
            String selection = "(" + CalendarContract.Events.EVENT_LOCATION + " = ? ) AND ("
                    + CalendarContract.Events.DTSTART + " =? )";
            String[] selectionArgs = new String[]{"Clinica Parroquial Cristo Redentor", dtStart};

            return cr.query(uri, mProjection, selection, selectionArgs, null);
        }
    }


    private ContentValues prepararEvento(ContentResolver cr, Calendar cal, TimeZone timeZone) {
        ContentValues values = new ContentValues();
        //Preparar evento
        values.put(CalendarContract.Events.DTSTART, cal.getTimeInMillis());
        values.put(CalendarContract.Events.DTEND, cal.getTimeInMillis());
        values.put(CalendarContract.Events.TITLE, "Cita médica - " + mCita.medico);
        values.put(CalendarContract.Events.DESCRIPTION, mCita.descripcion);
        values.put(CalendarContract.Events.CALENDAR_ID, 1);
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.EVENT_LOCATION, "Clinica Parroquial Cristo Redentor");
        values.put(CalendarContract.Events.HAS_ALARM, 1);
        values.put(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
        return values;
    }

    private ContentValues prepararAlarma(ContentResolver cr, long id) {
        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Reminders.EVENT_ID, id);
        cv.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT);
        cv.put(CalendarContract.Reminders.MINUTES, 60 * 6);
        return cv;
    }

    private ContentValues prepararCorreo(ContentResolver cr, long id) {
        ContentValues cv = new ContentValues();
        cv.put(CalendarContract.Reminders.EVENT_ID, id);
        cv.put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_EMAIL);
        cv.put(CalendarContract.Reminders.MINUTES, 60 * 12);
        return cv;
    }

    public interface CitaDlgListener {
        Citas getCitaSelected();
    }
}
