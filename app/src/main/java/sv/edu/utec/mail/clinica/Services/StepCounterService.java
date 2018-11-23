package sv.edu.utec.mail.clinica.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.OnDataPointListener;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Fitness.StepWrapper;

public class StepCounterService extends Service implements OnDataPointListener {

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
        Control.iniciarConteo(this);
        scClient = StepWrapper.getInstance(this);
        scClient.buildClient();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scClient.iniciar(this);
        Log.i("STEP_SERVICIO", "Servicio iniciado");
        Control.iniciarConteo(this);
        Log.i("STEP_SERVICIO", Control.usrPasosHoy.fecha);
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i("STEP_SERVICIO", "Servicio detenido");
        scClient.stop();
    }

    @Override
    public void onDataPoint(DataPoint dataPoint) {
        for (final Field field : dataPoint.getDataType().getFields()) {
            Control.usrPasosHoy.valor += dataPoint.getValue(field).asInt();
        }
    }
}
