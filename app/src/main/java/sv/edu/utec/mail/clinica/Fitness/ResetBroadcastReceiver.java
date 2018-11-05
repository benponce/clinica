package sv.edu.utec.mail.clinica.Fitness;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class ResetBroadcastReceiver extends BroadcastReceiver {

    // Pasos diarios guardados en SharedPreferences
    private SharedPreferences sharedPrefStepCumulutative;

    //Instancia
    private static ResetBroadcastReceiver INSTANCE = null;

    //Constructor privado
    private ResetBroadcastReceiver() {
        super();
    }

    public static synchronized ResetBroadcastReceiver getInstance() {
        if (INSTANCE == null)
        { 	INSTANCE = new ResetBroadcastReceiver();
        }
        return INSTANCE;
    }

    //Lectura del conteo de pasos almacenado
    public int readStepSaveMidnight (Context main) {
        sharedPrefStepCumulutative = PreferenceManager.getDefaultSharedPreferences(main);
        return sharedPrefStepCumulutative.getInt("STEP_CUMULUTATIVE",0);
    }

    //Escritura del conteo de pasos actualizado
    public void saveStepSaveMidnight(Context main, int n) {
        sharedPrefStepCumulutative = PreferenceManager.getDefaultSharedPreferences(main);
        sharedPrefStepCumulutative.edit().putInt("STEP_CUMULUTATIVE",n).apply();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        //Actualizar los pasos diarios
        int n = readStepSaveMidnight(context);
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit().putInt("THE_STEP_AT_MIDNIGHT",n).apply();
    }
}
