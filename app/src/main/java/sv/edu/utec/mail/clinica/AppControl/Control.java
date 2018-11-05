package sv.edu.utec.mail.clinica.AppControl;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import sv.edu.utec.mail.clinica.LoginActivity;
import sv.edu.utec.mail.clinica.MainActivity;

import static android.support.v4.content.ContextCompat.startActivity;

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
}
