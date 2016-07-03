package de.gdoeppert.klimastatistik.gui;

import android.graphics.Typeface;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Vector;

import de.gdoeppert.klima.model.Jahre;
import de.gdoeppert.klima.model.Monatswerte;
import de.gdoeppert.klima.model.Tagesmittel;
import de.gdoeppert.klima.model.Wertermittler;
import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 12.04.16.
 */
public class HistorieHandler extends WerteHandler implements View.OnClickListener {

    @Override
    public int getHelpID() {
        return R.string.hist_help;
    }

    @Override
    public void setViewParam(Vector<?> v) {

        Log.i("HistorieHandler", "setViewParam");

        tvv = (Vector<Wertermittler.Tval>) v;
    }

    @Override
    public boolean needsUpdate() {

        if (super.needsUpdate()) return true;

        Log.d("HistorieHandler", "needs update: check heute");

        Calendar heute = activity.getHeute();

        int monat1 = heute.get(Calendar.MONTH) + 1;
        int jahr1 = heute.get(Calendar.YEAR);

        return (monat != monat1 || jahr != jahr1);
    }

    @Override
    public Vector<Wertermittler.Tval> doWork() {

        startWork();

        Calendar heute = activity.getHeute();

        Monatswerte tm = new Monatswerte();
        tm.setDbBean(activity.getDb());
        tm.setStation(activity.getStation());

        monat = heute.get(Calendar.MONTH) + 1;
        jahr = heute.get(Calendar.YEAR);
        tm.setMonat(monat);
        tm.setJahr(jahr);

        Wertermittler.Tval tv = tm.getWerte();
        Vector<Wertermittler.Tval> tvv = new Vector<Wertermittler.Tval>();
        if (tv != null) {
            tvv.add(tv);
            partial = tm.isPartial();
        }

        Tagesmittel tmm = new Tagesmittel();
        tmm.setDbBean(activity.getDb());
        tmm.setStation(activity.getStation());
        Jahre jahre = new Jahre();
        jahre.setPeriode(activity.getSettings().jahre);
        tmm.setJahre(jahre);
        Tagesmittel.TagesParam tp = new Tagesmittel.TagesParam();

        tp.monat = monat;
        tp.tag = -1;
        tmm.setTagesParam(tp);

        tv = tmm.getWerte();
        if (tv != null) {
            tvv.add(tv);
        }

        return tvv;
    }

    @Override
    public int getLayoutId() {
        return R.layout.historie;
    }

    @Override
    public void postprocess() {

        Log.i("HistorieHandler", "postprocess0");

        Calendar heute = activity.getHeute();

        if (tvv == null || heute == null) {
            return;
        }

        Log.i("HistorieHandler", "postprocess");

        TextView txt = (TextView) overview.findViewById(R.id.jahr_heute);
        txt.setText(String.format("%04d", jahr));
        txt.setClickable(true);
        txt.setOnClickListener(this);

        txt = (TextView) overview.findViewById(R.id.monat);
        txt.setText(monatsnamen[monat]);

        TableRow row = (TableRow) overview.findViewById(R.id.row_schneeanteil);
        row.setVisibility(monat < 5 || monat > 9 ? View.VISIBLE : View.GONE);

        row = (TableRow) overview.findViewById(R.id.row_schneehoehe);
        row.setVisibility(monat < 5 || monat > 9 ? View.VISIBLE : View.GONE);
        row.setEnabled(monat < 5 || monat > 9);

        if (tvv.size() > 0) {

            Wertermittler.Tval tv = tvv.get(0);

            txt = (TextView) overview.findViewById(R.id.min_mitt);
            txt.setText(tv.getTnS());

            txt = (TextView) overview.findViewById(R.id.min_abs);
            txt.setText(tv.getTnkS());

            txt = (TextView) overview.findViewById(R.id.mitt_mitt);
            txt.setText(tv.getTmS());

            txt = (TextView) overview.findViewById(R.id.max_mitt);
            txt.setText(tv.getTxS());

            txt = (TextView) overview.findViewById(R.id.max_abs);
            txt.setText(tv.getTxkS());

            txt = (TextView) overview.findViewById(R.id.rs_mitt);
            txt.setText(tv.getRsS());
            txt.setTypeface(null, partial ? Typeface.ITALIC : Typeface.NORMAL);

            txt = (TextView) overview.findViewById(R.id.sd_mitt);
            txt.setText(tv.getSdS());
            txt.setTypeface(null, partial ? Typeface.ITALIC : Typeface.NORMAL);

            txt = (TextView) overview.findViewById(R.id.nm_mitt);
            txt.setText(tv.getNmS());

            txt = (TextView) overview.findViewById(R.id.schnee);
            txt.setText(tv.getSchneeS());

            txt = (TextView) overview.findViewById(R.id.schneeAnt);
            txt.setText(tv.getSchneeAntS());

            txt = (TextView) overview.findViewById(R.id.ndsAnt);
            txt.setText(tv.getNdsAntS());

            txt = (TextView) overview.findViewById(R.id.heiterAnt);
            txt.setText(tv.getHeiterAntS());

            txt = (TextView) overview.findViewById(R.id.truebAnt);
            txt.setText(tv.getTruebAntS());


            DistributionView dv = (DistributionView) overview.findViewById(R.id.distribution);
            if (dv != null && tv.tmDist != null) {
                dv.setDistribution(tv.tmDist);
                dv.setTmMittel(tv.getTm());
            }

            WetterlagenView wv = (WetterlagenView) overview.findViewById(R.id.wlrichtung_mon);
            wv.setPercentages(tv.wl);
            wv.invalidate();

            try {
                txt = (TextView) overview.findViewById(R.id.n_f_mon);
                txt.setText(tv.wl.format(tv.wl.percentages.get("F")));
            } catch (Throwable th) {
                txt.setText("?");
            }
            try {

                txt = (TextView) overview.findViewById(R.id.n_t_mon);
                txt.setText(tv.wl.format(tv.wl.percentages.get("T")));
            } catch (Throwable th) {
                txt.setText("?");
            }
        }
        if (tvv.size() > 1) {

            Wertermittler.Tval tv = tvv.get(1);

            txt = (TextView) overview.findViewById(R.id.min_mitt_mon);
            txt.setText(tv.getTnS());

            txt = (TextView) overview.findViewById(R.id.min_abs_mon);
            txt.setText(tv.getTnkS());

            txt = (TextView) overview.findViewById(R.id.mitt_mitt_mon);
            txt.setText(tv.getTmS());

            txt = (TextView) overview.findViewById(R.id.max_mitt_mon);
            txt.setText(tv.getTxS());

            txt = (TextView) overview.findViewById(R.id.max_abs_mon);
            txt.setText(tv.getTxkS());

            txt = (TextView) overview.findViewById(R.id.rs_mitt_mon);
            txt.setText(tv.getRsS());

            txt = (TextView) overview.findViewById(R.id.sd_mitt_mon);
            txt.setText(tv.getSdS());

            txt = (TextView) overview.findViewById(R.id.nm_mitt_mon);
            txt.setText(tv.getNmS());

            txt = (TextView) overview.findViewById(R.id.schnee_mon);
            txt.setText(tv.getSchneeS());

            txt = (TextView) overview.findViewById(R.id.schneeAnt_mon);
            txt.setText(tv.getSchneeAntS());

            txt = (TextView) overview.findViewById(R.id.ndsAnt_mon);
            txt.setText(tv.getNdsAntS());

            txt = (TextView) overview.findViewById(R.id.heiterAnt_mon);
            txt.setText(tv.getHeiterAntS());

            txt = (TextView) overview.findViewById(R.id.truebAnt_mon);
            txt.setText(tv.getTruebAntS());
        }

    }

    @Override
    public void setView(View v) {
        overview = v;
        Button btn = (Button) overview.findViewById(R.id.jahr_incr);
        btn.setOnClickListener(this);
        btn = (Button) overview.findViewById(R.id.jahr_decr);
        btn.setOnClickListener(this);
    }


    private View overview = null;
    private Vector<Wertermittler.Tval> tvv = null;

    boolean partial = true;
    int monat = 0;
    int jahr = 2999;
}
