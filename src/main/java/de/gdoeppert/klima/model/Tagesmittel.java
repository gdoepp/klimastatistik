package de.gdoeppert.klima.model;

import java.sql.SQLException;
import java.sql.Statement;

public class Tagesmittel extends Wertermittler {

    Jahre jahre;

    public Jahre getJahre() {
        return jahre;
    }

    public void setJahre(Jahre jahre) {
        this.jahre = jahre;
    }

    TagesParam tagesParam;

    public void setTagesParam(TagesParam tp) {
        tagesParam = tp;
        tv = null;
    }

    static public class TagesParam {
        public int monat;
        public int tag;
    }

    public String calc() throws SQLException {
        Statement st = null;
        try {

            st = getDbBean().getStatement();

            String von = jahre.getVon();
            String bis = jahre.getBis();

            String stat = getStation().getStatKey();
            if (!stat.equals("")) {
                stat = "where stat=" + stat;
            } else {
                stat = "where (1=1)";
            }
            String condition = "monat=" + tagesParam.monat;
            if (tagesParam.tag > 0) {
                condition += " and tag=" + tagesParam.tag;
            }

            tv = eval(st, stat, von, bis, condition);

            if (tagesParam.tag < 1) {
                tv.tmDist = getTvalDistrib(st, stat, von, bis, condition);
                addWetterlage(st, tagesParam.monat, von, bis, condition);
            }

        } catch (Exception ex) {

        } finally {
            if (st != null) st.close();

        }
        return "ok";

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

}
