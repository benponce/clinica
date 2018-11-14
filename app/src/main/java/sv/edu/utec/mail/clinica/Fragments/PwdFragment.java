package sv.edu.utec.mail.clinica.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.R;
import sv.edu.utec.mail.clinica.Red.ClienteRest;

public class PwdFragment extends Fragment {
    Usuario usr;
    EditText mActual;
    EditText mNueva;
    EditText mConfirm;
    Button mCambiar;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pwd, container, false);
        try {
            Gson gson = new Gson();
            SharedPreferences settings = this.getActivity().getSharedPreferences("clinica", 0);
            usr = gson.fromJson(settings.getString("Usuario", ""), Usuario.class);

        } catch (Exception e) {
            Log.i("Usuario", "No se encontró");
        }
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
        if (usr.pwd.equals(strActual)) {
            if (strNueva.equals(strActual)) {
                Toast.makeText(getActivity(), "La Nueva contraseña debe ser diferente a la Actual.", Toast.LENGTH_LONG).show();
            } else {
                if (strNueva.equals(strConfirm)) {
                    String url = ClienteRest.getCambioPwd() + strNueva + '/' + usr.id;
                    JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                            (Request.Method.PUT, url, null, new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    cambioExitoso();
                                }
                            }, new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    if (error instanceof ParseError) {
                                        cambioExitoso();
                                    } else {
                                        Toast.makeText(getActivity(), "Operación rechazada por el servidor", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                    ClienteRest.getInstance(getActivity()).addToRequestQueue(jsonObjectRequest);
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
