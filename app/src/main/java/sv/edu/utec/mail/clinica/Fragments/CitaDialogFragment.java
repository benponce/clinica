package sv.edu.utec.mail.clinica.Fragments;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import sv.edu.utec.mail.clinica.R;

public class CitaDialogFragment extends DialogFragment {


    CitaDialogListener mListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (CitaDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " no implementa la interfaz requerida.");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        builder.setView(inflater.inflate(R.layout.citas_dialog, null))
                .setTitle("Detalles de la Cita")
                .setPositiveButton("Agendar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogPositiveClick(CitaDialogFragment.this);
                    }
                })
                .setNegativeButton("Cerrar", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mListener.onDialogNegativeClick(CitaDialogFragment.this);
                    }
                });
        return builder.create();
    }

    public interface CitaDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);

        void onDialogNegativeClick(DialogFragment dialog);
    }

}
