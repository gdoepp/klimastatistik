package de.gdoeppert.klima.guibase;

import java.io.IOException;
import java.util.Vector;

import de.gdoeppert.klima.model.TageswerteNds;

public class JahresverlaufNds {

    public void makeSvg(Vector<TageswerteNds> tage,
                        Vector<TageswerteNds> tageVgl, IGraphicsWriterBase writer)
            throws IOException {

        if (tageVgl != null) {
            Vector<TageswerteNds> tageDiff = new Vector<TageswerteNds>();
            for (int j = 0; j < tage.size() && j < tageVgl.size(); j++) {
                TageswerteNds diff = new TageswerteNds();
                diff.monat = tageVgl.get(j).monat;
                diff.tag = tageVgl.get(j).tag;
                diff.fm = tageVgl.get(j).fm - tage.get(j).fm;
                diff.hm = tageVgl.get(j).hm - tage.get(j).hm;
                diff.sd = tageVgl.get(j).sd - tage.get(j).sd;
                diff.rs = tageVgl.get(j).rs - tage.get(j).rs;
                diff.nm = tageVgl.get(j).nm - tage.get(j).nm;
                tageDiff.add(diff);
            }
            tage = tageDiff;
        }
        writer.start();

        mny = 50;
        mxy = -50;
        height = 880;
        width = 1600;
        x1 = 100;
        dx = (width - x1) / 365f;

        for (int j = 0; j < tage.size(); j++) {
            TageswerteNds tv = tage.get(j);
            if (!Double.isNaN(tv.sd) && !Double.isNaN(tv.fm)
                    && !Double.isNaN(tv.rs) && !Double.isNaN(tv.nm)) {
                mny = Math.min(mny, Math.min(tv.rs,
                        Math.min(tv.sd, Math.min(tv.fm, tv.nm))));
                mxy = Math.max(mxy, Math.max(tv.rs,
                        Math.max(tv.sd, Math.max(tv.fm, tv.nm))));
            }
            if (!Double.isNaN(tv.hm)) {
                mny = Math.min(mny, tv.hm / 10);
                mxy = Math.max(mxy, tv.hm / 10);
            }
        }

        dmy = height / (mxy - mny);

        drawAxes(writer);

        if (tage != null) {
            int lw = 2;

            for (int c = 0; c < 5; c++) {
                IPath path = writer.createPath();
                for (int j = 0; j < tage.size(); j++) {
                    TageswerteNds tv = tage.get(j);
                    double t = 0;
                    switch (c) {

                        case 0:
                            t = tv.rs;
                            break;
                        case 1:
                            t = tv.nm;
                            break;
                        case 2:
                            t = tv.sd;
                            break;
                        case 3:
                            t = tv.fm;
                            break;
                        case 4:
                            t = tv.hm / 10;
                            break;

                    }

                    float x = x1 + dx * j;
                    int y = height - (int) ((t - mny) * dmy);
                    if (j == 0) {
                        path.moveTo(x, y);
                    } else {
                        path.lineTo(x, y);
                    }
                }
                writer.writePath(path, col[c], lw);

            }
        }

        writer.finish();

    }

    void drawAxes(IGraphicsWriterBase writer) throws IOException {

        writer.writeRect(0, 0, width, height + 20, "white");

        // Achsen und Gitter zeichnen
        // horizontal:
        for (int j = (int) Math.ceil(mny); j <= Math.floor(mxy); j++) {
            double y = height - (j - mny) * dmy;
            IPath path = writer.createPath();
            path.moveTo(x1, (int) y);
            path.lineTo(x1 + 365 * dx, (int) y);

            writer.writePath(path, "black", 1);
            if (j < Math.floor(mxy)) {
                writer.writeText(65, height - (j - mny) * dmy, j + "", "black", 9);
            }
        }

        writer.writeText(30, height - (Math.floor(mxy) - mny) * dmy,
                String.format("%2.0f%% ", Math.floor(mxy) * 10), "black", 10);

        // vertikal
        String[] ms = {"Jan", "Febr", "März", "April", "Mai", "Juni", "Juli", "Aug.",
                "Sept.", "Okt.", "Nov.", "Dez.", ""};
        int[] ml = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 0};
        int jm = 0;
        for (int j = 0; j < 13; j++) {

            writer.writeText(x1 + 5 + jm * dx, height + 20, ms[j], "black", 9);

            IPath path = writer.createPath();
            path.moveTo(x1 + jm * dx, height + 20);
            path.lineTo(x1 + jm * dx, 0);
            writer.writePath(path, "black", 2);

            for (int k = 10; k < 28 && jm + k < 366; k += 10) {
                IPath path1 = writer.createPath();
                path1.moveTo(x1 + (jm + k) * dx, height);
                path1.lineTo(x1 + (jm + k) * dx, 0);

                writer.writePath(path1, "black", 1);
            }

            jm += ml[j];
        }


        writer.writeText(1, height - (2.5 - mny) * dmy, "Nds", col[0], 8);

        writer.writeText(1, height - (3.5 - mny) * dmy, "Wind", col[3], 8);

        writer.writeText(1, height - (4.5 - mny) * dmy, "Wolken", col[1], 8);

        writer.writeText(1, height - (5.5 - mny) * dmy, "Sonne", col[2], 8);

        writer.writeText(1, height - (6.5 - mny) * dmy, "Feuchte", col[4], 8);

    }

    String[] col = {"blue", "green", "red", "violet", "brown", "orange",
            "magenta", "pink"};
    private float dx;
    private double mny;
    private double mxy;
    private int height;
    private int width;
    private double dmy;
    private int x1;

}
