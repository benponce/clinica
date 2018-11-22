package sv.edu.utec.mail.clinica.AppControl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.evernote.android.job.JobRequest;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import sv.edu.utec.mail.clinica.CitaActivity;
import sv.edu.utec.mail.clinica.HRActivity;
import sv.edu.utec.mail.clinica.Jobs.StepsSyncJob;
import sv.edu.utec.mail.clinica.LoginActivity;
import sv.edu.utec.mail.clinica.MainActivity;
import sv.edu.utec.mail.clinica.POJO.Citas;
import sv.edu.utec.mail.clinica.POJO.Lectura;
import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.PerfilActivity;
import sv.edu.utec.mail.clinica.StepsActivity;

public class Control {
    public static Usuario sysUsr;
    public static Lectura[] usrPasos;
    public static Citas[] usrCitas;
    public static Lectura usrPasosHoy;

    public static void RedirectMain(Context context) {
        //Abrir MainActivity
        context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP).addCategory(Intent.CATEGORY_HOME));
    }

    public static void Salir(Context context) {
        sysUsr = null;
        //Borrar registro del usuario
        SharedPreferences sp = context.getSharedPreferences("clinica", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("Usuario", "");
        editor.commit();
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
        context.startActivity(new Intent(context, HRActivity.class));
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

    public static void iniciarConteo(Context context) {
        Gson gson = new Gson();
        SharedPreferences settings = context.getSharedPreferences("clinica", 0);
        try {
            usrPasosHoy = gson.fromJson(settings.getString("PasosHoy", ""), Lectura.class);
            Log.i("CARGA_PASOS", usrPasosHoy.valor + "");
        } catch (Exception e) {
            Calendar cal = new GregorianCalendar();
            cal.setTimeInMillis(Control.todayMillis());
            String fechaHoy = new SimpleDateFormat("yyyyMMdd").format(cal.getTime());
            usrPasosHoy = new Lectura();
            usrPasosHoy.fecha = fechaHoy;
            usrPasosHoy.valor = 0;
            Log.d("PasosHoy", e.getMessage());
        }
    }

    public static void guardarConteo(Context context) {
        Gson gson = new Gson();
        SharedPreferences sp = context.getSharedPreferences("clinica", 0);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString("PasosHoy", gson.toJson(usrPasosHoy));
        editor.commit();
    }

    public static long todayMillis() {
        //Colocar a las 0 horas
        Calendar cal = new GregorianCalendar();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTimeInMillis();
    }

    public static void guardarPasos() {
        //Colocar a las 0 horas
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(todayMillis());
        //10 pm
        cal.add(Calendar.HOUR, 21);
        long ini = cal.getTimeInMillis();
        //6 am
        cal.add(Calendar.HOUR, 8);
        long fin = cal.getTimeInMillis();

        new JobRequest.Builder(StepsSyncJob.TAG)
                .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                .setExecutionWindow(ini, fin)
                .build()
                .schedule();

    }

}
