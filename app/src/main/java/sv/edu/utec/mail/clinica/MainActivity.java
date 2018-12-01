package sv.edu.utec.mail.clinica;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.Fitness;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Fragments.CerrarDialogFragment;
import sv.edu.utec.mail.clinica.Red.Sincro;
import sv.edu.utec.mail.clinica.Services.StepCounterService;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private boolean DESCARGAR = true;
    //Banner de bienvenida
    private TextView mBanner;
    public ProgressBar mPrgDescarga;
    //Menú
    private LinearLayout mLogout;
    private LinearLayout mHeartRate;
    private LinearLayout mCitas;
    private LinearLayout mSteps;
    private LinearLayout mConfig;
    //
    public int progreso;
    private Handler mHandler;
    //Manejo de permisos
    public final int PERMISSION_REQ = 1101;
    private String[] PERMISOS = new String[]{
            Manifest.permission.WRITE_CALENDAR,
            Manifest.permission.READ_CALENDAR,
            Manifest.permission.ACCESS_FINE_LOCATION
    };
    //Cliente de la API Google
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    private GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        mHandler = new Handler();
        mBanner = findViewById(R.id.txtBanner);
        mLogout = findViewById(R.id.optLogout);
        mLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CerrarDialogFragment dlg = new CerrarDialogFragment();
                dlg.show(getSupportFragmentManager(), "cerrarDlg");
            }
        });
        mHeartRate = findViewById(R.id.optHeartRate);
        mHeartRate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Control.HR(MainActivity.this);
            }
        });
        mCitas = findViewById(R.id.optCitas);
        mCitas.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Control.Citas(MainActivity.this);
            }
        });
        mSteps = findViewById(R.id.optStep);
        mSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Control.Pasos(MainActivity.this);
            }
        });
        mConfig = findViewById(R.id.optConfig);
        mConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Control.Perfil(MainActivity.this);
            }
        });
        mPrgDescarga = findViewById(R.id.pb_dnload);
        iniciarClienteApi();
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
    }

    private void descargarDatos() {
        progreso = 0;
        Sincro.SincroCallback sincroCallback = new Sincro.SincroCallback() {
            @Override
            public void onSincronizado() {
                progreso += 1;
                Log.i("SINCRO", "" + progreso);
                if (progreso == 2) {
                    mPrgDescarga.setVisibility(View.INVISIBLE);
                }
            }
        };
        Sincro.getInstance(this).downloadPasos(sincroCallback);
        Sincro.getInstance(this).downloadCitas(sincroCallback);
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPrgDescarga.setVisibility(View.INVISIBLE);
                if (progreso < 2) {
                    Toast.makeText(MainActivity.this,
                            "No ha sido posible descargar tus datos.\n Comprueba tu conexión a internet.", Toast.LENGTH_LONG).show();
                }
            }
        }, 30000);
    }

    private void iniciarClienteApi() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    private void revisarPermisos() {
        if (ActivityCompat.checkSelfPermission(this, PERMISOS[0]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, PERMISOS[1]) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, PERMISOS[2]) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISOS, PERMISSION_REQ);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
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
                if (ActivityCompat.checkSelfPermission(this, PERMISOS[0]) != PackageManager.PERMISSION_GRANTED) {
                    mCitas.setEnabled(false);
                } else {
                    mCitas.setEnabled(true);
                }
                if (ActivityCompat.checkSelfPermission(this, PERMISOS[2]) != PackageManager.PERMISSION_GRANTED) {
                    mHeartRate.setEnabled(false);
                } else {
                    mHeartRate.setEnabled(true);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        authInProgress = false;
        if (resultCode == RESULT_OK) {
            if (!mApiClient.isConnecting() && !mApiClient.isConnected()) {
                mApiClient.connect();
            }
        } else if (resultCode == RESULT_CANCELED) {
            Log.e("MAIN_ACTIVITY", "RESULT_CANCELED");
        }
    }

    @Override
    public void onBackPressed() {
        this.moveTaskToBack(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            Intent intent = new Intent(this, StepCounterService.class);
            intent.setAction(StepCounterService.ACTION_SAVE_COUNTER);
            startService(intent);
        } catch (Exception e) {
            Log.e("INVOCAR_SERVICIO", e.getMessage());
        }
        mApiClient.disconnect();
        mSteps.setEnabled(true);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        mSteps.setEnabled(false);
        if (!authInProgress) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(MainActivity.this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {

            }
        } else {
            Log.e("MAIN_ACTIVITY", "authInProgress");
        }
    }
}
