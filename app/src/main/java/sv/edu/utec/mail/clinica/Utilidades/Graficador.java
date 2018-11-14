package sv.edu.utec.mail.clinica.Utilidades;

import android.content.Context;
import android.widget.Toast;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.DataPointInterface;
import com.jjoe64.graphview.series.LineGraphSeries;
import com.jjoe64.graphview.series.OnDataPointTapListener;
import com.jjoe64.graphview.series.Series;

import sv.edu.utec.mail.clinica.POJO.Lectura;

public class Graficador {
    private Graficador() {

    }

    public static LineGraphSeries<DataPoint> llenarSerie(Lectura[] lecturas, final Context context) {

        DataPoint[] dataPoints = new DataPoint[lecturas.length + 1];
        int i = 1;
        dataPoints[0] = new DataPoint(0, 0);
        for (Lectura lectura : lecturas) {
            dataPoints[i] = new DataPoint(i, lectura.valor);
            i++;
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                String y = new Double(dataPoint.getY()).toString();
                Toast.makeText(context, y + " pasos", Toast.LENGTH_SHORT).show();

            }
        });
        return series;
    }
}
