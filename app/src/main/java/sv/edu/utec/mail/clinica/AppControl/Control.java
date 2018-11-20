package sv.edu.utec.mail.clinica.AppControl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;

import sv.edu.utec.mail.clinica.CitaActivity;
import sv.edu.utec.mail.clinica.HRActivity;
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

}
