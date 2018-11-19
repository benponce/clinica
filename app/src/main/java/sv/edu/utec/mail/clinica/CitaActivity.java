package sv.edu.utec.mail.clinica;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import sv.edu.utec.mail.clinica.AppControl.NonSwipeableViewPager;
import sv.edu.utec.mail.clinica.Fragments.CitaCalFragment;
import sv.edu.utec.mail.clinica.Fragments.CitaDetFragment;
import sv.edu.utec.mail.clinica.POJO.Citas;

public class CitaActivity extends AppCompatActivity
        implements CitaDetFragment.CitaDetListener, CitaCalFragment.CitaCalListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private NonSwipeableViewPager mViewPager;
    Citas mCita;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cita);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);
        //Temporalmente
        mCita = new Citas();
        mCita.medico = "Dr Matínez";
        mCita.codigo_cita = 1;
        mCita.descripcion = "Migraña";
        mCita.fecha = "20181201";
    }

    @Override
    public void onFechaSelecionada(Citas cita) {
        mCita = cita;
        mViewPager.setCurrentItem(1);
    }

    @Override
    public void onRegresar() {
        mViewPager.setCurrentItem(0);
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            if (position == 1) {
                CitaDetFragment detFragment = new CitaDetFragment();
                Toast.makeText(CitaActivity.this, mCita.fecha + " " + mCita.medico, Toast.LENGTH_LONG).show();
                Bundle args = new Bundle();
                args.putSerializable("CitaSelec", mCita);
                detFragment.setArguments(args);
                return detFragment;
            } else {
                CitaCalFragment calFragment = new CitaCalFragment();
                return calFragment;
            }
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
