package sv.edu.utec.mail.clinica.Utilidades;

import android.content.Context;
import android.graphics.Color;
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

        DataPoint[] dataPoints = new DataPoint[lecturas.length];
        int i = 1;
        for (Lectura lectura : lecturas) {
            dataPoints[i] = new DataPoint(i, lectura.valor);
            i++;
        }
        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(dataPoints);
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10);
        series.setColor(Color.CYAN);
        series.setOnDataPointTapListener(new OnDataPointTapListener() {
            @Override
            public void onTap(Series series, DataPointInterface dataPoint) {
                String y = new Double(dataPoint.getY()).toString();
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
}
