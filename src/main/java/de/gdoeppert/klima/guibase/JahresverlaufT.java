package de.gdoeppert.klima.guibase;

import java.io.IOException;
import java.util.Vector;

import de.gdoeppert.klima.model.TagesWerteT;

public class JahresverlaufT {

    String[] col = {"green", "blue", "red", "violet", "brown", "orange",
            "magenta", "pink"};

    public void makeSvg(Vector<TagesWerteT> tage, Vector<TagesWerteT> tageVgl,
                        IGraphicsWriterBase writer) throws IOException {

        int nLines;

        if (tageVgl != null) {
            Vector<TagesWerteT> tageDiff = new Vector<TagesWerteT>();
            for (int j = 0; j < tage.size() && j < tageVgl.size(); j++) {
                TagesWerteT diff = new TagesWerteT();
                diff.monat = tageVgl.get(j).monat;
                diff.tag = tageVgl.get(j).tag;
                diff.tm = tageVgl.get(j).tm - tage.get(j).tm;
                diff.tn = tageVgl.get(j).tn - tage.get(j).tn;
                diff.tx = tageVgl.get(j).tx - tage.get(j).tx;
                diff.tm_o = diff.tm;
                diff.tm_u = diff.tm;
                diff.tnk = diff.tn;
                diff.txk = diff.tx;
                tageDiff.add(diff);
            }
            tage = tageDiff;
            nLines = 3;
        } else {
            nLines = 5;
        }

        writer.start();


        mny = 50;
        mxy = -50;
        height = 880;
        width = 1600;
        x1 = 90;
        dx = (width - x1) / 365f;

        for (int j = 0; j < tage.size(); j++) {
            TagesWerteT tv = tage.get(j);
            mny = (float) Math.min(mny, Math.min(tv.tm_u, Math.min(tv.tn, tv.tnk)));
            mxy = (float) Math.max(mxy, Math.max(tv.tm_o, Math.max(tv.tx, tv.txk)));
        }

        dmy = height * 1.0f / (mxy - mny);

        drawAxes(writer);

        if (tage != null) {

            int lw = 2;
            if (nLines > 3) {
                IPath fpath = writer.createPath();
                for (int j = 0; j < tage.size(); j++) {
                    TagesWerteT tv = tage.get(j);
                    double t = 0;
                    t = tv.tm_u;

                    int x = (int) (x1 + dx * j);
                    int y = height - (int) ((t - mny) * dmy);
                    if (j == 0) {
                        fpath.moveTo(x, y);
                    } else {
                        fpath.lineTo(x, y);
                    }
                }
                for (int j = tage.size() - 1; j >= 0; j--) {
                    TagesWerteT tv = tage.get(j);
                    double t = 0;
                    t = tv.tm_o;

                    int x = (int) (x1 + dx * j);
                    int y = height - (int) ((t - mny) * dmy);
                    if (j == 0) {
                        fpath.moveTo(x, y);
                    } else {
                        fpath.lineTo(x, y);
                    }
                }
                writer.writeFilledPath(fpath, "cyan", 0.2, 1);
            }

            for (int c = 0; c < nLines; c++) {
                IPath path = writer.createPath();
                for (int j = 0; j < tage.size(); j++) {
                    TagesWerteT tv = tage.get(j);
                    double t = 0;
                    switch (c) {
                        case 0:
                            t = tv.tm;
                            break;
                        case 1:
                            t = tv.tn;
                            break;
                        case 2:
                            t = tv.tx;
                            break;
                        case 3:
                            t = tv.tnk;
                            break;
                        case 4:
                            t = tv.txk;
                            break;
                    }
                    int x = (int) (x1 + dx * j);
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
            if (j % 5 == 0) {
                double y = height - (j - mny) * dmy;

                IPath path = writer.createPath();
                path.moveTo(x1, (int) y);
                path.lineTo(x1 + 365 * dx, (int) y);

                writer.writePath(path, "black", (j == 0 ? 4 : 2));

                writer.writeText(25, height - (j - mny) * dmy, j + "°",
                        "black", 10);
            }
        }

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

        writeLegend(writer);

    }

    private void writeLegend(IGraphicsWriterBase writer) throws IOException {


        writer.writeRect(150, 5, 280, 210, "white");

        writer.writeText(160, 195, "Tmitt", col[0], 8);

        writer.writeText(160, 130, "Tmitt", "cyan", 8);
        writer.writeText(160, 155, "+\u03C3", "cyan", 8);

        writer.writeText(160, 90, "Tmax", col[2], 8);
        writer.writeText(160, 30, "Tmax", col[4], 8);
        writer.writeText(160, 55, "abs", col[4], 8);

        writer.writeRect(800, 635, 900, 810, "white");

        writer.writeText(810, 665, "Tmitt", "cyan", 8);
        writer.writeText(810, 690, "-\u03C3", "cyan", 8);
        writer.writeText(810, 730, "Tmin", col[1], 8);
        writer.writeText(810, 770, "Tmin", col[3], 8);
        writer.writeText(810, 795, "abs", col[3], 8);
    }

    private float dx;
    private float mny;
    private float mxy;
    private int height;
    private int width;
    private float dmy;
    private int x1;

}
