package de.gdoeppert.klimastatistik.gui;

import java.util.Vector;

import de.gdoeppert.klima.model.Gradtage;
import de.gdoeppert.klima.model.GradtageParm;
import de.gdoeppert.klima.model.Jahre;
import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 06.01.16.
 */
public class GradtageTable extends TableHandler {

    @Override
    public int getHelpID() {
        return R.string.gradtage_help;
    }


    @Override
    public KlimaGridAdapter getAdapter() {

        return new KlimaGridAdapter(activity) {

            @Override
            public int getColumns() {
                return ncols;
            }

            @Override
            public int getCount() {
                if (tvals != null) {
                    return (tvals.size() + 1) * getColumns();
                } else return 0;
            }


            @Override
            public String getItem(int i) {

                int n = i / ncols;

                if (n == 0) {
                    switch (i % ncols) {
                        case 0: {
                            switch (mode) {
                                case "0":
                                    return "Monat";
                                case "1":
                                    return "Jahr";
                                case "2":
                                    return "Winter";
                            }
                        }
                        case 1:
                            return "Heiztage";
                        case 2:
                            return "Gradtage";
                        case 3:
                            if (mode.equals("0"))
                                return "Anteil";
                            else
                                return "     ";
                    }

                }

                Gradtage.Tval tv = tvals.get(n - 1);
                switch (i % ncols) {
                    case 0: {
                        return tv.getGroup();
                    }
                    case 1:
                        if (mode.equals("0")) {
                            return tv.getTageS();
                        } else {
                            return String.format("%3.0f", tv.getTage());
                        }
                    case 2:
                        return tv.getTmS();
                    case 3:
                        if (mode.equals("0"))
                            return tv.getAnteil();
                        else
                            return "     ";

                }
                return "";
            }

            @Override
            public long getItemId(int i) {
                return i;
            }

        };
    }

    @Override
    public boolean needsUpdate() {

        if (super.needsUpdate() || parm == null) return true;

        return dirty ||
                parm.getCut() != activity.getSettings().gradTemp ||
                parm.getBasis() != activity.getSettings().gradSchwelle;
    }


    @Override
    public void setViewParam(Vector<?> v) {
        tvals = (Vector<Gradtage.Tval>) v;
    }

    @Override
    public Vector<?> doWork() {

        startWork();
        Gradtage gradtage = new Gradtage();
        gradtage.setDbBean(activity.getDb());
        gradtage.setStation(activity.getStation());
        Jahre jahre = new Jahre();
        jahre.setVon(String.valueOf(activity.getSettings().vglJahr));
        jahre.setBis(String.valueOf(activity.getSettings().vglJahr));
        jahre.setPeriode(activity.getSettings().jahre);

        parm = new GradtageParm();
        parm.setBasis(activity.getSettings().gradSchwelle);
        parm.setCut(activity.getSettings().gradTemp);
        gradtage.setJahre(jahre);
        gradtage.setGradtageParm(parm);
        if (!mode.equals("0")) {
            mode = (activity.getSettings().heizmodusJahr ? "1" : "2");
        }
        Vector<Gradtage.Tval> result = gradtage.getWerte(mode);

        return result;

    }

    public void setMode(String md) {
        if (md.contains("Jahr")) {
            dirty = !mode.equals("2");
            mode = "2";
            ncols = 3;
        } else if (md.contains("Monat")) {
            dirty = !mode.equals("0");
            mode = "0";
            ncols = 4;
        }

    }

    private Vector<Gradtage.Tval> tvals;
    private String mode = "2";
    private int ncols;
    private GradtageParm parm = null;

}
