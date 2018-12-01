package sv.edu.utec.mail.clinica.Fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;

import sv.edu.utec.mail.clinica.AppControl.Control;

public class CerrarDialogFragment extends DialogFragment {

    public CerrarDialogFragment() {
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        //Generar la alerta
        builder.setTitle("Saliendo de la aplicación...")
                .setMessage("Al cerrar sesión se borrarán tus datos almacenados en este dispositivo, incluyendo tu conteo de pasos.\n¿Deseas continuar?")
                .setPositiveButton("Aceptar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Control.Salir(getActivity());
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        CerrarDialogFragment.this.getDialog().cancel();
                    }
                });
        return builder.create();
    }

}
