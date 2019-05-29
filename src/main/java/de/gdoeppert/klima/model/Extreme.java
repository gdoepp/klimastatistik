package de.gdoeppert.klima.model;

import android.support.annotation.NonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class Extreme {

    DbBase dbBean;

    Station station;
    TagesParam tagesParam;

    public DbBase getDbBean() {
        return dbBean;
    }

    public void setDbBean(DbBase dbBean) {
        this.dbBean = dbBean;
    }


    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public void setTagesParam(TagesParam tp) {
        tagesParam = tp;
        tv = null;
    }


    static public class TagesParam {
        public int monat;
        public int tag;
    }

    static public class TvalRecord {
        public String tag;
        public double wert;
    }


    static public class Tval {
        public String wert4Name;
        public TvalRecord[] min = null;
        public TvalRecord[] max = null;
        public TvalRecord[] ndsmin = null;
        public TvalRecord[] ndsmax = null;
        public TvalRecord[] sonnemin = null;
        public TvalRecord[] sonnemax = null;
        public TvalRecord[] nds = null;
        public TvalRecord[] schnee = null;
        public TvalRecord[] wind = null;
        public Tagesmittel.TvalDistrib[] tmDist = null;
    }

    public String calc() throws SQLException {
        Statement st=null;
        try {
            st = dbBean.getStatement();


            String stat = station.getStatKey();
            if (!stat.equals("")) {
                stat = "where stat=" + stat;
            } else {
                stat = "where (1=1)";
            }
            String condition = "monat=" + tagesParam.monat;
            if (tagesParam.tag > 0) {
                condition += " and tag=" + tagesParam.tag;
            }

            tv = eval(st, stat, condition);

        } catch (Exception ex) {

        } finally {
            try {
                if (st != null && !st.isClosed() ) st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "ok";

    }

    Tval eval(Statement st, String stat, String condition)
            throws SQLException {

        Tval tv = new Tval();

        tv.min = getTvalRecords(st, stat, condition, "tn", "asc");

        tv.max = getTvalRecords(st, stat, condition, "tx", "desc");

        tv.nds = getTvalRecords(st, stat, condition, "rs", "desc");
        tv.wind = getTvalRecords(st, stat, condition, "fm", "desc");

        if (tagesParam.monat < 5 || tagesParam.monat > 9) {

            tv.schnee = getTvalRecords(st, stat, condition, "schnee", "desc");
        }
        if (tagesParam.tag < 1) {

            tv.ndsmin = getTvalRecordsSum(st, stat, condition, "rs", "asc");

            tv.ndsmax = getTvalRecordsSum(st, stat, condition, "rs", "desc");

            tv.sonnemin = getTvalRecordsSum(st, stat, condition, "sd", "asc");

            tv.sonnemax = getTvalRecordsSum(st, stat, condition, "sd", "desc");

        }
        return tv;
    }

    @NonNull
    private TvalRecord[] getTvalRecords(Statement st, String stat, String condition, String wert, String order) throws SQLException {
        ResultSet rs = st
                .executeQuery("select jahr, monat, tag, " + wert + " as wert "
                                + " from " + dbBean.getSchema() + "tageswerte "
                                + stat
                                + " and " + condition
                                + "  and " + wert + " not null order by wert " + order + " limit 5"
                );


        int j = 0;
        TvalRecord[] werte = new TvalRecord[5];
        while (rs.next()) {
            TvalRecord tr = new TvalRecord();

            tr.tag = String.format("%02d.%02d.%04d", rs.getInt(3), rs.getInt(2), rs.getInt(1));
            tr.wert = rs.getDouble(4);
            if (order.equals("asc") || tr.wert > 0) {
                werte[j++] = tr;
            }
        }
        for (; j < 5; j++) {
            werte[j] = null;
        }
        return werte;
    }

    @NonNull
    private TvalRecord[] getTvalRecordsSum(Statement st, String stat, String condition, String wert, String order) throws SQLException {
        ResultSet rs = st
                .executeQuery("select jahr, monat, sum(" + wert + ") as wert, count(*) as cnt "
                                + " from " + dbBean.getSchema() + "tageswerte "
                                + stat
                                + " and " + condition
                                + "  and " + wert + " not null group by jahr, monat having cnt >= 28 order by wert " + order + " limit 5"
                );


        int j = 0;
        TvalRecord[] werte = new TvalRecord[5];
        while (rs.next()) {
            TvalRecord tr = new TvalRecord();

            tr.tag = String.format("   %02d.%04d", rs.getInt(2), rs.getInt(1));
            tr.wert = rs.getDouble(3);
            werte[j++] = tr;
        }
        return werte;
    }




    public Tval getWerte() {
        if (tv == null) {
            try {
                calc();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return tv;
    }

    Tval tv = null;
}
