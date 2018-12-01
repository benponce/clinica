package sv.edu.utec.mail.clinica;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;

import java.util.HashMap;
import java.util.Map;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.Red.ClienteRest;
import sv.edu.utec.mail.clinica.Services.StepCounterService;
import sv.edu.utec.mail.clinica.Utilidades.Graficador;

public class StepsActivity extends AppCompatActivity {

    TextView mBanner;
    GraphView graph;
    Button mGuardar;
    int mCuenta;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_steps);

        SharedPreferences sp = getSharedPreferences("clinica", 0);
        mCuenta = sp.getInt("PasosHoy", 0);
        mBanner = findViewById(R.id.txtPasos);
        mBanner.setText("Pasos de hoy: " + mCuenta);
        //
        graph = findViewById(R.id.graphSteps);
        graph.getViewport().setBackgroundColor(Color.rgb(208, 231, 255));
        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(8);
        graph.getViewport().setYAxisBoundsManual(true);
        graph.getViewport().setMinY(0);
        graph.getViewport().setMaxY(10000);
        graph.addSeries(Graficador.lineaMeta());
        graph.getGridLabelRenderer().setHorizontalLabelsColor(Color.rgb(0, 51, 102));
        graph.getGridLabelRenderer().setVerticalLabelsColor(Color.rgb(0, 51, 102));
        //
        if (Control.usrPasos != null) {
            StaticLabelsFormatter staticLabelsFormatter = new StaticLabelsFormatter(graph);
            staticLabelsFormatter.setHorizontalLabels(Graficador.getEtiquetas(Control.usrPasos.length));
            graph.getGridLabelRenderer().setLabelFormatter(staticLabelsFormatter);
            graph.addSeries(Graficador.llenarSerie(Control.usrPasos, StepsActivity.this, mCuenta));
        } else {
            Toast.makeText(this, "No tienes registro de pasos", Toast.LENGTH_LONG).show();
        }
        //
        mGuardar = findViewById(R.id.btnGuardarPasos);
        mGuardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                guardar();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mStepReceiver, stepUpdateIntentFilter());
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mStepReceiver);
    }

    private static IntentFilter stepUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(StepCounterService.ACTION_STEP_COUNT);
        return intentFilter;
    }


    private final BroadcastReceiver mStepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (StepCounterService.ACTION_STEP_COUNT.equals(action)) {
                mCuenta += intent.getIntExtra(StepCounterService.STEP_COUNT, 0);
                mBanner.setText("Pasos de hoy: " + mCuenta);
            }
        }
    };

    private void guardar() {
        Map<String, String> params = new HashMap<String, String>();
        params.put("unidad", "Pasos");
        params.put("codigo_pac", String.valueOf(Control.sysUsr.paciente));
        params.put("codigo_vitales", "7");
        params.put("fecha", Control.getFechaActual());
        params.put("valor", String.valueOf(mCuenta));
        ClienteRest.getInstance(this).addToRequestQueue(
                ClienteRest.subirDatos(this, Request.Method.POST, ClienteRest.getRegistroVitalesUrl()
                        , "Pasos registrados con éxito", "No se pudo establecer la conexión con el servidor", params));
    }

}
