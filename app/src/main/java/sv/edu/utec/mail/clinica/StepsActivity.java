package sv.edu.utec.mail.clinica;

import android.content.Intent;
import android.content.IntentSender;
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
import com.jjoe64.graphview.helper.StaticLabelsFormatter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Fitness.FitClient;
import sv.edu.utec.mail.clinica.Red.ClienteRest;
import sv.edu.utec.mail.clinica.Services.StepCounterService;
import sv.edu.utec.mail.clinica.Utilidades.Graficador;

public class StepsActivity extends FitClient {

    TextView mBanner;
    GraphView graph;
    Button mGuardar;

    private OnDataPointListener onDataPointListener;

    private final DataType DATA_TYPE = DataType.TYPE_STEP_COUNT_DELTA;
    private int mCount;
    private static final int REQUEST_OAUTH = 1;
    private static final String AUTH_PENDING = "auth_state_pending";
    private boolean authInProgress = false;
    //Cliente de la API
    private GoogleApiClient mApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }

        mBanner = findViewById(R.id.txtPasos);
        mBanner.setText("Pasos de hoy: " + Control.usrPasosHoy.valor);
        graph = findViewById(R.id.graphSteps);
        graph.getViewport().setBackgroundColor(Color.rgb(208, 231, 255));
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(8);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(10000);
        graph.addSeries(Graficador.lineaMeta());
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.rgb(0, 51, 102));
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.rgb(0, 51, 102));
        mCount = Control.usrPasosHoy.valor;
        mGuardar=findViewById(R.id.btnGuardarPasos);
        mGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar();
            }
        });
        buildSensor();
        if (Control.usrPasos != null) {
            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
            staticLabelsFormatter.setHorizontalLabels(Graficador.getEtiquetas(Control.usrPasos.length));
            graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
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
        Intent intent = new Intent(this, StepCounterService.class);
        startService(intent);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    //Sensor de Fitness
    private void buildSensor() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
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
                    mCount += dataPoint.getValue(field).asInt();
                    mBanner.setText("Pasos de hoy: " + Control.usrPasosHoy.valor);
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

    private void guardar() {
        mCount = Control.usrPasosHoy.valor;
        Map<String, String> params = new HashMap<String, String>();
        params.put("unidad", "Pasos");
        params.put("codigo_pac", String.valueOf(Control.sysUsr.paciente));
        params.put("codigo_vitales", "7");
        params.put("fecha", Control.usrPasosHoy.fecha);
        params.put("valor", String.valueOf(Control.usrPasosHoy.valor));
        ClienteRest.getInstance(this).addToRequestQueue(
        ClienteRest.subirDatos(this, Request.Method.POST, ClienteRest.getRegistroVitalesUrl()
                , "Pasos registrados con éxito", "No se pudo establecer la conexión con el servidor", params));
    }

}
