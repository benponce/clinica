package sv.edu.utec.mail.clinica;

import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.jjoe64.graphview.GraphView;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Fitness.FitClient;
import sv.edu.utec.mail.clinica.Red.ClienteRest;
import sv.edu.utec.mail.clinica.Utilidades.Graficador;

public class StepsActivity extends FitClient {

    TextView mBanner;
    GraphView graph;
    Button mTmpGuardar;

    private OnDataPointListener onDataPointListener;

    private final DataType DATA_TYPE = DataType.TYPE_STEP_COUNT_DELTA;
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    //Cliente de la API
    private GoogleApiClient mApiClient;

    //SharedPreferences
    private SharedPreferences sharedPrefStep;
    private Date hoy = new Date();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        mBanner = findViewById(R.id.txtPasos);
        graph = findViewById(R.id.graphSteps);
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(10);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(10000);
        graph.getViewport().setBackgroundColor(Color.argb(128, 224, 224, 224));
        graph.addSeries(Graficador.lineaMeta());
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.WHITE);
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.WHITE);

        mTmpGuardar = findViewById(R.id.tmpGuardar);
        mTmpGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardarPasos();
            }
        });

        buildSensor();
        if (Control.usrPasos != null) {
            graph.addSeries(Graficador.llenarSerie(Control.usrPasos, StepsActivity.this));
        } else {
            Toast.makeText(this, "No tienes registro de pasos", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes(DATA_TYPE)
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                    if (DATA_TYPE.equals(dataSource.getDataType())) {
                        registerFitnessDataListener(dataSource, DATA_TYPE);
                    }
                }
            }
        };
        Fitness.SensorsApi.findDataSources(mApiClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
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
            Log.e("onActivityResult", "RESULT_CANCELED");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!authInProgress) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(StepsActivity.this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {

            }
        } else {
            Log.e("GoogleFit", "authInProgress");
        }
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Sensor de Fitness
        Fitness.SensorsApi.remove(mApiClient, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            mApiClient.disconnect();
                        }
                    }
                });
        Control.guardarConteo(this);
        Log.i("ONSTOP", "Se detuvo");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    //Sensor de Fitness
    private void buildSensor() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    //Sensor de Fitness
    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        SensorRequest request = new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(dataType)
                .setSamplingRate(3, TimeUnit.SECONDS)
                .build();

        onDataPointListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (final Field field : dataPoint.getDataType().getFields()) {
                    actualizarPasosHoy(dataPoint.getValue(field).asInt());
                }
            }
        };
        Fitness.SensorsApi.add(mApiClient, request, onDataPointListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.e("GoogleFit", "SensorApi successfully added");
                        }
                    }
                });
    }

    private void actualizarPasosHoy(int pasosHoy) {
        if (Control.usrPasosHoy == null) {
            Control.iniciarConteo(this);
        }
        Control.usrPasosHoy.valor += pasosHoy;
        mBanner.setText("Pasos de hoy: " + Control.usrPasosHoy.valor);
    }

    private void guardarPasos() {
        String url = ClienteRest.getRegistroVitalesUrl();
        Map<String, String> params = new HashMap<String, String>();
        params.put("unidad", "Pasos");
        params.put("codigo_pac", String.valueOf(Control.sysUsr.paciente));
        params.put("codigo_vitales", "7");
        params.put("fecha", Control.usrPasosHoy.fecha);
        params.put("valor", String.valueOf(Control.usrPasosHoy.valor));
        StringRequest stringRequest = ClienteRest.subirDatos(getApplicationContext(), Request.Method.POST, url, "Registro de pasos almacenado", "Error al guardar los pasos", params);
        ClienteRest.getInstance(this).addToRequestQueue(stringRequest);
    }

}
