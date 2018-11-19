package sv.edu.utec.mail.clinica;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.AppCompatActivity;

import sv.edu.utec.mail.clinica.AppControl.NonSwipeableViewPager;
import sv.edu.utec.mail.clinica.Fragments.CitaCalFragment;
import sv.edu.utec.mail.clinica.Fragments.CitaDetFragment;
import sv.edu.utec.mail.clinica.POJO.Citas;

public class CitaActivity extends AppCompatActivity
        implements CitaDetFragment.CitaDetListener, CitaCalFragment.CitaCalListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private NonSwipeableViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cita);
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

    }

    @Override
    public void onFechaSelecionada(Citas citas) {

    }

    @Override
    public void onRegresar() {

    }

    @Override
    public Citas getCitaSeleccionada() {
        return null;
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            /*if(position==1){
                CitaDetFragment detFragment= new CitaDetFragment();
                return detFragment;
            }else{*/
            CitaCalFragment calFragment = new CitaCalFragment();
            return calFragment;
            //}
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
