package sv.edu.utec.mail.clinica.Utilidades;

import android.content.Context;
import android.graphics.Color;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

import sv.edu.utec.mail.clinica.AppControl.Control;
import sv.edu.utec.mail.clinica.POJO.Lectura;

public class Graficador {
    private Graficador() {

    }

    public static LineGraphSeries<DataPoint> llenarSerie(Lectura[] lecturas, final Context context) {
        DataPoint[] dataPoints = new DataPoint[lecturas.length + 1];
        dataPoints[lecturas.length] = new DataPoint(lecturas.length, Control.usrPasosHoy.valor);
        int i = lecturas.length - 1;
        for (Lectura lectura : lecturas) {
            dataPoints[i] = new DataPoint(i, lectura.valor);
            i--;
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setColor(Color.rgb(0,51,102));
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                String y = String.valueOf(Math.floor(dataPoint.getY()));
                Toast.makeText(context, y + " pasos", Toast.LENGTH_SHORT).show();

            }
        });
        return series;
    }

    public static LineGraphSeries<DataPoint> lineaMeta() {
        LineGraphSeries<DataPoint> series = new LineGraphSeries<DataPoint>(new DataPoint[]{
                new DataPoint(0, 5000),
                new DataPoint(10, 5000)
        });
        series.setColor(Color.RED);
        return series;
    }

    public static String[] getEtiquetas(int quantity) {
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(Control.todayMillis());
        cal.add(Calendar.DAY_OF_MONTH, (-1 * quantity));
        SimpleDateFormat sp = new SimpleDateFormat("dd-MM");
        String[] etiquetas = new String[5];
        for (int i = 0; i < 5; i++) {
            etiquetas[i] = sp.format(cal.getTime());
            cal.add(Calendar.DAY_OF_MONTH, 2);
        }
        return etiquetas;
    }
}
