package sv.edu.utec.mail.clinica.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.OnDataPointListener;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Fitness.StepWrapper;

public class StepCounterService extends Service implements OnDataPointListener,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private StepWrapper scClient;

    public StepCounterService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        scClient = StepWrapper.getInstance(this);
        scClient.buildClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Control.iniciarConteo(this);
        Log.i("STEP_SERVICIO", "Servicio invocado");
        if (Control.usrPasosHoy != null) {
            Log.i("STEP_SERVICIO", "Valor guardado " + Control.usrPasosHoy.valor);
            Control.guardarConteo(this);
        }
        scClient.iniciar(new OnDataPointListener() {
            @Override
            public void onDataPoint(DataPoint dataPoint) {
                for (final Field field : dataPoint.getDataType().getFields()) {
                    Control.usrPasosHoy.valor += dataPoint.getValue(field).asInt();
                    Log.i("STEP_SERVICIO", "PASOS: " + dataPoint.getValue(field).asInt());
                }
            }
        });
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("STEP_SERVICIO", "Servicio detenido");
        scClient.stop();
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        scClient.connected();
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
