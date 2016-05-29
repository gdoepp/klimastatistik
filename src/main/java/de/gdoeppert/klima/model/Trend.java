package de.gdoeppert.klima.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class Trend {

    DbBase dbBean;

    Jahre jahre;

    Station station;

    TrendParm trendParm;

    public class TrendWert {
        public int getJahr() {
            return jahr;
        }

        public void setJahr(int jahr) {
            this.jahr = jahr;
        }

        public int jahr;
        public double wert[];
        public double tage[];

        public TrendWert() {
            wert = new double[]{0, 0, 0, 0, 0};
            tage = new double[]{0, 0, 0, 0, 0};
        }

        public String[] getWertS() {
            String[] s = new String[5];
            for (int j = 0; j < 5; j++)
                s[j] = String.format("%-6.1f", wert[j]);
            return s;
        }

        public double[] getWert() {
            return wert;
        }

        public String[] getWertAvgS() {
            String[] s = new String[5];
            for (int j = 0; j < 5; j++)
                s[j] = String.format("%-6.1f", wert[j] / tage[j]);
            return s;
        }

        public double[] getWertAvg() {
            double[] w = new double[5];
            for (int j = 0; j < 5; j++)
                w[j] = wert[j] / tage[j];
            return w;
        }
    }

    public String calc(String wertIdx_s) {

        try {
            trendParm.setWertidx(Integer.valueOf(wertIdx_s));
        } catch (Exception e) {

        }

        Statement st;

        try {
            st = dbBean.getStatement();

            String von = jahre.getVon();
            String bis = jahre.getBis();
            String stat = station.getStatKey();
            if (!stat.equals("")) {
                stat = "where stat=" + stat;
            } else {
                stat = "where (1=1)";
            }

            ResultSet rs = st.executeQuery("select jahr, monat, sum("
                    + TrendParm.wertAtt[trendParm.getWertidx()] + "), "
                    + "count(tag) from " + dbBean.getSchema() + "tageswerte " + stat
                    + " and jahr between " + von + " and " + bis + " and "
                    + TrendParm.wertAtt[trendParm.getWertidx()]
                    + " is not null "
                    + "group by jahr, monat order by jahr, monat");

            jahreswerte = new Vector<TrendWert>(100);
            int jahr0 = -1;
            TrendWert tv = null;
            while (rs.next()) {
                try {
                    int jahr = rs.getInt(1);
                    int monat = rs.getInt(2);
                    if (monat == 12)
                        jahr++;
                    int jahreszeit = (monat % 12) / 3;
                    if (tv == null || jahr != jahr0) {
                        tv = new TrendWert();
                        tv.jahr = jahr;
                        jahreswerte.add(tv);
                        jahr0 = jahr;
                    }
                    tv.wert[jahreszeit] += rs.getDouble(3);
                    tv.tage[jahreszeit] += rs.getInt(4);
                    if (rs.getInt(4) < 25) { // too many days missing!
                        tv.wert[jahreszeit] = Double.NaN;
                    }
                    tv.wert[4] += tv.wert[jahreszeit];
                    tv.tage[4] += tv.tage[jahreszeit];
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } /* while (rs.next()) */
            rs.close();

            if (trendParm.getFensterTrend() > 0) {

                Vector<TrendWert> jahre2 = new Vector<TrendWert>(100);

                for (int j = 0; j < jahreswerte.size(); j++) {
                    if (j < trendParm.getFensterTrend()
                            || j >= jahreswerte.size()
                            - trendParm.getFensterTrend()) {
                        jahre2.add(null);
                        continue;
                    }
                    TrendWert tvl = new TrendWert();

                    for (int k = -trendParm.getFensterTrend(); k <= trendParm
                            .getFensterTrend(); k++) {
                        int idx = j + k;

                        for (int jz = 0; jz < 5; jz++) {
                            TrendWert tv0 = jahreswerte.get(idx);
                            int f = (k == 0 ? 2 : 1);
                            tvl.wert[jz] += tv0.wert[jz] * f;
                            tvl.tage[jz] += tv0.tage[jz] * f;
                        }
                    }
                    tvl.jahr = jahreswerte.get(j).jahr;
                    for (int jz = 0; jz < 5; jz++) {
                        tvl.wert[jz] /= 2 * trendParm.getFensterTrend() + 2;
                        tvl.tage[jz] /= 2 * trendParm.getFensterTrend() + 2;
                    }
                    jahre2.add(tvl);
                }
                jahreswerte = jahre2;
            }
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
            return "failure";
        }

        return "ok";
    }

    public Vector<TrendWert> getJahreswerte(String wertIdx_s) {
        if (jahreswerte == null) {
            calc(wertIdx_s);
        }
        return jahreswerte;
    }

    public String getWertName(String wertIdx_s) {
        try {
            trendParm.setWertidx(Integer.valueOf(wertIdx_s));
        } catch (Exception e) {

        }

        return TrendParm.wertName[trendParm.getWertidx()];
    }

    public DbBase getDbBean() {
        return dbBean;
    }

    public void setDbBean(DbBase dbBean) {
        this.dbBean = dbBean;
    }

    public Jahre getJahre() {
        return jahre;
    }

    public void setJahre(Jahre jahre) {
        this.jahre = jahre;
    }

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public TrendParm gettrendParm() {
        return trendParm;
    }

    public void setTrendParm(TrendParm trendParm) {
        this.trendParm = trendParm;
    }

    private Vector<TrendWert> jahreswerte = null;

}
