package sv.edu.utec.mail.clinica.POJO;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Citas implements Serializable {
    @SerializedName("codigo_cita")
    public int codigo_cita;

    @SerializedName("fecha")
    public String fecha;

    @SerializedName("descripcion")
    public String descripcion;

    @SerializedName("medico")
    public String medico;
}
