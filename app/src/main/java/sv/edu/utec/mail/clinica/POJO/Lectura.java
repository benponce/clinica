package sv.edu.utec.mail.clinica.POJO;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Lectura implements Serializable {
    @SerializedName("valor")
    public int valor;

    @SerializedName("fecha")
    public String fecha;
}
