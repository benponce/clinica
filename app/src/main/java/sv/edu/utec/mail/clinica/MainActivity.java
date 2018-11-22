package sv.edu.utec.mail.clinica;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Red.Sincro;

public class MainActivity extends AppCompatActivity {

    private boolean DESCARGAR = true;
    private TextView mBanner;
    private ImageView mLogout;
    private ImageView mHeartRate;
    private ImageView mCitas;
    private ImageView mSteps;
    private ImageView mConfig;

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
        mHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Control.HR(MainActivity.this);
            }
        });
        mCitas = findViewById(R.id.ibnCitas);
        mCitas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Control.Citas(MainActivity.this);
            }
        });
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
        Control.iniciarConteo(this);
        Control.readOffLine(this);
        if (DESCARGAR || Control.usrCitas == null || Control.usrPasos == null) {
            descargarDatos();
            DESCARGAR = false;
        }
    }

    private void bienvenida() {
        String msj = "Bienvenido: " + Control.sysUsr.firstName.toString() + " " + Control.sysUsr.lastName.toString();
        mBanner.setText(msj);
    }

    private void descargarDatos() {
        Sincro.getInstance(this).downloadPasos();
        Sincro.getInstance(this).downloadCitas();
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}
