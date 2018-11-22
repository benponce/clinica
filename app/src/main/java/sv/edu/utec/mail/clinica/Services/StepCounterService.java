package sv.edu.utec.mail.clinica.Services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;

import sv.edu.utec.mail.clinica.Fitness.StepCounterClient;

public class StepCounterService extends Service {

    private StepCounterClient scClient;

    public StepCounterService() {
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        scClient = StepCounterClient.getInstance(this);
        return Service.START_STICKY;
    }
}
