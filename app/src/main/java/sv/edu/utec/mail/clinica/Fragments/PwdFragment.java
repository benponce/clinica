package sv.edu.utec.mail.clinica.Fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;

import sv.edu.utec.mail.clinica.POJO.Usuario;
import sv.edu.utec.mail.clinica.R;

public class PwdFragment extends Fragment {
    Usuario usr;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pwd, container, false);
        try {
            Gson gson = new Gson();
            SharedPreferences settings = this.getActivity().getSharedPreferences("clinica", 0);
            usr = gson.fromJson(settings.getString("Usuario", ""), Usuario.class);

        } catch (Exception e) {
            Log.i("Usuario", "No se encontró");
        }
        return view;
    }
}
