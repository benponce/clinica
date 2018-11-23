package sv.edu.utec.mail.clinica;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;

import org.json.JSONObject;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.Red.ClienteRest;

public class LoginActivity extends AppCompatActivity {
    private EditText mUserId;
    private EditText mPassword;
    private Button mLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mUserId = findViewById(R.id.txtUser);
        mPassword = findViewById(R.id.txtPwd);
        mLogin = findViewById(R.id.btnLogin);

        mLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ingresar();
            }
        });

        try {
            Gson gson = new Gson();
            SharedPreferences settings = getSharedPreferences("clinica", 0);
            Control.sysUsr = gson.fromJson(settings.getString("Usuario", ""), Usuario.class);
            if (Control.sysUsr != null) {
                redireccionar();
            }
        } catch (Exception e) {
            Log.d("Login", "No registrado");
        }
    }

    private void ingresar() {
        String usr = mUserId.getText().toString();
        String pdw = mPassword.getText().toString();
        String url = ClienteRest.getLoginUrl() + usr + '/' + pdw;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Gson gson = new Gson();
                            Control.sysUsr = gson.fromJson(response.toString(), Usuario.class);
                            guardarIngreso(response);
                            Toast.makeText(getApplicationContext(), "Usuario verificado", Toast.LENGTH_LONG).show();
                            redireccionar();
                        } catch (Exception e) {
                            Log.d("Lectura de usuario", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "Usuario o Contraseña no válidos", Toast.LENGTH_LONG).show();
                    }
                });
        ClienteRest.getInstance(this).addToRequestQueue(jsonObjectRequest);
    }

    private void guardarIngreso(JSONObject response) {
        SharedPreferences sp = getSharedPreferences("clinica", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Usuario", response.toString());
        editor.commit();
    }

    private void redireccionar() {
        Control.RedirectMain(this);
    }
}
