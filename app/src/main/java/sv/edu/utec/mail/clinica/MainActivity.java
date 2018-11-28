package sv.edu.utec.mail.clinica;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Red.Sincro;
import sv.edu.utec.mail.clinica.Services.StepCounterService;

public class MainActivity extends AppCompatActivity {

    private String[] PERMISOS=new String[]{
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.ACCESS_COARSE_LOCATION
    };
    public final int PERMISSION_REQ=1101;
    private boolean DESCARGAR = true;
    private TextView mBanner;
    private LinearLayout mLogout;
    private LinearLayout mHeartRate;
    private LinearLayout mCitas;
    private LinearLayout mSteps;
    private LinearLayout mConfig;

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
        revisarPermisos();
        preparar();
    }

    private void preparar() {
        String msj = "Bienvenido: " + Control.sysUsr.firstName + " " + Control.sysUsr.lastName;
        mBanner.setText(msj);
        Control.readOffLine(this);
        if (DESCARGAR || Control.usrCitas == null || Control.usrPasos == null) {
            descargarDatos();
            DESCARGAR = false;
        }
        Control.iniciarConteo(this);
        try {
            Intent intent = new Intent(this, StepCounterService.class);
            startService(intent);
        } catch (Exception e) {
            Log.e("INVOCAR_SERVICIO", e.getMessage());
        }

    }

    private void descargarDatos() {
        Sincro.getInstance(this).downloadPasos();
        Sincro.getInstance(this).downloadCitas();
    }

    private void revisarPermisos(){
        if (ActivityCompat.checkSelfPermission(this, PERMISOS[0]) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this, PERMISOS[1]) != PackageManager.PERMISSION_GRANTED||
                ActivityCompat.checkSelfPermission(this, PERMISOS[2]) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, PERMISOS, PERMISSION_REQ);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQ) {
            boolean allGranted = true;
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    break;
                }
            }
            if (!allGranted) {
                ActivityCompat.requestPermissions(this, PERMISOS, PERMISSION_REQ);
                Toast.makeText(this, "Posiblemente no nos concediste los permisos necesarios.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

}
