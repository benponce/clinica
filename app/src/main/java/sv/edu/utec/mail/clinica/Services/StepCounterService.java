package sv.edu.utec.mail.clinica.Services;

import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.PersistableBundle;
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
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataSourcesRequest;
import com.google.android.gms.fitness.request.OnDataPointListener;
import com.google.android.gms.fitness.request.SensorRequest;
import com.google.android.gms.fitness.result.DataSourcesResult;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.concurrent.TimeUnit;

import sv.edu.utec.mail.clinica.AppControl.Control;

public class StepCounterService extends Service implements OnDataPointListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public final static String ACTION_STEP_COUNT = "sv.edu.utec.mail.clinica.ACTION_STEP_COUNT";
    public final static String ACTION_SAVE_COUNTER = "sv.edu.utec.mail.clinica.ACTION_SAVE_COUNTER";
    public final static String ACTION_RESET_COUNTER = "sv.edu.utec.mail.clinica.ACTION_RESET_COUNTER";
    public final static String ACTION_REGISTRAR_ACT = "sv.edu.utec.mail.clinica.ACTION_REGISTRAR_ACT";
    public final static String STEP_COUNT = "sv.edu.utec.mail.clinica.STEP_COUNT";
    private final String TAG = "STEP_COUNTER";
    //
    private final DataType DATA_TYPE = DataType.TYPE_STEP_COUNT_DELTA;
    public GoogleApiClient mClient;
    //DataPointListener
    private OnDataPointListener onDataPointListener = new OnDataPointListener() {
        @Override

        public void onDataPoint(DataPoint dataPoint) {
            for (final Field field : dataPoint.getDataType().getFields()) {
                broadcastUpdate(dataPoint.getValue(field).asInt());
            }
        }
    };
    //
    private Handler mHandler;
    private int mCuenta;
    private boolean mProgramada;
    private boolean mEnviada;

    public StepCounterService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mHandler = new Handler();
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                programarActualizacion();
                mHandler.postDelayed(this, 3600000);
            }
        }, 3600000);
        buildClient();
        mClient.connect();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            if (intent.getAction().equals(ACTION_SAVE_COUNTER)) {
                guardar();
            } else if (intent.getAction().equals(ACTION_RESET_COUNTER)) {
                mCuenta = 0;
            } else if (intent.getAction().equals(ACTION_REGISTRAR_ACT)) {
                mEnviada = true;
            }
        } else {
            SharedPreferences sp = getSharedPreferences("clinica", 0);
            mCuenta = sp.getInt("PasosHoy", 0);
        }
        Log.d(TAG,"Pasos al inicio:"+mCuenta);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        guardar();
        Log.i("STEP_SERVICIO", "Servicio detenido");
        Fitness.SensorsApi.remove(mClient, onDataPointListener)
                .setResultCallback(new ResultCallback<Status>() {
                    @Override
                    public void onResult(Status status) {
                        if (status.isSuccess()) {
                            mClient.disconnect();
                        }
                    }
                });

        Log.e(TAG, "STOPED");
    }

    private void guardar() {
        SharedPreferences sp = getSharedPreferences("clinica", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putInt("PasosHoy", mCuenta);
        editor.commit();
    }

    private void programarActualizacion() {
        Calendar cal = new GregorianCalendar();
        long currentMillis = cal.getTimeInMillis();
        cal.setTimeInMillis(Control.todayMillis());
        cal.add(Calendar.MILLISECOND, 1);//00 horas
        long ini = cal.getTimeInMillis();
        cal.add(Calendar.HOUR, 23);//11 pm
        long fin = cal.getTimeInMillis();

        if (fin < currentMillis && !mProgramada) {
            PersistableBundle extra = new PersistableBundle();
            extra.putString("fecha", Control.getFechaActual());
            extra.putInt("pasos", mCuenta);

            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(this, StepSyncService.class))
                    .setMinimumLatency(1)
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setExtras(extra)
                    .setPersisted(true);
            JobScheduler jobScheduler = (JobScheduler) getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
            mProgramada = true;
        }
        if (ini < currentMillis && mEnviada && mProgramada) {
            mProgramada = false;
            mEnviada = false;
        }
    }

    private void broadcastUpdate(final int steps) {
        mCuenta += steps;
        final Intent intent = new Intent(ACTION_STEP_COUNT);
        intent.putExtra(STEP_COUNT, steps);
        sendBroadcast(intent);
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
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
                        registerFitnessDataListener(dataSource, DATA_TYPE, 3);
                    }
                }
            }
        };
        Fitness.SensorsApi.findDataSources(mClient, dataSourceRequest)
                .setResultCallback(dataSourcesResultCallback);
    }

    public void buildClient() {
        mClient = new GoogleApiClient.Builder(this)
                .addApi(Fitness.HISTORY_API)
                .addApi(Fitness.SENSORS_API)
                .addScope(new Scope(Scopes.FITNESS_BODY_READ_WRITE))
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
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
                            Log.e(TAG, "API Agregada");
                        }
                    }
                });
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("SERVICE", "Conexion suspendida");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        Log.e("SERVICE", "La conexión falló");
    }


}
