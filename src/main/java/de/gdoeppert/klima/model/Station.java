package de.gdoeppert.klima.model;

import android.support.annotation.NonNull;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.TreeMap;

public class Station implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    DbBase dbBean;

    public DbBase getDbBean() {
        return dbBean;
    }

    public void setDbBean(DbBase dbBean) {
        this.dbBean = dbBean;
    }

    public Station() {
    }

    public boolean init() {

        stationen_alle = new TreeMap<String, Stat>();
        stationen = new TreeMap<String, Stat>();
        stationen_i = new TreeMap<Integer, Stat>();

        Statement st = null;
        int jahr1 = 0;
        int jahr2 = 9999;

        try {
            st = dbBean.getStatement();
            ResultSet rs = st
                    .executeQuery("select s.stat, name, min(jahr), max(jahr), hoehe "
                            + "from " + dbBean.getSchema() + "station s inner join " + dbBean.getSchema() + "tageswerte t on (s.stat=t.stat) group by name, s.stat "
                            + "order by name");


            while (rs.next()) {
                Stat stat = new Stat();
                stat.stat = rs.getInt(1);
                stat.name = dbBean.getCol(rs, 2);
                stat.jahr1 = rs.getInt(3);
                stat.jahr2 = rs.getInt(4);
                stat.hoehe = rs.getDouble(5);
                if (stat.jahr1 > jahr1)
                    jahr1 = stat.jahr1;
                if (stat.jahr2 < jahr2)
                    jahr2 = stat.jahr2;

                stationen.put(dbBean.getCol(rs, 1) + "#" + dbBean.getCol(rs, 2),
                        stat);
                stationen_i.put(stat.stat, stat);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
/*
        Stat gesamt = new Stat();
        gesamt.stat = 0;
        gesamt.name = "Gesamt";
        gesamt.jahr1 = jahr1;
        gesamt.jahr2 = jahr2;
       // stationen.put("", gesamt);
        //stationen_i.put(gesamt.stat, gesamt);

        //selStat = gesamt;
        */
        if (!stationen_i.isEmpty()) {
            int first = stationen_i.keySet().iterator().next();
            selStat = stationen_i.get(first);
        }

        return true;
    }

    public String getStation() throws SQLException {

        if (selStat != null || init()) return selStat.getName();
        return null;

    }

    public Stat[] getStationen() {
        if (stationen != null || init()) {
            return stationen.values().toArray(new Stat[0]);
        } else return new Stat[0];
    }

    public Stat[] getStationenAlle() {
        return getStationenIntern("select stat, name, aktiv_von, aktiv_bis, hoehe "
                + "from " + dbBean.getSchema() + "station order by name");
    }

    public Stat[] getStationenAlle(double lat, double lng) {

        String query = "select stat, name, aktiv_von, aktiv_bis, hoehe "
                + "from " + dbBean.getSchema() +
                "station where abs(latitude-" + lat + ") < 0.25 and abs(longitude-" + lng + ") < 0.25 order by name";

        return getStationenIntern(query);
    }

    @NonNull
    private Stat[] getStationenIntern(String query) {
        stationen_alle.clear();

        try {
            Statement st = dbBean.getStatement();
            ResultSet rs = st
                    .executeQuery(query);

            while (rs.next()) {
                Stat stat = new Stat();
                stat.stat = rs.getInt(1);
                stat.name = dbBean.getCol(rs, 2);
                stat.jahr1 = rs.getInt(3);
                stat.jahr2 = rs.getInt(4);
                stat.hoehe = rs.getDouble(5);
                stationen_alle.put(stat.name, stat);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return new Stat[0];
        }

        return stationen_alle.values().toArray(new Stat[0]);
    }

    public void setSelStat(String s) {
        int pos = s.indexOf('#');
        if (pos >= 0) {
            int nr = Integer.valueOf(s.substring(0, pos));
            setSelStat(nr);
        } else {
            selStat = stationen.get(s);
        }
        if (selStat == null) {
            selStat = stationen_alle.get(s);
        }
    }

    public void setSelStat(int nr) {
        if (selStat != null || init()) {
            selStat = stationen_i.get(nr);
        }
    }

    public Stat getSelStat() {
        if (selStat != null || init()) {
            return selStat;
        } else return null;
    }

    public String getStatKey() {
        if (selStat == null || selStat.getStat() == 0) return "";
        else return String.format("%d", selStat.getStat());
    }

    private Stat selStat = null;
    private Map<String, Stat> stationen_alle = null;
    private Map<String, Stat> stationen = null;
    private Map<Integer, Stat> stationen_i = null;
}
