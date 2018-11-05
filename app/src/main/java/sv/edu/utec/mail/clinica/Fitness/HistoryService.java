package sv.edu.utec.mail.clinica.Fitness;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.common.api.Scope;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.result.DailyTotalResult;

import java.text.DateFormat;
import java.util.concurrent.TimeUnit;

public class HistoryService implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    //History
    public GoogleApiClient mClient = null;

    private static HistoryService INSTANCE = null;

    private HistoryService() {

    }


    public static synchronized HistoryService getInstance() {
        if (INSTANCE == null)
        { 	INSTANCE = new HistoryService();
        }
        return INSTANCE;
    }

    //Cliente
    public void buildFitnessClientHistory(FitClient client) {
        //Google API Client
        mClient = new GoogleApiClient.Builder(client)
                .addApi(Fitness.HISTORY_API)
                .addScope(new Scope(Scopes.FITNESS_ACTIVITY_READ_WRITE))
                .addConnectionCallbacks(client)
                .enableAutoManage(client, 0, client)
                .build();
    }

    //Ver pasos de hoy
    public void displayStepDataForToday() {
        DailyTotalResult result = Fitness.HistoryApi.readDailyTotal( mClient, DataType.TYPE_STEP_COUNT_DELTA ).await(1, TimeUnit.SECONDS);
        showDataSet(result.getTotal());
    }

    private void showDataSet(DataSet dataSet) {
        Log.e("History", "Datos registrados para el Data type: " + dataSet.getDataType().getName());
        DateFormat dateFormat = DateFormat.getDateInstance();
        DateFormat timeFormat = DateFormat.getTimeInstance();

        for (DataPoint dp : dataSet.getDataPoints()) {
            Log.e("History", "Data point:");
            Log.e("History", "\tTipo: " + dp.getDataType().getName());
            Log.e("History", "\tInicio: " + dateFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            Log.e("History", "\tFin: " + dateFormat.format(dp.getEndTime(TimeUnit.MILLISECONDS)) + " " + timeFormat.format(dp.getStartTime(TimeUnit.MILLISECONDS)));
            for(Field field : dp.getDataType().getFields()) {
                //Log.e("History", "\tField: " + field.getName() + " Value: " + dp.getValue(field));
                //lab.addFitActivity("Field: " + field.getName() + " Value: " + dp.getValue(field));
            }
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Log.e("HistoryService", "onConnected");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e("HistoryService", "onConnectionSuspended");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {Log.e("HistoryService", "onConnectionFailed");}

}
