package de.gdoeppert.klimastatistik.gui;

import java.util.Vector;

import de.gdoeppert.klima.model.Frostindex;
import de.gdoeppert.klima.model.Jahre;
import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 06.01.16.
 */
public class WinterTable extends TableHandler {

    @Override
    public int getHelpID() {
        return R.string.winter_help;
    }

    @Override
    public KlimaGridAdapter getAdapter() {


        return new KlimaGridAdapter(activity) {

            final int ncols = 8;
            private String[] colName = {"Jahr", "Kältesumme", "Eistage", "Schneetage", "Frosttage", "Bodenfrosttage", "erster Bodenfrost", "letzter Bodenfrost"};

            @Override
            public int getColumns() {
                return ncols;
            }

            @Override
            public int getCount() {
                if (tvals != null) {
                    return (tvals.size() + 1) * ncols;
                } else return 0;
            }

            @Override
            public String getItem(int i) {

                int n = i / ncols;
                if (n == 0) {
                    return colName[i % ncols];
                }
                Frostindex.Tval tv = tvals.get(n - 1);
                switch (i % ncols) {
                    case 0: {
                        return tv.getGroup();
                    }
                    case 1:
                        return tv.getKaeltesummeS();
                    case 4:
                        return String.valueOf(tv.getFrosttage());
                    case 2:
                        return String.valueOf(tv.getEistage());
                    case 3:
                        return String.valueOf(tv.getSchneetage());
                    case 6:
                        return formatDate(tv.getErsterFrost());
                    case 7:
                        return formatDate(tv.getLetzterFrost());
                    case 5:
                        return String.valueOf(tv.getBodenfrosttage());

                }
                return "";
            }

            @Override
            public long getItemId(int i) {
                return i;
            }


        };
    }

    private String formatDate(String dat) {
        if (dat != null && dat.length() >= 4) {
            String tag = dat.substring(2, 4);
            String mon = dat.substring(0, 2);
            return tag + "." + mon + ".";
        } else return "";
    }

    @Override
    public void setViewParam(Vector<?> v) {

        tvals = (Vector<Frostindex.Tval>) v;
    }

    @Override
    public Vector<?> doWork() {
        startWork();
        Frostindex frost = new Frostindex();
        frost.setDbBean(activity.getDb());
        frost.setStation(activity.getStation());
        Jahre jahre = new Jahre();
        jahre.setVon(String.valueOf(activity.getSettings().vglJahr));
        jahre.setBis(String.valueOf(activity.getSettings().vglJahr));
        jahre.setPeriode(activity.getSettings().jahre);
        frost.setJahre(jahre);
        return frost.getWerte();
    }

    private Vector<Frostindex.Tval> tvals;
}
