package sv.edu.utec.mail.clinica.AppControl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import sv.edu.utec.mail.clinica.CitaActivity;
import sv.edu.utec.mail.clinica.HRConActivity;
import sv.edu.utec.mail.clinica.LoginActivity;
import sv.edu.utec.mail.clinica.MainActivity;
import sv.edu.utec.mail.clinica.POJO.Citas;
import sv.edu.utec.mail.clinica.POJO.Lectura;
import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.PerfilActivity;
import sv.edu.utec.mail.clinica.Services.StepCounterService;
import sv.edu.utec.mail.clinica.StepsActivity;

public class Control {
    public static Usuario sysUsr;
    public static Lectura[] usrPasos;
    public static Citas[] usrCitas;

    public static void RedirectMain(Context context) {
        //Abrir MainActivity
        context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP).addCategory(Intent.CATEGORY_HOME));
    }

    public static void Salir(Context context) {
        //Cerrar el servicio
        Intent intent = new Intent(context, StepCounterService.class);
        intent.setAction(StepCounterService.ACTION_CERRAR);
        context.startService(intent);
        sysUsr = null;
        //Borrar registro del usuario
        SharedPreferences sp = context.getSharedPreferences("clinica", 0);
        sp.edit().clear().commit();
        //Redireccionar a Login
        context.startActivity(new Intent(context, LoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP).addCategory(Intent.CATEGORY_HOME));
    }

    public static void Pasos(Context context) {
        //Abrir StepsActivity
        context.startActivity(new Intent(context, StepsActivity.class));
    }

    public static void Perfil(Context context) {
        //Abrir PerfilActivity
        context.startActivity(new Intent(context, PerfilActivity.class));
    }

    public static void Citas(Context context) {
        //Abrir PerfilActivity
        context.startActivity(new Intent(context, CitaActivity.class));
    }

    public static void HR(Context context) {
        //Abrir PerfilActivity
        context.startActivity(new Intent(context, HRConActivity.class));
    }

    public static void readOffLine(Context context) {
        Gson gson = new Gson();
        SharedPreferences settings = context.getSharedPreferences("clinica", 0);
        try {
            usrCitas = gson.fromJson(settings.getString("Citas", ""), Citas[].class);
        } catch (Exception e) {
            usrCitas = null;
            Log.d("Citas", e.getMessage());
        }
        try {
            usrPasos = gson.fromJson(settings.getString("Pasos", ""), Lectura[].class);
        } catch (Exception e) {
            usrPasos = null;
            Log.d("Pasos", e.getMessage());
        }
    }

    public static long todayMillis() {
        //Colocar a las 0 horas
        Calendar cal = new GregorianCalendar();
        cal.setTimeZone(TimeZone.getDefault());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static String getFechaActual() {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(Control.todayMillis());
        return new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
    }
}
