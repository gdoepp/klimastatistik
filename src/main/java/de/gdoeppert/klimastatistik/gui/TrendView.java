package de.gdoeppert.klimastatistik.gui;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.IOException;
import java.util.Vector;

import de.gdoeppert.klima.model.Jahre;
import de.gdoeppert.klima.model.Trend;
import de.gdoeppert.klima.model.TrendParm;
import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 30.12.15.
 */
public class TrendView extends ViewHandler {


    @Override
    public int getHelpID() {
        return R.string.trend_help;
    }


    @Override
    public void doDraw(Paint painter, Canvas canvas) {
        if (tgWerte == null) return;
        try {
            trendDia.makeSvg(tgWerte, new AndroidGraphicsWriter(getActivity().getResources().getDimension(R.dimen.textsize), painter, canvas));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setViewParam(Vector<?> v) {
        tgWerte = (Vector<Trend.TrendWert>) v;
    }

    @Override
    public Vector doWork() {

        startWork();
        Trend tr = new Trend();

        tr.setDbBean(activity.getDb());
        tr.setStation(activity.getStation());
        Jahre jahre = new Jahre();
        jahre.setVon(String.valueOf(activity.getSettings().vglJahr));
        jahre.setBis(String.valueOf(activity.getSettings().vglJahr));
        jahre.setPeriode(activity.getSettings().jahre);
        tr.setJahre(jahre);
        TrendParm tpar = new TrendParm();
        tpar.setFensterTrend(activity.getSettings().winTrdTemp);
        tpar.setWertidx(5);
        tr.setTrendParm(tpar);
        dirty = false;
        return tr.getJahreswerte("5");
    }


    private Vector<Trend.TrendWert> tgWerte;

    private de.gdoeppert.klima.guibase.TrendDia trendDia = new de.gdoeppert.klima.guibase.TrendDia();

}
