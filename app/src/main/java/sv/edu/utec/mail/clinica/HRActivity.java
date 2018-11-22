package sv.edu.utec.mail.clinica;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.fitness.data.DataPoint;

import sv.edu.utec.mail.clinica.Fitness.FitClient;

public class HRActivity extends FitClient {
    private String TAG = "HR_BLE_SENSOR_CR";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

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
}
