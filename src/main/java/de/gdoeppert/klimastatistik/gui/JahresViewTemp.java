package de.gdoeppert.klimastatistik.gui;

import android.graphics.Canvas;
import android.graphics.Paint;

import java.io.IOException;
import java.util.Vector;

import de.gdoeppert.klima.guibase.JahresverlaufT;
import de.gdoeppert.klima.model.Jahre;
import de.gdoeppert.klima.model.JahresVerlauf;
import de.gdoeppert.klima.model.JahresverlaufTemp;
import de.gdoeppert.klima.model.TagesWerteT;
import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 30.12.15.
 */
public class JahresViewTemp extends ViewHandler {


    @Override
    public int getHelpID() {
        return R.string.jahr_temp_help;
    }


    @Override
    public void doDraw(Paint painter, Canvas canvas) {
        if (tgWerte == null) return;
        try {
            jvlt.makeSvg(tgWerte, null, new AndroidGraphicsWriter(getActivity().getResources().getDimension(R.dimen.textsize), painter, canvas));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setViewParam(Vector<?> v) {
        tgWerte = (Vector<TagesWerteT>) v;
    }

    @Override
    public Vector<TagesWerteT> doWork() {
        startWork();
        JahresverlaufTemp jvlt = new JahresverlaufTemp();
        jvlt.setDbBean(activity.getDb());
        jvlt.setStation(activity.getStation());
        JahresVerlauf jvl = new JahresVerlauf();
        jvl.setFensterVerl(activity.getSettings().winTemp);
        Jahre jahre = new Jahre();
        jahre.setVon(String.valueOf(activity.getSettings().vglJahr));
        jahre.setBis(String.valueOf(activity.getSettings().vglJahr));
        jahre.setPeriode(activity.getSettings().jahre);

        jvlt.setJahre(jahre);
        jvlt.setJahresverlauf(jvl);

        Vector<TagesWerteT> result = jvlt.getTage(null);
        return result;

    }

    private Vector<TagesWerteT> tgWerte;

    final private JahresverlaufT jvlt = new JahresverlaufT();

}
