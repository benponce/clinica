package sv.edu.utec.mail.clinica.POJO;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class Citas {
    @SerializedName("codigo_cita")
    public int codigo_cita;

    @SerializedName("fecha")
    public Date fecha;

    @SerializedName("descripcion")
    public String descripcion;

    @SerializedName("medico")
    public String medico;
}
