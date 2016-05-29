package de.gdoeppert.klima.model;

import java.sql.SQLException;
import java.sql.Statement;

public class Monatswerte extends Wertermittler {

    int jahr;

    public int getJahr() {
        return jahr;
    }

    public void setJahr(int jahr) {
        this.jahr = jahr;
        tv = null;
    }

    int monat;

    public int getMonat() {
        return monat;
    }

    public void setMonat(int m) {
        monat = m;
        tv = null;
    }


    public String calc() throws SQLException {
        Statement st = null;
        try {

            st = dbBean.getStatement();

            String jahr_s = String.valueOf(jahr);

            String stat = station.getStatKey();
            if (!stat.equals("")) {
                stat = "where stat=" + stat;
            } else {
                stat = "where (1=1)";
            }
            String condition = "monat=" + monat;

            tv = eval(st, stat, jahr_s, jahr_s, condition);

            tv.tmDist = getTvalDistrib(st, stat, jahr_s, jahr_s, condition);
            addWetterlage(st, monat, jahr_s, jahr_s, condition);

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
