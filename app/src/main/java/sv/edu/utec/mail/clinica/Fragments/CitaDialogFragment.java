package sv.edu.utec.mail.clinica.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;

import sv.edu.utec.mail.clinica.POJO.Citas;
import sv.edu.utec.mail.clinica.R;

public class CitaDialogFragment extends DialogFragment {
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
        try {
            TimeZone timeZone = TimeZone.getDefault();
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
            Calendar cal = Calendar.getInstance();
            cal.setTime(df.parse(mCita.fecha));
            //Llamada a la Ventana de Google Calendar
            Intent calIntent = new Intent(Intent.ACTION_INSERT);
            calIntent.setType("vnd.android.cursor.item/event");
            calIntent.putExtra(CalendarContract.Events.TITLE, "Cita médica - " + mCita.medico);
            calIntent.putExtra(CalendarContract.Events.EVENT_LOCATION, "Clinica parroquial Cristo Redentor");
            calIntent.putExtra(CalendarContract.Events.DESCRIPTION, mCita.descripcion);
            calIntent.putExtra(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
            calIntent.putExtra(CalendarContract.Events.HAS_ALARM, 1);
            calIntent.putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, true);
            calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cal.getTimeInMillis());
            calIntent.putExtra(CalendarContract.EXTRA_EVENT_END_TIME, cal.getTimeInMillis());
            startActivity(calIntent);
        } catch (ParseException e) {
            Log.d("Conversion de fecha", e.getMessage());
        }
    }

    public interface CitaDlgListener {
        Citas getCitaSelected();
    }
}
