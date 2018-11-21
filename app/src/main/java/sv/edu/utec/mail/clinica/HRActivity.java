package sv.edu.utec.mail.clinica;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
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

import java.util.concurrent.TimeUnit;

import sv.edu.utec.mail.clinica.Fitness.FitClient;

public class HRActivity extends FitClient {

    private static final int REQUEST_OAUTH = 1;
    private static final int REQUEST_BLUETOOTH = 1001;
    private static final String AUTH_PENDING = "auth_state_pending";
    private final DataType DATA_TYPE = DataType.TYPE_HEART_RATE_BPM;
    private boolean authInProgress = false;
    private OnDataPointListener mListener;
    //Cliente de la API
    private GoogleApiClient mClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr);
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        buildFitnessClient();
        buildBLE();
        mClient.connect();
        findFitnessDataSources();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {

    }

    private void buildFitnessClient() {
        if (mClient == null) {
            mClient = new GoogleApiClient.Builder(this)
                    .addScope(new Scope(Scopes.FITNESS_BODY_READ))
                    .addApi(Fitness.SENSORS_API)
                    .addApi(Fitness.BLE_API)
                    .addConnectionCallbacks(
                            new GoogleApiClient.ConnectionCallbacks() {
                                @Override
                                public void onConnected(Bundle bundle) {
                                    Log.i("", "Connected!!!");
                                }

                                @Override
                                public void onConnectionSuspended(int i) {
                                    if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
                                        Log.i("Conexión", "Se perdió la conexión: Caída de la red.");
                                    } else if (i
                                            == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
                                        Log.i("Conexión", "Se perdió la conexión: Servicio Desconectado");
                                    }
                                }
                            }
                    )
                    .enableAutoManage(this, 0, new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult result) {
                            Log.i("Conexión", "Falló la conexión con Google Play services: " + result.toString());
                            Toast.makeText(getApplicationContext(),
                                    "Ocurrió una falla durante la conexión con Google Play Services",
                                    Toast.LENGTH_LONG).show();
                        }
                    })
                    .build();
        }
    }

    //BLE
    private void buildBLE() {
        BleScanCallback callback = new BleScanCallback() {
            @Override
            public void onDeviceFound(BleDevice device) {
                Log.d("BLE", "Found bluetooth Device");
                PendingResult<Status> pendingResult = Fitness.BleApi.claimBleDevice(mClient, device);
                Toast.makeText(getApplicationContext(), "Conexion con la pulsera: ", Toast.LENGTH_LONG).show();
                Log.d("BLE", "Claimed bluetooth Device");
            }

            @Override
            public void onScanStopped() {
                Log.d("BLE", "Escaneo interrumpido");
            }

        };

        StartBleScanRequest request = new StartBleScanRequest.Builder()
                .setDataTypes(DATA_TYPE)
                .setBleScanCallback(callback)
                .build();

        if (mClient != null) {
            PendingResult<Status> pendingResult = Fitness.BleApi.startBleScan(mClient, request);
            Log.d("BLE", "Se encontraron las fuentes");
            Log.d("BLE", "Resultado pendiente: " + pendingResult.toString());

        } else {
            Log.d("BLE", "No se creó el Cliente de la API");
        }
    }

    //Encontrar fuentes de datos
    private void findFitnessDataSources() {
        Fitness.SensorsApi.findDataSources(mClient, new DataSourcesRequest.Builder()
                .setDataTypes(DATA_TYPE)
                //.setDataSourceTypes(DataSource.TYPE_RAW)
                .build())
                .setResultCallback(new ResultCallback<DataSourcesResult>() {
                    @Override
                    public void onResult(DataSourcesResult dataSourcesResult) {
                        Log.i("Resultado", "Result: " + dataSourcesResult.getStatus().toString());

                        for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                            Log.i("Resultado", "Data source found: " + dataSource.toString());
                            Log.i("Resultado", "Data Source type: " + dataSource.getDataType().getName());
                            if (dataSource.getDataType().equals(DATA_TYPE)
                                    && mListener == null) {
                                Log.i("Resultado", "Lector de ritmo cardiaco encontrado.");
                                registerFitnessDataListener(dataSource, DATA_TYPE);
                            }
                        }
                    }
                });
    }

    //DataListener
    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        mListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (Field field : dataPoint.getDataType().getFields()) {
                    Value val = dataPoint.getValue(field);
                    Log.i("Data Point", "Detected DataPoint field: " + field.getName());
                    Log.i("Data Point", "Detected DataPoint value: " + val);
                    Toast.makeText(getApplicationContext(), "Valor: " + val, Toast.LENGTH_LONG).show();
                }
            }
        };

        Fitness.SensorsApi.add(
                mClient,
                new SensorRequest.Builder()
                        .setDataSource(dataSource)
                        .setDataType(dataType)
                        .setSamplingRate(10, TimeUnit.SECONDS)
                        .build(),
                mListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.i("Listener", "Listener registered!");
                        } else {
                            Log.i("Listener", "Listener not registered.");
                        }
                    }
                });
    }
}
