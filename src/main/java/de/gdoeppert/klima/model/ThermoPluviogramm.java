package de.gdoeppert.klima.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class ThermoPluviogramm {

    DbBase dbBean;

    Jahre jahre;

    Station station;

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    JahresVergleich jahresVergleich;

    public boolean calcForStat() {

        Statement st;
        try {
            st = dbBean.getStatement();

            if ("".equals(jahresVergleich.vglJahr)) {
                throw new IllegalArgumentException(
                        "Jahresangabe für die Auswertung fehlt");
            }

            Vector<MonatValTN> monate_vgl;

            String stat = station.getStatKey();
            if (!stat.equals("")) {
                stat = "where stat=" + stat;
            } else {
                stat = "where (1=1)";
            }

            monate_vgl = eval(st, stat, "jahr between " + jahre.getVon() + " and "
                    + jahre.getBis(), "monat, jahr");
            if (monate_vgl.size() < 12) {
                monate = new Vector<MonatValTN>();
                return false;
            }

            monate = eval(st, stat, "(jahr = " + jahresVergleich.vglJahr
                    + " or (jahr = " + jahresVergleich.vglJahr
                    + "-1 and monat = 12))", "jahr, monat");

            calc(monate_vgl);
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.err.println("thermoPluviogramm ok");
        return true;
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

    public JahresVergleich getJahresVergleich() {
        return jahresVergleich;
    }

    public void setJahresVergleich(JahresVergleich jahresVergleich) {
        this.jahresVergleich = jahresVergleich;
    }

    private void calc(Vector<MonatValTN> monateVgl) {

        Vector<MonatValTN> jahreszeit = new Vector<MonatValTN>(5);
        Vector<MonatValTN> jahreszeitVgl = new Vector<MonatValTN>(5);

        MonatValTN jahr = new MonatValTN();
        MonatValTN jahrVgl = new MonatValTN();
        jahr.monat = 13 + 4;
        jahrVgl.monat = 13 + 4;
        for (int j = 0; j < monate.size() && j < 12; j++) { // monate beginnt
            // mit Dez VJ
            int jz = (j / 3) % 4;
            int vglIndex = (j + 12 - 1) % 12;

            if (jz >= jahreszeit.size()) {
                jahreszeit.add(new MonatValTN());
                jahreszeitVgl.add(new MonatValTN());
                jahreszeit.lastElement().monat = jz + 13;
                jahreszeitVgl.lastElement().monat = jz + 13;
            }

            jahreszeit.get(jz).tm += monate.get(j % 12).tm
                    * monate.get(j).nTage;
            jahreszeitVgl.get(jz).tm += monateVgl.get(vglIndex).tm
                    * monateVgl.get(j).nTage;

            jahreszeit.get(jz).nds += monate.get(j % 12).nds;
            jahreszeitVgl.get(jz).nds += monateVgl.get(vglIndex).nds;

            jahreszeit.get(jz).nTage += monate.get(j % 12).nTage;
            jahreszeitVgl.get(jz).nTage += monateVgl.get(vglIndex).nTage;

            int j1 = j;
            if (j1 == 0) {
                j1 = 12;
            }
            if (j1 < monate.size()) {

                jahr.tm += monate.get(j1).tm * monate.get(j1).nTage;
                jahrVgl.tm += monateVgl.get(vglIndex).tm
                        * monateVgl.get(vglIndex).nTage;

                jahr.nds += monate.get(j1).nds;
                jahrVgl.nds += monateVgl.get(vglIndex).nds;

                jahr.nTage += monate.get(j1).nTage;
                jahrVgl.nTage += monateVgl.get(vglIndex).nTage;
            }
        }
        jahreszeit.add(jahr);
        jahreszeitVgl.add(jahrVgl);

        monate.remove(0); // Dez Vorjahr

        // Jahreszeiten und Gesamtjahr hinzufügen
        for (int jz = 0; jz < jahreszeit.size(); jz++) {
            jahreszeit.get(jz).tm /= jahreszeit.get(jz).nTage;
            jahreszeitVgl.get(jz).tm /= jahreszeitVgl.get(jz).nTage;

            monate.add(jahreszeit.get(jz));
            monateVgl.add(jahreszeitVgl.get(jz));
        }

        // Differenz bilden und NDS in Prozent umrechnen
        int k = 0;
        for (int j = 0; j < monate.size(); j++) {

            while (monateVgl.get(k).monat < monate.get(j).monat)
                k++;

            monate.get(j).tm -= monateVgl.get(k).tm;
            monate.get(j).nds = 100
                    * (monate.get(j).nds - monateVgl.get(k).nds)
                    / monateVgl.get(k).nds;
        }
    }

    private Vector<MonatValTN> eval(Statement st, String stat, String where,
                                    String sort) throws SQLException, IllegalArgumentException {
        ResultSet rs = st
                .executeQuery("select monat, avg(tm) as tm, sum(rs) as nds, count(tag) as ntag"
                        + " from " + dbBean.getSchema() + "tageswerte "
                        + stat
                        + " and "
                        + where
                        + " group by " + sort + " order by " + sort);

        int monat0 = 0;

        Vector<MonatValTN> monate = new Vector<MonatValTN>(13);
        Vector<Double> tmv = null;
        Vector<Double> ndsv = null;
        Vector<Integer> tage = null;
        while (rs.next()) {
            try {
                int monat = rs.getInt(1);

                if (monat != monat0) {
                    if (tmv != null && tmv.size() > 0) {
                        addMonat(monate, monat0, tmv, ndsv, tage);
                    }
                    tmv = new Vector<Double>(100);
                    ndsv = new Vector<Double>(100);
                    tage = new Vector<Integer>(100);
                }
                if (rs.getString(2) != null) {
                    tmv.add(rs.getDouble(2));
                }
                if (rs.getString(3) != null) {
                    ndsv.add(rs.getDouble(3));
                }
                if (rs.getString(4) != null) {
                    tage.add(rs.getInt(4));
                }
                monat0 = monat;
            } catch (Exception e) {
            }
        } /* while (rs.next()) */
        rs.close();
        if (tmv == null || tmv.size() == 0) {
            throw new IllegalArgumentException(
                    "keine Daten für diesen Zeitraum vorhanden");
        }
        addMonat(monate, monat0, tmv, ndsv, tage);

        return monate;
    }

    private void addMonat(Vector<MonatValTN> monate, int monat0,
                          Vector<Double> tmv, Vector<Double> ndsv, Vector<Integer> tage) {
        MonatValTN t = new MonatValTN();
        t.monat = monat0;

        double s = 0;

        for (int j = 0; j < tmv.size(); j++) {
            s += tmv.get(j);
        }

        t.tm = s / tmv.size();
        t.tm = Math.floor(t.tm * 10 + 0.5) / 10.0;
        s = 0;
        for (int j = 0; j < ndsv.size(); j++) {
            s += ndsv.get(j);
        }

        t.nds = s / ndsv.size();
        t.nds = Math.floor(t.nds + 0.5);
        s = 0;
        for (int j = 0; j < tage.size(); j++) {
            s += tage.get(j);
        }
        t.nTage = (int) (s / tage.size());

        if (monate.size() == 0 && t.monat == 12) // Dez VJ
            t.monat -= 12;
        monate.add(t);
    }

    public Vector<MonatValTN> getMonate() {
        if (monate == null) {
            calcForStat();
        }
        return monate;
    }

    public String getJahr() {
        return jahresVergleich.vglJahr + " vgl. mit " + jahre.von + "-"
                + jahre.bis;
    }

    private Vector<MonatValTN> monate = null;
}
