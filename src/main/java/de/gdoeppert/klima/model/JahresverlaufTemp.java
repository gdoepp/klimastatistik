package de.gdoeppert.klima.model;

import android.util.Log;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class JahresverlaufTemp {

    DbBase dbBean;

    Jahre jahre;

    JahresVergleich jahresvergleich;

    public JahresVergleich getJahresvergleich() {
        return jahresvergleich;
    }

    public void setJahresvergleich(JahresVergleich jahresvergleich) {
        this.jahresvergleich = jahresvergleich;
    }

    JahresVerlauf jahresverlauf;

    public JahresVerlauf getJahresverlauf() {
        return jahresverlauf;
    }

    public void setJahresverlauf(JahresVerlauf jahresverlauf) {
        this.jahresverlauf = jahresverlauf;
    }

    Station station;
    String condition = "";

    public void setCondition(String c) {
        condition = c;
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

    public String calc(boolean doVgl, String vgl) throws SQLException {

        Integer fenster = 1;
        if (vgl != null && vgl.equals("true")) {
            fenster = jahresverlauf.getFensterVgl();
            vgl = jahresvergleich.getVglJahr();
        } else {
            fenster = jahresverlauf.getFensterVerl();
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

            if (doVgl) {
                tage_vgl = eval(st, stat, vgl, vgl, fenster);
            } else {
                tage = eval(st, stat, von, bis, fenster);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "failure";
        }
        return "ok";
    }


    Vector<TagesWerteT> eval(Statement st, String stat, String von, String bis,
                             int fenster) throws SQLException {

        Log.i("JahresverlaufTemp", "stat " + stat);


        ResultSet rs = st
                .executeQuery("select monat, tag, avg(tm), avg(tn), avg(tx), min(tn), max(tx), avg(tm*tm), count(*) from " + dbBean.getSchema() + "tageswerte "
                        + stat
                        + " " + condition
                        + " and jahr between "
                        + von
                        + " and "
                        + bis
                        + " group by monat,tag");

        int monat0 = 0;
        int tag0 = 0;

        Vector<TagesWerteT> tage = new Vector<TagesWerteT>(366);
        Vector<Double> tmv = null;
        Vector<Double> tnv = null;
        Vector<Double> txv = null;

        while (rs.next()) {
            try {
                int monat = rs.getInt(1);
                int tag = rs.getInt(2);
                if (monat == 2 && tag == 29)
                    continue;

                TagesWerteT tw = new TagesWerteT();
                tw.monat = monat;
                tw.tag = tag;

                if (rs.getString(3) != null) tw.tm = rs.getDouble(3);
                if (rs.getString(4) != null) tw.tn = rs.getDouble(4);
                if (rs.getString(5) != null) tw.tx = rs.getDouble(5);
                if (rs.getString(6) != null) tw.tnk = rs.getDouble(6);
                if (rs.getString(7) != null) tw.txk = rs.getDouble(7);

                double s = 0;
                int n = 1;
                if (rs.getString(9) != null) n = rs.getInt(9);
                if (n > 1) {
                    if (rs.getString(8) != null) s = rs.getDouble(8);
                    s = s - tw.tm * tw.tm;
                    s *= (1.0 * n) / (n - 1);
                    s = Math.sqrt(s);
                }

                tw.tm_o = tw.tm + s;
                tw.tm_u = tw.tm - s;

                tage.add(tw);

            } catch (Exception e) {
            }
        } /* while (rs.next()) */
        rs.close();

        if (fenster > 0) {
            Vector<TagesWerteT> tage2 = new Vector<TagesWerteT>(366);
            for (int j = 0; j < tage.size(); j++) {
                TagesWerteT tv = new TagesWerteT();
                for (int k = -fenster; k <= fenster; k++) {
                    TagesWerteT tv0 = tage.get((j + k + tage.size())
                            % tage.size());
                    double f = (k == 0 ? 2 : 1);

                    tv.tm += tv0.tm * f;
                    tv.tm_o += tv0.tm_o * f;
                    tv.tm_u += tv0.tm_u * f;
                    tv.tn += tv0.tn * f;
                    tv.tx += tv0.tx * f;
                    tv.tnk += tv0.tnk * f;
                    tv.txk += tv0.txk * f;
                }
                tv.tm /= 2 * fenster + 2;
                tv.tm_o /= 2 * fenster + 2;
                tv.tm_u /= 2 * fenster + 2;
                tv.tn /= 2 * fenster + 2;
                tv.tx /= 2 * fenster + 2;
                tv.tnk /= 2 * fenster + 2;
                tv.txk /= 2 * fenster + 2;
                tv.monat = tage.get(j).monat;
                tv.tag = tage.get(j).tag;
                tage2.add(tv);
            }
            tage = tage2;
        }
        return tage;
    }

    void addTag(Vector<TagesWerteT> tage, int monat0, int tag0,
                Vector<Double> tmv, Vector<Double> tnv, Vector<Double> txv) {
        TagesWerteT t = new TagesWerteT();
        t.monat = monat0;
        t.tag = tag0;

        double s = 0;
        double mn = 50;
        double mx = -50;
        for (int j = 0; j < tmv.size(); j++)
            s += tmv.get(j);
        t.tm = s / tmv.size();
        t.tm = Math.floor(t.tm * 10) / 10.0;

        s = 0;
        for (int j = 0; j < tnv.size(); j++) {
            s += tnv.get(j);
            mn = Math.min(mn, tnv.get(j));
        }
        t.tn = s / tnv.size();
        t.tn = Math.floor(t.tn * 10) / 10.0;
        t.tnk = mn;

        s = 0;
        for (int j = 0; j < txv.size(); j++) {
            s += txv.get(j);
            mx = Math.max(mx, txv.get(j));
        }
        t.tx = s / txv.size();
        t.tx = Math.floor(t.tx * 10) / 10.0;
        t.txk = mx;

        int n = (int) (tmv.size() * 5.0 / 6.0);
        t.tm_o = tmv.get(n);
        n = (int) (tmv.size() * 1.0 / 6.0);
        t.tm_u = tmv.get(n);
        tage.add(t);
    }

    public Vector<TagesWerteT> getTage(String vgl) {
        if (tage == null) {
            try {
                calc(false, vgl);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return tage;
    }

    public Vector<TagesWerteT> getTageVgl(String vgl) {
        if (tage_vgl == null) {
            try {
                calc(true, vgl);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return tage_vgl;
    }

    private Vector<TagesWerteT> tage = null;
    private Vector<TagesWerteT> tage_vgl = null;
}
