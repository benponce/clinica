package sv.edu.utec.mail.clinica.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.R;

public class CuentaFragment extends Fragment {
    TextView lblUsuario;
    TextView lblNombre;
    TextView lblApellido;
    TextView lblEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cuenta, container, false);

        lblUsuario = view.findViewById(R.id.lblUser);
        lblUsuario.setText("Usuario: " + Control.sysUsr.userName);
        lblNombre = view.findViewById(R.id.lblNombre);
        lblNombre.setText("Nombre: " + Control.sysUsr.firstName);
        lblApellido = view.findViewById(R.id.lblApellido);
        lblApellido.setText("Apellido: " + Control.sysUsr.lastName);
        lblEmail = view.findViewById(R.id.lblEmail);
        lblEmail.setText("E-Mail: " + Control.sysUsr.email);

        return view;
    }
}
