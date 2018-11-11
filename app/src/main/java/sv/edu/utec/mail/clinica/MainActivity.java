package sv.edu.utec.mail.clinica;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.POJO.Usuario;

public class MainActivity extends AppCompatActivity {
    TextView mBanner;
    ImageView mLogout;
    ImageView mHeartRate;
    public Usuario usr;
    ImageView mSteps;
    ImageView mConfig;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBanner = findViewById(R.id.txtBanner);
        mLogout = findViewById(R.id.ibnLogout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Control.Salir(MainActivity.this);
            }
        });
        mHeartRate = findViewById(R.id.ibnHeartRate);
        mSteps = findViewById(R.id.ibnStep);
        mSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Control.Pasos(MainActivity.this);
            }
        });
        mConfig = findViewById(R.id.ibnConfig);
        mConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Control.Perfil(MainActivity.this);
            }
        });

        bienvenida();
    }
    private void bienvenida(){
        try{
            Gson gson = new Gson();
            SharedPreferences settings = getSharedPreferences("clinica",0);
            usr = gson.fromJson(settings.getString("Usuario",""), Usuario.class);
            String msj="Bienvenido: " + usr.firstName.toString() + " " + usr.lastName.toString();
            mBanner.setText( msj);
        } catch (Exception e){
            Toast.makeText(getApplicationContext(), "Error al leer preferencias de usuario", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onBackPressed() {

    }
}
