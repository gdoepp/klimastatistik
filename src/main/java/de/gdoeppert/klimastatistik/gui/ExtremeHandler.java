package de.gdoeppert.klimastatistik.gui;

import android.annotation.TargetApi;
import android.graphics.Color;
import android.os.Build;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Vector;

import de.gdoeppert.klima.model.Extreme;
import de.gdoeppert.klima.model.Tagesmittel;
import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 07.01.16.
 */
public class ExtremeHandler extends WerteHandler {

    @Override
    public int getHelpID() {
        return R.string.extreme_help;
    }

    @Override
    public void setViewParam(Vector<?> v) {
        tvv = (Vector<Extreme.Tval>) v;
    }

    @Override
    public boolean needsUpdate() {

        if (super.needsUpdate()) return true;

        Calendar heute = activity.getHeute();

        Log.d("ExtremeHandler", "needs update: check heute");

        int monat1 = heute.get(Calendar.MONTH) + 1;
        int tag1 = heute.get(Calendar.DAY_OF_MONTH);

        return (monat != monat1 || tag != tag1);
    }

    @Override
    public Vector<Extreme.Tval> doWork() {

        if (activity == null) return null;

        Extreme.TagesParam tp = new Extreme.TagesParam();
        Calendar cal = activity.getHeute();
        tp.monat = cal.get(Calendar.MONTH) + 1;
        tp.tag = cal.get(Calendar.DAY_OF_MONTH);
        monat = tp.monat;
        tag = tp.tag;
        startWork();

        Extreme tex = new Extreme();
        tex.setDbBean(activity.getDb());
        tex.setStation(activity.getStation());
        tex.setTagesParam(tp);

        Extreme.Tval tv = tex.getWerte();

        tvv = new Vector<Extreme.Tval>();
        Log.i("ExtremeHandler", "tv: " + tv);

        if (tv != null) {
            tvv.add(tv);
        }
        tp.tag = -1;
        tex.setTagesParam(tp);

        tv = tex.getWerte();
        // Log.i("ExtremeHandler", "tv: " + tv);

        if (tv != null) {
            tvv.add(tv);
        }
        Log.i("ExtremeHandler", "tvv-size: " + tvv.size());

        Tagesmittel tm = new Tagesmittel();
        tm.setDbBean(activity.getDb());
        tm.setStation(activity.getStation());
        Tagesmittel.TagesParam tp2 = new Tagesmittel.TagesParam();

        tp2.monat = cal.get(Calendar.MONTH) + 1;
        tp2.tag = -1;
        tm.setTagesParam(tp2);

        tvmittel = tm.getWerte();

        return tvv;
    }

    @Override
    public int getLayoutId() {
        return R.layout.extreme;
    }

    @Override
    public void postprocess() {

        if (tvv == null) {
            return;
        }

        Log.i("ExtremeHandler", "tvv-size: " + tvv.size());

        textsize = activity.getResources().getDimension(R.dimen.sp20) / 20f * 18f;
        dp20 = (int) activity.getResources().getDimension(R.dimen.dp20);

        TextView txt = (TextView) overview.findViewById(R.id.tag_heute3);
        txt.setText(String.format("%02d.%02d.", tag, monat));

        txt = (TextView) overview.findViewById(R.id.tag_heute4);
        txt.setText(String.format("%02d.%02d.", tag, monat));

        txt = (TextView) overview.findViewById(R.id.tag_heute5);
        txt.setText(String.format("%02d.%02d.", tag, monat));

        txt = (TextView) overview.findViewById(R.id.monat_heute3);
        txt.setText(monatsnamen[monat]);

        txt = (TextView) overview.findViewById(R.id.monat_heute4);
        txt.setText(monatsnamen[monat]);

        txt = (TextView) overview.findViewById(R.id.monat_heute5);
        txt.setText(monatsnamen[monat]);

        txt = (TextView) overview.findViewById(R.id.monat_heute6);
        txt.setText(monatsnamen[monat]);

        int cap[] = {R.id.extremeTempTag, R.id.extremeTempMonat};

        for (int g = 0; g < tvv.size() && g < 2; g++) {

            Extreme.Tval tv = tvv.get(g);

            ViewGroup vg = (ViewGroup) overview.findViewById(cap[g]);

            for (int r = 0; r < 5; r++) {

                if (tv.min[r] == null || tv.max[r] == null) {
                    continue;
                }

                TableRow tr = getOrCreateTableRow(vg, r, Color.rgb(255, 0, 235), Color.rgb(0xc4, 0x79, 0x08));

                txt = (TextView) tr.getChildAt(0);
                txt.setText(tv.min[r].tag);

                txt = (TextView) tr.getChildAt(1);
                txt.setText(String.format("%4.1f", tv.min[r].wert));

                txt = (TextView) tr.getChildAt(2);
                txt.setText(tv.max[r].tag);

                txt = (TextView) tr.getChildAt(3);
                txt.setText(String.format("%4.1f", tv.max[r].wert));

            }
        }

        LinearLayout lay = (LinearLayout) overview.findViewById(R.id.ext_section_schnee);
        lay.setVisibility(monat < 5 || monat > 9 ? View.VISIBLE : View.GONE);

        int cap2[] = {R.id.extremePhen, R.id.extremePhenMonat};

        for (int g = 0; g < tvv.size(); g++) {

            Extreme.Tval tv = tvv.get(g);

            ViewGroup vg = (ViewGroup) overview.findViewById(cap2[g]);

            if (tv.schnee != null) {
                vg.setVisibility(View.VISIBLE);

                for (int r = 0; r < 5; r++) {

                    TableRow tr = getOrCreateTableRow(vg, r, Color.rgb(255, 255, 235), Color.rgb(255, 255, 255));

                    if (tv.schnee[r] != null) {
                        tr.setVisibility(View.VISIBLE);
                        txt = (TextView) tr.getChildAt(0);
                        txt.setText(tv.schnee[r].tag);

                        txt = (TextView) tr.getChildAt(1);
                        txt.setText(String.format("%4.1f", tv.schnee[r].wert));
                    } else {
                        tr.setVisibility(View.GONE);
                    }
                }
            } else {
                vg.setVisibility(View.GONE);
            }
        }

        Extreme.Tval tv = tvv.get(0);

        ViewGroup vg = (ViewGroup) overview.findViewById(R.id.extremeNdsWindTag);

        for (int r = 0; r < 5; r++) {
            if (tv.nds[r] == null && tv.wind[r] == null) {
                continue;
            }

            TableRow tr = getOrCreateTableRow(vg, r, Color.rgb(0, 0xa0, 0xf0), Color.rgb(0xb0, 0x1f, 0xff));

            if (tv.nds[r] != null) {
                txt = (TextView) tr.getChildAt(0);
                txt.setText(tv.nds[r].tag);

                txt = (TextView) tr.getChildAt(1);
                txt.setText(String.format("%4.1f", tv.nds[r].wert));
            }

            if (tv.wind[r] != null) {

                txt = (TextView) tr.getChildAt(2);
                txt.setText(tv.wind[r].tag);

                txt = (TextView) tr.getChildAt(3);
                txt.setText(String.format("%4.1f", tv.wind[r].wert));
            }
        }


        if (tvv.size() > 1) {

            tv = tvv.get(1);

            vg = (ViewGroup) overview.findViewById(R.id.extremeNdsMonat);

            for (int r = 0; r < 5; r++) {
                if (tv.ndsmin[r] == null || tv.ndsmax == null) {
                    continue;
                }

                TableRow tr = getOrCreateTableRow(vg, r, Color.rgb(0, 0xa0, 0xf0), Color.rgb(0, 0xc0, 0xff));

                txt = (TextView) tr.getChildAt(0);
                txt.setText(tv.ndsmin[r].tag);

                txt = (TextView) tr.getChildAt(1);
                txt.setText(String.format("%4.1f", tv.ndsmin[r].wert));

                txt = (TextView) tr.getChildAt(2);
                txt.setText(tv.ndsmax[r].tag);

                txt = (TextView) tr.getChildAt(3);
                txt.setText(String.format("%4.1f", tv.ndsmax[r].wert));

            }
        }

        if (tvv.size() > 1) {

            tv = tvv.get(1);

            vg = (ViewGroup) overview.findViewById(R.id.extremeSonneMonat);

            for (int r = 0; r < 5; r++) {

                TableRow tr = getOrCreateTableRow(vg, r, Color.rgb(220, 220, 0), Color.rgb(255, 255, 0));

                if (tv.sonnemin != null && tv.sonnemin[r] != null) {
                    txt = (TextView) tr.getChildAt(0);
                    txt.setText(tv.sonnemin[r].tag);
                    txt = (TextView) tr.getChildAt(1);
                    txt.setText(String.format("%4.1f", tv.sonnemin[r].wert));
                }

                if (tv.sonnemax != null && tv.sonnemax[r] != null) {
                    txt = (TextView) tr.getChildAt(2);
                    txt.setText(tv.sonnemax[r].tag);

                    txt = (TextView) tr.getChildAt(3);
                    txt.setText(String.format("%4.1f", tv.sonnemax[r].wert));
                }
            }
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private TableRow getOrCreateTableRow(ViewGroup vg, int r, int col1, int col2) {
        TextView txt;
        TableRow tr;
        if (r >= vg.getChildCount()) {
            tr = new TableRow(vg.getContext());
            TableRow.LayoutParams lpar = new TableRow.LayoutParams();
            lpar.height = lpar.width = TableRow.LayoutParams.WRAP_CONTENT;
            lpar.setMargins(dp20 / 5, dp20 / 5, dp20 / 5, dp20 / 5);
            tr.setLayoutParams(lpar);

            for (int j = 0; j < 4; j++) {
                txt = new TextView(vg.getContext());
                if (j < 2) {
                    txt.setTextColor(col1);
                } else {
                    txt.setTextColor(col2);
                }
                txt.setTextSize(TypedValue.COMPLEX_UNIT_PX, textsize);
                if (j < 3) {
                    txt.setPadding(0, 0, dp20, 0);
                }
                if (j % 2 == 1) {
                    txt.setTextAlignment(TextView.TEXT_ALIGNMENT_TEXT_END);
                }
                tr.addView(txt);
            }
            vg.addView(tr);
        } else {
            tr = (TableRow) vg.getChildAt(r);
        }
        return tr;
    }

    @Override
    public void setView(View v) {
        overview = v;
    }

    private View overview = null;
    private Vector<Extreme.Tval> tvv = null;
    Tagesmittel.Tval tvmittel = null;
    private float textsize;
    private int dp20;

    private int monat = 99;
    private int tag = 99;
}
