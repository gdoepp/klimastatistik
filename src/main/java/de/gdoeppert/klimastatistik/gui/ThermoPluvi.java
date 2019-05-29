package de.gdoeppert.klimastatistik.gui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.io.IOException;
import java.util.Vector;

import de.gdoeppert.klima.guibase.Thermopluvi;
import de.gdoeppert.klima.model.Jahre;
import de.gdoeppert.klima.model.JahresVergleich;
import de.gdoeppert.klima.model.MonatValTN;
import de.gdoeppert.klima.model.ThermoPluviogramm;
import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 30.12.15.
 */
public class ThermoPluvi extends ViewHandler {


    @Override
    public int getHelpID() {
        return R.string.tpluvi_help;
    }


    @Override
    public void doDraw0(Paint painter, Canvas canvas) {
        if (monate == null) return;
        painter.setColor(Color.BLACK);
        painter.setTextSize(textsize);
        canvas.drawText(String.format("%d", jahr), textsize, 2 * textsize, painter);
    }

    @Override
    public void doDraw(Paint painter, Canvas canvas) {
        if (monate == null) return;
        try {
            tplu.makeSvg(monate, new AndroidGraphicsWriter(getActivity().getResources().getDimension(R.dimen.textsize), painter, canvas));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setViewParam(Vector<?> v) {
        monate = (Vector<MonatValTN>) v;
    }

    @Override
    public boolean needsLandscape() {
        return true;
    }

    @Override
    public Vector<MonatValTN> doWork() {

        startWork();
        ThermoPluviogramm tp = new ThermoPluviogramm();

        tp.setDbBean(activity.getDb());
        tp.setStation(activity.getStation());
        textsize = activity.getResources().getDimension(R.dimen.textsize) / 20f * 18f;

        Jahre jahre = new Jahre();
        jahre.setVon(String.valueOf(activity.getSettings().vglJahr));
        jahre.setBis(String.valueOf(activity.getSettings().vglJahr));
        jahre.setPeriode(activity.getSettings().jahre);
        tp.setJahre(jahre);
        JahresVergleich jvgl = new JahresVergleich();
        jahr = activity.getSettings().vglJahr;
        if (jahr < 0) {
            jahr = activity.getStation().getSelStat().getJahr2() - 1;
        }
        jvgl.setVglJahr(String.valueOf(jahr));
        tp.setJahresVergleich(jvgl);
        Log.i("ThermoPluvi", "vgl mit " + jahr + " periode:" + jahre.getVon() + " , " + jahre.getBis());
        dirty = false;
        return tp.getMonate();
    }

    private Vector<MonatValTN> monate;
    private float textsize;
    private int jahr;

    final Thermopluvi tplu = new Thermopluvi();

}
