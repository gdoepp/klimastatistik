package de.gdoeppert.klimastatistik.gui;

import java.util.Vector;

import de.gdoeppert.klima.model.Jahre;
import de.gdoeppert.klima.model.Sommer;
import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 06.01.16.
 */
public class SommerTable extends TableHandler {

    @Override
    public int getHelpID() {
        return R.string.sommer_help;
    }


    @Override
    public KlimaGridAdapter getAdapter() {

        return new KlimaGridAdapter(activity) {

            final int ncols = 6;

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

            /*
             public String jahr;
        public int sommertage;
        public int tropentage;
        public int heissetage;
        public double sonnenstunden;
        public int sonnentage;

        public String vegbeg;
        public String vegend;
        public int vegdauer;
             */
            private String[] colName = {"Jahr", "Sommertage", "heiße Tage", "Tropennächte", "Sonnenstunden", "Sonnentage"};

            @Override
            public String getItem(int i) {

                int n = i / ncols;
                if (n == 0) {
                    return colName[i % ncols];
                }

                Sommer.Tval tv = tvals.get(n - 1);
                switch (i % ncols) {
                    case 0: {
                        return tv.getGroup();
                    }
                    case 1:
                        return String.valueOf(tv.getSommertage());
                    case 2:
                        return String.valueOf(tv.getHeissetage());
                    case 3:
                        return String.valueOf(tv.getTropentage());
                    case 4:
                        return String.valueOf(tv.getSonnenstunden());
                    case 5:
                        return String.valueOf(tv.getSonnentage());
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
    public void setViewParam(Vector<?> v) {

        tvals = (Vector<Sommer.Tval>) v;
    }

    @Override
    public Vector<?> doWork() {
        Sommer sommer = new Sommer();
        sommer.setDbBean(activity.getDb());
        sommer.setStation(activity.getStation());
        stat = activity.getStation().getSelStat().getStat();
        Jahre jahre = new Jahre();
        jahre.setVon(String.valueOf(activity.getSettings().vglJahr));
        jahre.setBis(String.valueOf(activity.getSettings().vglJahr));
        jahre.setPeriode(activity.getSettings().jahre);

        sommer.setJahre(jahre);

        return sommer.getWerte();
    }

    private Vector<Sommer.Tval> tvals;
}
