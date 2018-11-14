package sv.edu.utec.mail.clinica.POJO;

import com.google.gson.annotations.SerializedName;

public class Usuario {
    @SerializedName("user_id")
    public long id;

    @SerializedName("username")
    public String userName;

    @SerializedName("password")
    public String pwd;

    @SerializedName("first_name")
    public String firstName;

    @SerializedName("last_name")
    public String lastName;

    @SerializedName("email")
    public String email;

    @SerializedName("codigo_pac")
    public int paciente;
}
