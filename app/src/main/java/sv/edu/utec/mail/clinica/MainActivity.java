package sv.edu.utec.mail.clinica;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Red.Sincro;

public class MainActivity extends AppCompatActivity {

    public final int PERMISOS_REQUEST_CODE = 1010;
    //Permisos de acceso requeridos
    private final String[] PERMISOS = new String[]{
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.BODY_SENSORS
    };
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
        Control.readOffLine(this);
        if (DESCARGAR || Control.usrCitas == null || Control.usrPasos == null) {
            descargarDatos();
            DESCARGAR = false;
        }
        verificarPermisos();
    }

    private void bienvenida() {
        String msj = "Bienvenido: " + Control.sysUsr.firstName.toString() + " " + Control.sysUsr.lastName.toString();
        mBanner.setText(msj);
    }

    private void descargarDatos() {
        Sincro.getInstance(this).downloadPasos();
        Sincro.getInstance(this).downloadCitas();
    }

    private void verificarPermisos() {
        boolean concedidos = true;
        for (String permiso : PERMISOS) {
            if (ActivityCompat.checkSelfPermission(this, permiso) != PackageManager.PERMISSION_GRANTED) {
                concedidos = false;
                break;
            }
        }
        if (!concedidos) {
            Snackbar.make(findViewById(R.id.main_activity),
                    "Se requieren permisos para el funcionamiento de esta aplicación.",
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction("Aceptar", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            ActivityCompat.requestPermissions(MainActivity.this, PERMISOS, PERMISOS_REQUEST_CODE);
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }
}
