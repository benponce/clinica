package sv.edu.utec.mail.clinica.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;

import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.R;

public class CuentaFragment extends Fragment {
    Usuario usr;

    TextView lblUsuario;
    TextView lblNombre;
    TextView lblApellido;
    TextView lblEmail;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_cuenta, container, false);
        try {
            Gson gson = new Gson();
            SharedPreferences settings = this.getActivity().getSharedPreferences("clinica", 0);
            usr = gson.fromJson(settings.getString("Usuario", ""), Usuario.class);
            lblUsuario = view.findViewById(R.id.lblUser);
            lblUsuario.setText("Usuario: " + usr.userName);
            lblNombre = view.findViewById(R.id.lblNombre);
            lblNombre.setText("Nombre: " + usr.firstName);
            lblApellido = view.findViewById(R.id.lblApellido);
            lblApellido.setText("Apellido: " + usr.lastName);
            lblEmail = view.findViewById(R.id.lblEmail);
            lblEmail.setText("E-Mail: " + usr.email);
        } catch (Exception e) {
            Log.i("Usuario", "No se encontró");
        } finally {
            return view;
        }
    }
}
