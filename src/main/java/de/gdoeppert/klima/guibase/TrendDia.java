package de.gdoeppert.klima.guibase;

import java.io.IOException;
import java.util.Vector;

import de.gdoeppert.klima.model.Trend.TrendWert;

public class TrendDia {

    String[] col = {"blue", "green", "red", "brown", "black"};
    private double dmy;

    public void makeSvg(Vector<TrendWert> jahre, IGraphicsWriterBase writer)
            throws IOException {

        if (jahre.isEmpty()) {
            return;
        }

        idx0 = 0;
        while (idx0 < jahre.size() && jahre.get(idx0) == null)
            idx0++;
        idx1 = jahre.size() - 1;
        while (idx1 > 0 && jahre.get(idx1) == null)
            idx1--;
        if (idx0>idx1) {
            return;
        }

        writer.start();

        double mnx = jahre.get(idx0).jahr;
        double mxx = jahre.get(idx1).jahr;

        dx = 1500.0 / (mxx - mnx);

        mny = 5000;
        mxy = -5000;
        height = 850;
        width = 1550;

        int[] anz = new int[5];

        for (int j = idx0; j <= idx1; j++) {
            TrendWert tv = jahre.get(j);
            if (tv == null)
                continue;
            for (int c = 0; c < 5; c++) {
                if (tv.tage[c] > 0) {
                    double t = tv.getWertAvg()[c];
                    if (!Double.isNaN(t)) {
                        mny = Math.min(mny, t);
                        mxy = Math.max(mxy, t);
                        anz[c]++;
                    }
                }
            }
        }


        dmy = (height - 20) / (mxy - mny);

        drawAxes(writer, jahre);

        if (jahre != null)
            for (int jz = 0; jz < 5; jz++) {
                IPath path = writer.createPath();
                boolean first = true;
                for (int j = idx0; j <= idx1; j++) {
                    TrendWert tv = jahre.get(j);
                    if (tv == null || tv.tage[jz] < 80
                            || Double.isNaN(tv.wert[jz])) {
                        continue;
                    }
                    double t = 0;
                    t = tv.getWertAvg()[jz];
                    int x = (int) (70 + dx * (j - idx0));
                    int y = height - (int) ((t - mny) * dmy);
                    if (first) {
                        path.moveTo(x, y);
                        first = false;
                    } else {
                        path.lineTo(x, y);
                    }
                    writer.writePath(path, col[jz], 1);
                }
            }
        writer.finish();

    }

    void drawAxes(IGraphicsWriterBase writer, Vector<TrendWert> jahre)
            throws IOException {

        writer.writeRect(-100, -10, width + 100, height + 60, "white");

        double step = Math.pow(10,
                Math.floor(Math.log(mxy - mny) / Math.log(10)));

        int nsteps = (int) ((mxy - mny) / step);

        if (nsteps < 5) {
            step /= 2;
            nsteps *= 2;
        }
        double mny_step = Math.ceil(mny / step) * step;

        String yformat = "%2.0f";
        if (step < 1)
            yformat = "%3.1f";
        else if (step > 100)
            yformat = "%3.0f";

        double y1 = mny_step + 0.3 * step;
        if (y1 - mny > 0.8 * step) {
            y1 -= 0.6 * step;
        }

        writer.writeText(4, (height - (y1 - mny) * dmy), "Winter", col[0], 8);

        double y2 = Math.floor(mxy / step) * step - 0.3 * step;
        if (mxy - y2 > 0.8 * step) {
            y2 += 0.6 * step;
        }
        writer.writeText(4, (height - (y2 - mny) * dmy), "Sommer", col[2], 8);

        y1 = mny_step + (0.3 + (y1 > mny_step ? 1 : 0)) * step;

        writer.writeText(4, (height - (y1 - mny) * dmy), "Frühl.", col[1], 8);
        y1 += 0.4 * step;
        writer.writeText(4, (height - (y1 - mny) * dmy), "Jahr", col[4], 8);
        y1 += 0.6 * step;
        writer.writeText(4, (height - (y1 - mny) * dmy), "Herbst", col[3], 8);

        for (double y = mny_step; y <= mxy; y += step) {
            writer.writePath(
                    writer.createPath().moveTo(70, height - (y - mny) * dmy)
                            .lineTo(1550, height - (y - mny) * dmy), "black", 1);

            writer.writeText(40, height - (y - mny) * dmy,
                    String.format(yformat, y), "black", 8);
        }

        for (int j = idx0; j <= idx1; j++) {
            if (jahre.get(j) == null)
                continue;
            if (jahre.get(j).jahr % 10 == 0) {

                writer.writeText(75 + (j - idx0) * dx, height + 20,
                        String.format("%04d", jahre.get(j).jahr), "black", 8);

                writer.writePath(
                        writer.createPath()
                                .moveTo(70 + (j - idx0) * dx, height + 10)
                                .lineTo(70 + (j - idx0) * dx, 20), "black", 3);
            } else if (jahre.get(j).jahr % 10 == 5) {
                writer.writePath(
                        writer.createPath()
                                .moveTo(70 + (j - idx0) * dx, height)
                                .lineTo(70 + (j - idx0) * dx, 20), "black", 3);

            } else {
                writer.writePath(
                        writer.createPath()
                                .moveTo(70 + (j - idx0) * dx, height)
                                .lineTo(70 + (j - idx0) * dx, 20), "black", 1);

            }
        }
    }

    private double dx;
    private double mny;
    private double mxy;
    private int height;
    private int width;
    private int idx0;
    private int idx1;

}
