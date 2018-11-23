package sv.edu.utec.mail.clinica.Fitness;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

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
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;
import com.google.gson.Gson;

import java.util.concurrent.TimeUnit;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.POJO.Lectura;

public class StepWrapper implements OnDataPointListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = "WRAPPER";
    public static Lectura mPasos;
    private static StepWrapper mInstance;
    private static Context context;
    private final DataType DATA_TYPE = DataType.TYPE_STEP_COUNT_DELTA;
    public GoogleApiClient mClient;
    private OnDataPointListener onDataPointListener;

    private StepWrapper() {

    }

    public static synchronized StepWrapper getInstance(Context cxt) {

        context = cxt;
        if (mInstance == null) {
            Gson gson = new Gson();
            SharedPreferences sp = cxt.getSharedPreferences("clinica", 0);
            mPasos = gson.fromJson(sp.getString("PasosHoy", ""), Lectura.class);
            mInstance = new StepWrapper();
        }
        return mInstance;
    }

    public void buildClient() {
        mClient = new GoogleApiClient.Builder(context)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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

    private void registerFitnessDataListener(DataSource dataSource, DataType data_type) {
        SensorRequest request = new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(data_type)
                .setSamplingRate(5, TimeUnit.SECONDS)
                .build();

        /*onDataPointListener = new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (final Field field : dataPoint.getDataType().getFields()) {
                    mPasos.valor += dataPoint.getValue(field).asInt();
                }
            }
        };*/
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

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "Conexion suspendida");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e(TAG, "Falla de conexion");
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {

    }

    public void iniciar(OnDataPointListener listener) {
        onDataPointListener = listener;
        mClient.connect();
    }

    public void stop() {
        Fitness.SensorsApi.remove(mClient, this)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            mClient.disconnect();
                        }
                    }
                });
        Control.guardarConteo(context);
        Log.e(TAG, "STOPED");
    }

}
