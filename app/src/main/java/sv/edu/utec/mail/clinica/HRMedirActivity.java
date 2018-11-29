package sv.edu.utec.mail.clinica;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Red.ClienteRest;
import sv.edu.utec.mail.clinica.Services.BluetoothLeService;

public class HRMedirActivity extends AppCompatActivity {
    private final static String TAG = "MEDICION";

    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mDataField;
    private Button mMedir;
    private Button mGuardar;
    private Switch mSwicth;
    private String mDeviceAddress;
    private BluetoothLeService mBluetoothLeService;
    public static String HEART_RATE_SERVICE = "00000af0-0000-1000-8000-00805f9b34fb";
    public static String HEART_RATE_MEASUREMENT = "00000af2-0000-1000-8000-00805f9b34fb";
    BluetoothGattCharacteristic CHAR_HEART_RATE;
    private boolean mConnected = false;
    private BluetoothGattCharacteristic mNotifyCharacteristic;

    //Conexión al servicio BLE
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BluetoothLeService.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("Conectado");
                mSwicth.setChecked(true);
                mMedir.setEnabled(true);
            } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("Desconectado");
                mSwicth.setChecked(false);
                mMedir.setEnabled(false);
                mGuardar.setEnabled(false);
                clearUI();
            } else if (BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                displayGattServices(mBluetoothLeService.getSupportedGattServices());
            } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hr_med);

        final Intent intent = getIntent();
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        mSwicth = findViewById(R.id.swConectar);
        mDataField = (TextView) findViewById(R.id.txtHRData);
        mMedir = (Button) findViewById(R.id.btnMedir);
        mMedir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                leer();
            }
        });
        mGuardar = (Button) findViewById(R.id.btnGuardar);
        mGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar();
            }
        });
        Intent gattServiceIntent = new Intent(this, BluetoothLeService.class);
        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
    }

    private void leer() {
        final int charaProp = CHAR_HEART_RATE.getProperties();
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_READ) > 0) {
            if (mNotifyCharacteristic != null) {
                mBluetoothLeService.setCharacteristicNotification(
                        mNotifyCharacteristic, false);
                mNotifyCharacteristic = null;
            }
            mBluetoothLeService.readCharacteristic(CHAR_HEART_RATE);
        }
        if ((charaProp | BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) {
            mNotifyCharacteristic = CHAR_HEART_RATE;
            mBluetoothLeService.setCharacteristicNotification(CHAR_HEART_RATE, true);
        }
        mGuardar.setEnabled(true);
    }

    private void guardar() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("unidad", "bpm");
        params.put("codigo_pac", String.valueOf(Control.sysUsr.paciente));
        params.put("codigo_vitales", "8");
        params.put("fecha", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Calendar.getInstance().getTime()));
        params.put("valor", mDataField.getText().toString());
        ClienteRest.getInstance(this).addToRequestQueue(
                ClienteRest.subirDatos(this, Request.Method.POST, ClienteRest.getRegistroVitalesUrl()
                        , "Medición registrada con éxito.", "No se pudo establecer la conexión con el servidor.", params));
    }

    private void clearUI() {
        mDataField.setText(0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Resultado: " + result);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindService(mServiceConnection);
        mBluetoothLeService = null;
    }

    private void updateConnectionState(final String msj) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(HRMedirActivity.this, msj, Toast.LENGTH_LONG);
            }
        });
    }

    private void displayData(String data) {
        if (data != null) {
            mDataField.setText(data);
            mGuardar.setEnabled(true);
        }
    }

    private void displayGattServices(List<BluetoothGattService> gattServices) {
        Log.d(TAG, "Sí los registra");
        if (gattServices == null) return;

        BluetoothGattService mGattService = null;
        for (BluetoothGattService gattService : gattServices) {
            if (gattService.getUuid().toString().equals(HEART_RATE_SERVICE)) {
                Log.d(TAG, "registró el servicio");
                mGattService = gattService;
                break;
            }
        }
        if (mGattService != null) {
            for (BluetoothGattCharacteristic gattCharacteristic : mGattService.getCharacteristics()) {
                if (gattCharacteristic.getUuid().toString().equals(HEART_RATE_MEASUREMENT)) {
                    Log.d(TAG, "registró el la caracteristica");
                    CHAR_HEART_RATE = gattCharacteristic;
                    break;
                }
            }
        }
    }


    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BluetoothLeService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BluetoothLeService.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }
}
