package sv.edu.utec.mail.clinica.POJO;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Lectura {
    @SerializedName("valor")
    public int valor;

    @SerializedName("fecha")
    public Date fecha;
}
