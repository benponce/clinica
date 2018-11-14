package sv.edu.utec.mail.clinica;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.FitnessStatusCodes;
import com.google.android.gms.fitness.data.BleDevice;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.data.Value;
import com.google.android.gms.fitness.request.BleScanCallback;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.StartBleScanRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.gson.Gson;
import com.jjoe64.graphview.GraphView;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import sv.edu.utec.mail.clinica.Fitness.FitClient;
import sv.edu.utec.mail.clinica.Fitness.HistoryService;
import sv.edu.utec.mail.clinica.Fitness.ResetBroadcastReceiver;
import sv.edu.utec.mail.clinica.POJO.Lectura;
import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.Red.ClienteRest;
import sv.edu.utec.mail.clinica.Utilidades.Graficador;

public class StepsActivity extends FitClient {

    TextView mBanner;
    GraphView graph;
    Usuario usr;

    private OnDataPointListener onDataPointListener;

    private static final int REQUEST_OAUTH = 1;

    private static final String AUTH_PENDING = "auth_state_pending";

    private static final int REQUEST_BLUETOOTH = 1001;

    private int nbStepSaveMidnight = 0;

    private boolean authInProgress = false;

    //Cliente de la API
    private GoogleApiClient mApiClient;

    // Pasos guardados en SharedPreferences
    private SharedPreferences sharedPrefStep;
    public int nbStepOfDay;

    //Cliente del historial de Google Fit
    private HistoryService hist;

    private final ResultCallback mResultCallback = new ResultCallback() {
        @Override
        public void onResult(@NonNull Result result) {
            Status status = result.getStatus();
            if (!status.isSuccess()) {
                switch (status.getStatusCode()) {
                    case FitnessStatusCodes.DISABLED_BLUETOOTH:
                        try {
                            status.startResolutionForResult(
                                    StepsActivity.this, REQUEST_BLUETOOTH);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                }
            }
        }
    };

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
        graph.getViewport().setMaxX(7);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(10000);

        try {
            Gson gson = new Gson();
            SharedPreferences settings = getSharedPreferences("clinica", 0);
            usr = gson.fromJson(settings.getString("Usuario", ""), Usuario.class);
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error al leer preferencias de usuario", Toast.LENGTH_SHORT).show();
        }

        hist = HistoryService.getInstance();

        hist.buildFitnessClientHistory(this);

        buildSensor();

        readStepSaveMidnight();

        resetCounter(this);
        graficar();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        //Sensor Fitness Part
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes( DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setDataSourceTypes(DataSource.TYPE_DERIVED)
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for( DataSource dataSource : dataSourcesResult.getDataSources() ) {
                    if( DataType.TYPE_STEP_COUNT_CUMULATIVE.equals( dataSource.getDataType() ) ) {
                        registerFitnessDataListener(dataSource, DataType.TYPE_STEP_COUNT_CUMULATIVE);
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
        switch (requestCode) {
            case REQUEST_BLUETOOTH:
                startBleScan();
                Log.e( "onActivityResult", "REQUEST_BLUETOOTH" );
                break;
            case REQUEST_OAUTH:
                authInProgress = false;
                if( resultCode == RESULT_OK ) {
                    if( !mApiClient.isConnecting() && !mApiClient.isConnected() ) {
                        mApiClient.connect();
                    }
                } else if( resultCode == RESULT_CANCELED ) {
                    Log.e( "onActivityResult", "RESULT_CANCELED" );
                }
                break;
            default:
                Log.e("onActivityResult", "problem");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if( !authInProgress ) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult( StepsActivity.this, REQUEST_OAUTH );
            } catch(IntentSender.SendIntentException e ) {

            }
        } else {
            Log.e( "GoogleFit", "authInProgress" );
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
        Fitness.SensorsApi.remove( mApiClient, this )
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            mApiClient.disconnect();
                        }
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(AUTH_PENDING, authInProgress);
    }

    //Sensor de Fitness
    private void buildSensor() {
        mApiClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.BLE_API)
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
                .setDataSource( dataSource )
                .setDataType( dataType )
                .setSamplingRate( 3, TimeUnit.SECONDS )
                .build();

        onDataPointListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (final Field field : dataPoint.getDataType().getFields()) {
                    final Value value = dataPoint.getValue(field);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (value.asInt() > nbStepSaveMidnight) {
                                nbStepOfDay = value.asInt() - nbStepSaveMidnight;
                            } else {
                                resetStepSaveMidnight();
                                nbStepOfDay = value.asInt();
                            }
                            ResetBroadcastReceiver r = ResetBroadcastReceiver.getInstance();
                            r.saveStepSaveMidnight(getApplicationContext(), nbStepOfDay);
                            String msj = "Pasos de hoy: " + nbStepOfDay;
                            mBanner.setText(msj);
                        }
                    });
                }
            }
        };
        Fitness.SensorsApi.add(mApiClient, request, onDataPointListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.e( "GoogleFit", "SensorApi successfully added" );
                        }
                    }
                });
    }

    //BLE
    private void startBleScan () {
        BleScanCallback callback = new BleScanCallback() {
            @Override
            public void onDeviceFound(BleDevice device) {
                claimBleDevice(device);


            }
            @Override
            public void onScanStopped() {
                Log.e( "startBleScan", "onScanStopped" );
            }
        };

        StartBleScanRequest request = new StartBleScanRequest.Builder()
                .setDataTypes(DataType.TYPE_STEP_COUNT_CUMULATIVE)
                .setBleScanCallback(callback)
                .build();

        PendingResult<Status> pendingResult =
                Fitness.BleApi.startBleScan(mApiClient, request);
        pendingResult.setResultCallback(mResultCallback);
    }

    //BLE
    private void claimBleDevice (BleDevice bleDevice) {
        PendingResult<Status> pendingResult =
                Fitness.BleApi.claimBleDevice(mApiClient, bleDevice);
        pendingResult.setResultCallback(mResultCallback);
    }

    //BLE
    private void unclaimBleDevice (BleDevice bleDevice) {
        PendingResult<Status> pendingResult =
                Fitness.BleApi.unclaimBleDevice(mApiClient, bleDevice);
        pendingResult.setResultCallback(mResultCallback);
    }

    private void readStepSaveMidnight () {
        sharedPrefStep = PreferenceManager.getDefaultSharedPreferences(this);
        nbStepSaveMidnight = sharedPrefStep.getInt("THE_STEP_AT_MIDNIGHT",0);
    }

    private void resetStepSaveMidnight() {
        sharedPrefStep = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPrefStep.edit().remove("THE_STEP_AT_MIDNIGHT").apply();
    }

    private void resetCounter(Context context) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.add(Calendar.DAY_OF_MONTH, 1);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, new Intent(context, ResetBroadcastReceiver.class), PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC_WAKEUP, cal.getTimeInMillis(), 1000*60*60*24, pi);
    }

    private void graficar() {

        String url = ClienteRest.getPasosUrl() + usr.paciente;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.GET, url, null, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            Gson gson = new Gson();

                            Lectura[] lecturas = gson.fromJson(response.getJSONArray("items").toString(), Lectura[].class);
                            graph.addSeries(Graficador.llenarSerie(lecturas, StepsActivity.this));
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(getApplicationContext(), "No tiene registro de pasos ", Toast.LENGTH_LONG).show();
                    }
                });
        ClienteRest.getInstance(this).addToRequestQueue(jsonObjectRequest);

    }

}
