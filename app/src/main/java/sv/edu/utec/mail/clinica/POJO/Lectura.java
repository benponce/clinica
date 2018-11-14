package sv.edu.utec.mail.clinica.POJO;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Lectura {
    @SerializedName("codigo_vitales")
    public long id;

    @SerializedName("nombre||''||apellido")
    public String nombre;

    @SerializedName("valor")
    public int valor;

    @SerializedName("fecha")
    public Date fecha;
}
