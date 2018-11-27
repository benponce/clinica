package sv.edu.utec.mail.clinica.Fitness;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.concurrent.TimeUnit;

import sv.edu.utec.mail.clinica.AppControl.Control;

public class StepWrapper {

    public static final String TAG = "WRAPPER";
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
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) context)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) context)
                .build();
    }

    public void connected() {
        DataSourcesRequest dataSourceRequest = new DataSourcesRequest.Builder()
                .setDataTypes(DATA_TYPE)
                .build();

        ResultCallback<DataSourcesResult> dataSourcesResultCallback = new ResultCallback<DataSourcesResult>() {
            @Override
            public void onResult(DataSourcesResult dataSourcesResult) {
                for (DataSource dataSource : dataSourcesResult.getDataSources()) {
                    if (DATA_TYPE.equals(dataSource.getDataType())) {
                        registerFitnessDataListener(dataSource, DATA_TYPE, 3);
                    }
                }
            }
        };
        Fitness.SensorsApi.findDataSources(mClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }

    private void registerFitnessDataListener(DataSource dataSource, DataType data_type, int sampling) {
        SensorRequest request = new SensorRequest.Builder()
                .setDataSource(dataSource)
                .setDataType(data_type)
                .setSamplingRate(sampling, TimeUnit.SECONDS)
                .build();

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

    public void iniciar(OnDataPointListener listener) {
        onDataPointListener = listener;
        mClient.connect();
    }

    public void stop() {
        Fitness.SensorsApi.remove(mClient, onDataPointListener)
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
