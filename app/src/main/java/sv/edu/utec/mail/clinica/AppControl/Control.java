package sv.edu.utec.mail.clinica.AppControl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.widget.Toast;

import com.google.gson.Gson;

import sv.edu.utec.mail.clinica.CitasActivity;
import sv.edu.utec.mail.clinica.HRActivity;
import sv.edu.utec.mail.clinica.LoginActivity;
import sv.edu.utec.mail.clinica.MainActivity;
import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.PerfilActivity;
import sv.edu.utec.mail.clinica.StepsActivity;

public class Control {
    public static void RedirectMain(Context context){
        //Abrir MainActivity
        context.startActivity(new Intent(context, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP).addCategory(Intent.CATEGORY_HOME));
    }

    public static void Salir(Context context){
        //Borrar registro del usuario
        SharedPreferences sp = context.getSharedPreferences("clinica",0);
        SharedPreferences.Editor editor= sp.edit();
        editor.putString("Usuario","");
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
        context.startActivity(new Intent(context, CitasActivity.class));
    }

    public static void HR(Context context) {
        //Abrir PerfilActivity
        context.startActivity(new Intent(context, HRActivity.class));
    }

    public static Usuario getUsuario(Context context) {
        try {
            Gson gson = new Gson();
            SharedPreferences settings = context.getSharedPreferences("clinica", 0);
            return gson.fromJson(settings.getString("Usuario", ""), Usuario.class);
        } catch (Exception e) {
            Toast.makeText(context, "Error al leer preferencias de usuario", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

}
