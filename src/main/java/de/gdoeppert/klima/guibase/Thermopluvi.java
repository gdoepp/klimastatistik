package de.gdoeppert.klima.guibase;

import android.util.Log;

import java.io.IOException;
import java.util.Comparator;
import java.util.Vector;

import de.gdoeppert.klima.model.MonatValTN;

public class Thermopluvi {

    public void makeSvg(final Vector<MonatValTN> monate, IGraphicsWriterBase writer)
            throws IOException {

        txtHeight = writer.calcRectText("X", 12).h;
        writer.start();
        drawAxes(writer, monate);

        calcMonLabels(monate, writer);

        for (int j = 0; j < monate.size(); j++) {
            drawMonths(writer, monate.get(j), j);
        }
        writer.finish();
    }

    void calcMonLabels(final Vector<MonatValTN> monate, IGraphicsWriterBase writer) {

        float[] monlen = new float[mon.length];

        for (int j = 1; j < mon.length; j++) {
            monlen[j - 1] = writer.calcRectText(mon[j], 10).w;
        }

        Integer[] monidx = new Integer[monate.size()];
        for (int j = 0; j < monidx.length; j++) monidx[j] = j;

        java.util.Arrays.sort(monidx, new Comparator<Integer>() {

            @Override
            public int compare(Integer lhs, Integer rhs) {
                return Double.compare(monate.get(lhs).getTm(), monate.get(rhs).getTm());
            }
        });

        x1 = new double[monate.size()];
        x2 = new double[monate.size()];

        double x10 = 0;
        double x20 = 0;

        for (int j = 0; j < monidx.length; j++) {
            int midx = monidx[j];
            MonatValTN monat = monate.get(midx);

            if (monat.nds < 0) {
                x1[midx] = x10;
                Log.d("thermopluvi", "x1: " + x10);
                x10 += monlen[monat.monat - 1] + 20;
            } else {
                x2[midx] = x20;
                Log.d("thermopluvi", "x2: " + x20);
                x20 += monlen[monat.monat - 1] + 20;
            }
        }

        x10 = 1550;
        x20 = x10;

        for (int j = 0; j < monidx.length; j++) {

            int midx = monidx[monidx.length - j - 1];
            MonatValTN monat = monate.get(midx);

            double x = x0 + (monat.tm * dx);
            Log.d("thermopluvi", "mon " + mon[monat.monat] + " len " + monlen[monat.monat - 1]);
            if (monat.nds < 0) {
                if (x > x1[midx] && x < x10 - monlen[monat.monat - 1] - 20) {
                    x1[midx] = x;
                    Log.d("thermopluvi", "fit_1 x: " + x);
                } else if (x >= x10 - 20 - monlen[monat.monat - 1]) {
                    Log.d("thermopluvi", "right_1 al x: " + (x10 - 20 - monlen[monat.monat - 1]));
                    x1[midx] = x10 - 20 - monlen[monat.monat - 1];
                } else {
                    Log.d("thermopluvi", "left_1 al x: " + (x1[midx]));
                }
                x10 = x1[midx];
            } else {
                if (x > x2[midx] && x < x20 - monlen[monat.monat - 1] - 20) {
                    Log.d("thermopluvi", "fit_2 x: " + x);
                    x2[midx] = x;
                } else if (x >= x20 - 20 - monlen[monat.monat - 1]) {
                    x2[midx] = x20 - 20 - monlen[monat.monat - 1];
                    Log.d("thermopluvi", "right_2 al x: " + (x20 - 20 - monlen[monat.monat - 1]));
                } else {
                    Log.d("thermopluvi", "left_2 al x: " + (x2[midx]));
                }

                x20 = x2[midx];
            }
        }
    }

    void drawAxes(IGraphicsWriterBase writer, Vector<MonatValTN> monate)
            throws IOException {

        double mny = 0;
        double mxy = 0;
        double mnx = 0;
        double mxx = 0;

        height = 800;
        width = 1400;

        for (int j = 0; j < monate.size(); j++) {
            mny = Math.min(mny, monate.get(j).nds);
            mxy = Math.max(mxy, monate.get(j).nds);
            mnx = Math.min(mnx, monate.get(j).tm);
            mxx = Math.max(mxx, monate.get(j).tm);
        }

        dy = (height) / (mxy - mny);
        dx = (width) / (mxx - mnx);
        x0 = 50 + (-mnx) * dx;
        y0 = height + 50 - (-mny) * dy;

        double y1 = y0 - (int) (mny * dy);
        double y2 = y0 - (int) (mxy * dy);
        double x1 = x0 + (int) (mnx * dx);
        double x2 = x0 + (int) (mxx * dx);

        IPath fpathx = writer.createPath();
        IPath fpathy = writer.createPath();

        fpathx.moveTo((int) x1, (int) y0).lineTo((int) x2, (int) y0);
        fpathy.moveTo((int) x0, (int) y1).lineTo((int) x0, (int) y2);

        for (int j = (int) mnx; j <= (int) mxx; j++) {
            fpathx.moveTo(x0 + j * dx, y0 - 10).lineTo(x0 + j * dx, y0 + 10);

        }
        for (int j = (int) (mny / 10); j <= (int) (mxy / 10); j++) {
            fpathy.moveTo(x0 - 10, y0 - dy * j * 10).lineTo(x0 + 10,
                    y0 - dy * j * 10);
        }

        writer.writePath(fpathx, "black", 2);

        writer.writePath(fpathy, "black", 2);

        writer.writeText(x2, y0, String.format("%4.1f°", mxx), "black", 10);

        writer.writeText(x0, y2 + txtHeight, String.format("%3.0f%%", mxy), "black", 10);

        writer.writeText(x1 - 30, y0, String.format("%4.1f°", mnx),
                "black", 12);

        writer.writeText(x0, y1, String.format("%3.0f%%", mny), "black", 10);
    }

    void drawMonths(IGraphicsWriterBase writer, MonatValTN monat, int monidx)
            throws IOException {

        IPath path = writer.createPath();

        float x = (float) x0;
        float y = (float) y0;

        path.moveTo(x, y);

        x += (monat.tm * dx);
        y -= (monat.nds * dy);

        path.lineTo(x, y);

        writer.writePath(path, col[monat.monat], 4);

        path = writer.createPath();
        path.moveTo(x, y);


        if (monat.nds < 0) {
            y = height + 100;
            x = (float) x1[monidx];
        } else {
            y = txtHeight;
            x = (float) x2[monidx];
        }
        path.lineTo(x, y);
        writer.writePath(path, col[monat.monat], 1);

        writer.writeText(x, y, mon[monat.monat], "black", 10);

    }

    final static String[] col = {"", "blue", "blue", "green", "green",
            "green", "red", "red", "red", "brown", "brown", "brown", "blue",
            "blue", "green", "red", "brown", "black"};
    final static String[] mon = {"", "Jan", "Feb", "Mrz", "Apr", "Mai", "Jun",
            "Jul", "Aug", "Sep", "Okt", "Nov", "Dez", "Winter", "Frühling",
            "Sommer", "Herbst", "Jahr"};


    private double x0;
    private double y0;
    private double dy;
    private double dx;
    private int height;
    private int width;
    private float txtHeight;

    double[] x1;
    double[] x2;
}
