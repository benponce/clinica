package sv.edu.utec.mail.clinica;

import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

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
import com.google.android.gms.fitness.request.BleScanCallback;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.request.StartBleScanRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.concurrent.TimeUnit;

import sv.edu.utec.mail.clinica.Fitness.FitClient;

public class HRActivity extends FitClient {

    private final ResultCallback mResultCallback = new ResultCallback() {
        @Override
        public void onResult(@NonNull Result result) {
            Status status = result.getStatus();
            if (!status.isSuccess()) {
                switch (status.getStatusCode()) {
                    case FitnessStatusCodes.DISABLED_BLUETOOTH:
                        try {
                            status.startResolutionForResult(
                                    HRActivity.this, REQUEST_BLUETOOTH);
                        } catch (IntentSender.SendIntentException e) {

                        }
                        break;
                }
            }
        }
    };
    private static final int REQUEST_OAUTH = 1;
    private static final int REQUEST_BLUETOOTH = 1001;
    private static final String AUTH_PENDING = "auth_state_pending";
    private OnDataPointListener onDataPointListener;
    private final DataType DATA_TYPE = DataType.TYPE_HEART_RATE_BPM;
    private boolean authInProgress = false;
    //Cliente de la API
    private GoogleApiClient mClient;
    private String TAG = "HR_BLE_SENSOR_CR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr);
        if (savedInstanceState != null) {
            authInProgress = savedInstanceState.getBoolean(AUTH_PENDING);
        }
        buildSensor();
        buildBLE();
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
        Fitness.SensorsApi.findDataSources(mClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_BLUETOOTH:
                startBleScan();
                Log.e(TAG, "REQUEST_BLUETOOTH");
                break;
            case REQUEST_OAUTH:
                authInProgress = false;
                if (resultCode == RESULT_OK) {
                    if (!mClient.isConnecting() && !mClient.isConnected()) {
                        mClient.connect();
                    }
                } else if (resultCode == RESULT_CANCELED) {
                    Log.e(TAG, "RESULT_CANCELED");
                }
                break;
            default:
                Log.e(TAG, "problem");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        if (i == GoogleApiClient.ConnectionCallbacks.CAUSE_NETWORK_LOST) {
            Log.d(TAG, "Se perdió la conexión: Caída de la red.");
        } else if (i
                == GoogleApiClient.ConnectionCallbacks.CAUSE_SERVICE_DISCONNECTED) {
            Log.d(TAG, "Se perdió la conexión: Servicio Desconectado");
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!authInProgress) {
            try {
                authInProgress = true;
                connectionResult.startResolutionForResult(HRActivity.this, REQUEST_OAUTH);
            } catch (IntentSender.SendIntentException e) {

            }
        } else {
            Log.e(TAG, "authInProgress");
        }
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        mClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //Sensor de Fitness
        Fitness.SensorsApi.remove(mClient, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            mClient.disconnect();
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
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.SESSIONS_API)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
    }

    //BLE
    private void buildBLE() {
        BleScanCallback callback = new BleScanCallback() {
            @Override
            public void onDeviceFound(BleDevice device) {
                Log.d(TAG, "Found bluetooth Device: " + device.getName() + " " + device.getAddress());
                PendingResult<Status> pendingResult = Fitness.BleApi.claimBleDevice(mClient, device);
                Toast.makeText(getApplicationContext(), "Conexion con la pulsera: ", Toast.LENGTH_LONG).show();
                Log.d(TAG, "Claimed bluetooth Device");
            }

            @Override
            public void onScanStopped() {
                Log.d(TAG, "Escaneo interrumpido");
            }

        };

        StartBleScanRequest request = new StartBleScanRequest.Builder()
                .setDataTypes(DATA_TYPE)
                .setBleScanCallback(callback)
                .build();

        if (mClient != null) {
            PendingResult<Status> pendingResult = Fitness.BleApi.startBleScan(mClient, request);
            Log.d(TAG, "Se encontraron las fuentes");
            Log.d(TAG, "Resultado pendiente: " + pendingResult.toString());

        } else {
            Log.d(TAG, "No se creó el Cliente de la API");
        }
    }


    //Sensor de Fitness
    private void registerFitnessDataListener(DataSource dataSource, DataType dataType) {
        SensorRequest request = new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(dataType)
                .setSamplingRate(10, TimeUnit.SECONDS)
                .build();

        onDataPointListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (final Field field : dataPoint.getDataType().getFields()) {
                    Log.i(TAG, "Lectura: " + dataPoint.getValue(field).asInt());
                }
            }
        };
        Fitness.SensorsApi.add(mClient, request, onDataPointListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            Log.e(TAG, "SensorApi successfully added");
                        }
                    }
                });
    }

    private void startBleScan() {
        BleScanCallback callback = new BleScanCallback() {
            @Override
            public void onDeviceFound(BleDevice device) {
                claimBleDevice(device);
            }

            @Override
            public void onScanStopped() {
                Log.e(TAG, "onScanStopped");
            }
        };

        StartBleScanRequest request = new StartBleScanRequest.Builder()
                .setDataTypes(DATA_TYPE)
                .setBleScanCallback(callback)
                .build();

        PendingResult<Status> pendingResult =
                Fitness.BleApi.startBleScan(mClient, request);
        pendingResult.setResultCallback(mResultCallback);
    }

    //BLE
    private void claimBleDevice(BleDevice bleDevice) {
        PendingResult<Status> pendingResult =
                Fitness.BleApi.claimBleDevice(mClient, bleDevice);
        pendingResult.setResultCallback(mResultCallback);
    }
}
