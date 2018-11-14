package sv.edu.utec.mail.clinica.Utilidades;

import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import sv.edu.utec.mail.clinica.POJO.Lectura;

public class Graficador {
    private Graficador() {

    }

    public static LineGraphSeries<DataPoint> llenarSerie(Lectura[] lecturas) {

        DataPoint[] dataPoints = new DataPoint[lecturas.length + 1];
        int i = 1;
        dataPoints[0] = new DataPoint(0, 0);
        for (Lectura lectura : lecturas) {
            dataPoints[i] = new DataPoint(i, lectura.valor);
            i++;
        }
        return new LineGraphSeries<>(dataPoints);
    }
}
