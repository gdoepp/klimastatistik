package de.gdoeppert.klimastatistik.gui;

import android.app.DatePickerDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Vector;

import de.gdoeppert.klima.model.Jahre;
import de.gdoeppert.klima.model.Tagesmittel;
import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 07.01.16.
 */
public class MittelHandler extends WerteHandler implements View.OnClickListener, DatePickerDialog.OnDateSetListener {

    @Override
    public int getHelpID() {
        return R.string.mittel_help;
    }

    @Override
    public void setViewParam(Vector<?> v) {

        Log.i("MittelHandler", "setViewParam");

        tvv = (Vector<Tagesmittel.Tval>) v;
    }

    @Override
    public boolean needsUpdate() {

        if (super.needsUpdate()) return true;

        Log.d("MittelHandler", "needs update: check heute");

        Calendar heute = activity.getHeute();

        int monat1 = heute.get(Calendar.MONTH) + 1;
        int tag1 = heute.get(Calendar.DAY_OF_MONTH);

        return (monat != monat1 || tag != tag1);
    }

    @Override
    public Vector<Tagesmittel.Tval> doWork() {

        startWork();

        Calendar heute = activity.getHeute();

        tp.monat = heute.get(Calendar.MONTH) + 1;
        tp.tag = heute.get(Calendar.DAY_OF_MONTH);

        monat = tp.monat;
        tag = tp.tag;


        Tagesmittel tm = new Tagesmittel();
        tm.setDbBean(activity.getDb());
        tm.setStation(activity.getStation());
        Jahre jahre = new Jahre();
        jahre.setVon(String.valueOf(activity.getSettings().vglJahr));
        jahre.setBis(String.valueOf(activity.getSettings().vglJahr));
        jahre.setPeriode(activity.getSettings().jahre);
        tm.setJahre(jahre);
        tp = new Tagesmittel.TagesParam();

        heute = activity.getHeute();

        tp.monat = heute.get(Calendar.MONTH) + 1;
        tp.tag = heute.get(Calendar.DAY_OF_MONTH);
        tm.setTagesParam(tp);

        Tagesmittel.Tval tv = tm.getWerte();
        Vector<Tagesmittel.Tval> tvv = new Vector<Tagesmittel.Tval>();
        if (tv != null) {
            tvv.add(tv);
        }

        if (false && this.tvv != null && this.tvv.size() > 1 && this.tvv.get(1).monat_i == tp.monat) {
            tvv.add(this.tvv.get(1));
        } else {
            tp.tag = -1;
            tm.setTagesParam(tp);
            tv = tm.getWerte();
            if (tv != null) {
                tvv.add(tv);
            }
        }

        return tvv;
    }

    @Override
    public int getLayoutId() {
        return R.layout.mittelwerte;
    }

    @Override
    public void postprocess() {

        Log.i("MittelHandler", "postprocess0");

        Calendar heute = activity.getHeute();

        if (tvv == null || heute == null) {
            return;
        }

        Log.i("MittelHandler", "postprocess");


        TextView txt = (TextView) overview.findViewById(R.id.tag_heute);
        txt.setText(String.format("%02d.%02d.", tag, monat));
        txt.setClickable(true);
        txt.setOnClickListener(this);


        txt = (TextView) overview.findViewById(R.id.tag_heute2);
        txt.setText(String.format("%02d.%02d.", tag, monat));

        txt = (TextView) overview.findViewById(R.id.monat_heute);
        txt.setText(monatsnamen[monat]);

        txt = (TextView) overview.findViewById(R.id.monat_heute2);
        txt.setText(monatsnamen[monat]);

        txt = (TextView) overview.findViewById(R.id.monat_heute7);
        txt.setText(monatsnamen[monat]);

        TableRow row = (TableRow) overview.findViewById(R.id.row_schneeanteil);
        row.setVisibility(monat < 5 || monat > 9 ? View.VISIBLE : View.GONE);

        row = (TableRow) overview.findViewById(R.id.row_schneehoehe);
        row.setVisibility(monat < 5 || monat > 9 ? View.VISIBLE : View.GONE);
        row.setEnabled(monat < 5 || monat > 9);

        if (tvv.size() > 0) {

            Tagesmittel.Tval tv = tvv.get(0);

            txt = (TextView) overview.findViewById(R.id.min_mitt);
            txt.setText(tv.getTnS());

            txt = (TextView) overview.findViewById(R.id.mitt_mitt);
            txt.setText(tv.getTmS());

            txt = (TextView) overview.findViewById(R.id.max_mitt);
            txt.setText(tv.getTxS());

            txt = (TextView) overview.findViewById(R.id.rs_mitt);
            txt.setText(tv.getRsS());

            txt = (TextView) overview.findViewById(R.id.sd_mitt);
            txt.setText(tv.getSdS());

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

/*
            WindroseView wv = (WindroseView) overview.findViewById(R.id.wlrichtung_tag);
            wv.setPercentages(tv.getN_nw(), tv.getN_no(), tv.getN_so(), tv.getN_sw(), tv.getN_xx());
            wv.invalidate();

            txt = (TextView) overview.findViewById(R.id.n_f);
            txt.setText(tv.getN_f());

            txt = (TextView) overview.findViewById(R.id.n_t);
            txt.setText(tv.getN_t());
  */
        }

        if (tvv.size() > 1) {

            Tagesmittel.Tval tv = tvv.get(1);

            DistributionView dv = (DistributionView) overview.findViewById(R.id.distribution);
            if (dv != null && tv.tmDist != null) {
                dv.setDistribution(tv.tmDist);
                dv.setTmMittel(tv.getTm());
            }



            txt = (TextView) overview.findViewById(R.id.min_mitt_mon);
            txt.setText(tv.getTnS());

            txt = (TextView) overview.findViewById(R.id.mitt_mitt_mon);
            txt.setText(tv.getTmS());

            txt = (TextView) overview.findViewById(R.id.max_mitt_mon);
            txt.setText(tv.getTxS());


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
    }

    @Override
    public void setView(View v) {
        overview = v;
        Button btn = (Button) overview.findViewById(R.id.tag_decr);
        btn.setOnClickListener(this);
        btn = (Button) overview.findViewById(R.id.tag_incr);
        btn.setOnClickListener(this);
        btn = (Button) overview.findViewById(R.id.mon_decr);
        btn.setOnClickListener(this);
        btn = (Button) overview.findViewById(R.id.mon_incr);
        btn.setOnClickListener(this);
    }

    private View overview = null;
    private Vector<Tagesmittel.Tval> tvv = null;
    private Tagesmittel.TagesParam tp = new Tagesmittel.TagesParam();

    int monat = 99;
    int tag = 99;

}
