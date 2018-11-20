package sv.edu.utec.mail.clinica.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.R;
import sv.edu.utec.mail.clinica.Red.ClienteRest;

public class PwdFragment extends Fragment {
    EditText mActual;
    EditText mNueva;
    EditText mConfirm;
    Button mCambiar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pwd, container, false);

        mActual = view.findViewById(R.id.pwdActual);
        mNueva = view.findViewById(R.id.pwdNueva);
        mConfirm = view.findViewById(R.id.pwdConfirm);
        mCambiar = view.findViewById(R.id.btnCambiar);
        mCambiar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cambiar();
            }
        });
        return view;
    }

    private void cambiar() {
        String strActual = mActual.getText().toString();
        String strNueva = mNueva.getText().toString();
        String strConfirm = mConfirm.getText().toString();
        if (Control.sysUsr.pwd.equals(strActual)) {
            if (strNueva.equals(strActual)) {
                Toast.makeText(getActivity(), "La Nueva contraseña debe ser diferente a la Actual.", Toast.LENGTH_LONG).show();
            } else {
                if (strNueva.equals(strConfirm)) {
                    String url = ClienteRest.getCambioPwd() + strNueva + '/' + Control.sysUsr.id;
                    StringRequest stringRequest = new StringRequest
                            (Request.Method.PUT, url, new Response.Listener<String>() {
                                @Override
                                public void onResponse(String response) {
                                    cambioExitoso();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Toast.makeText(getActivity(), "Operación rechazada por el servidor", Toast.LENGTH_LONG).show();
                                }
                            });
                    ClienteRest.getInstance(getActivity()).addToRequestQueue(stringRequest);
                } else {
                    Toast.makeText(getActivity(), "La nueva contraseña no concide con la confirmación.", Toast.LENGTH_LONG).show();
                }
            }
        } else {
            Toast.makeText(getActivity(), "La contraseña no válida.", Toast.LENGTH_LONG).show();
        }
    }

    private void cambioExitoso() {
        Toast.makeText(getActivity(), "Su contraseña ha sido cambiada.\nDeberá volver a iniciar sesión.", Toast.LENGTH_LONG).show();
        mActual.setText("");
        mNueva.setText("");
        mConfirm.setText("");
        Control.Salir(getActivity());
    }
}
