package de.gdoeppert.klimastatistik.gui;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.IOException;
import java.util.Vector;

import de.gdoeppert.klima.model.Jahre;
import de.gdoeppert.klima.model.JahresVerlauf;
import de.gdoeppert.klima.model.JahresverlaufNds;
import de.gdoeppert.klima.model.TageswerteNds;
import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 30.12.15.
 */
public class JahresViewPhen extends ViewHandler {


    @Override
    public int getHelpID() {
        return R.string.jahr_phen_help;
    }

    @Override
    public void doDraw(Paint painter, Canvas canvas) {
        if (tgWerte == null) return;
        try {
            jvln.makeSvg(tgWerte, null, new AndroidGraphicsWriter(getActivity().getResources().getDimension(R.dimen.textsize), painter, canvas));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setViewParam(Vector<?> v) {

        tgWerte = (Vector<TageswerteNds>) v;

    }

    @Override
    public Vector doWork() {

        startWork();
        JahresverlaufNds jvln = new JahresverlaufNds();

        jvln.setDbBean(activity.getDb());
        jvln.setStation(activity.getStation());
        JahresVerlauf jvl = new JahresVerlauf();
        jvl.setFensterVerl(activity.getSettings().winPhen);
        Jahre jahre = new Jahre();
        jahre.setVon(String.valueOf(activity.getSettings().vglJahr));
        jahre.setBis(String.valueOf(activity.getSettings().vglJahr));
        jahre.setPeriode(activity.getSettings().jahre);

        jvln.setJahre(jahre);
        jvln.setJahresverlauf(jvl);

        Vector<?> result = jvln.getTage(null);
        return result;
    }


    private Vector<TageswerteNds> tgWerte;

    private de.gdoeppert.klima.guibase.JahresverlaufNds jvln = new de.gdoeppert.klima.guibase.JahresverlaufNds();

}
