package sv.edu.utec.mail.clinica.Fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import sv.edu.utec.mail.clinica.POJO.Citas;
import sv.edu.utec.mail.clinica.R;

public class CitaDetFragment extends Fragment {

    TextView mMedico;
    TextView mMotivo;
    TextView mFecha;
    Button mRegresar;
    Button mAgendar;
    private CitaDetListener mListener;
    private Citas mCita;

    public CitaDetFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_cita_det, container, false);
        //Iniciar views
        mFecha = v.findViewById(R.id.txtDlgFecha);
        mMedico = v.findViewById(R.id.txtDlgMedico);
        mMotivo = v.findViewById(R.id.txtDlgDesc);
        mAgendar = v.findViewById(R.id.btnCitaAgendar);
        mRegresar = v.findViewById(R.id.btnCitaRegresar);
        //Colocar valores y acciones en las view
        mFecha.setText(mCita.fecha);
        mMedico.setText(mCita.medico);
        mMotivo.setText(mCita.descripcion);
        mRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRegresar();
            }
        });
        mAgendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                agendar();
            }
        });
        //Retorno por defecto del método
        return v;

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (CitaDetListener) context;
            //mCita = mListener.getCitaSeleccionada();
        } catch (Exception e) {
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void agendar() {

    }

    public interface CitaDetListener {
        void onRegresar();

        Citas getCitaSeleccionada();
    }
}
