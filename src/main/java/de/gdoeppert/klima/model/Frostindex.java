package de.gdoeppert.klima.model;

import android.util.Log;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class Frostindex implements Serializable {

    DbBase dbBean;

    Jahre jahre;

    Station station;

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

    public class Tval {
        public Tval() {
            schneetage = 0;
            winter = "";
            kaeltesumme = 0;
        }

        public String winter;
        public int eistage;
        public int frosttage;
        public int schneetage;
        public String ersterFrost;
        public double kaeltesumme;
        public int bodenfrosttage;

        public String getErsterFrost() {
            return ersterFrost;
        }

        public String letzterFrost;

        public String getLetzterFrost() {
            return letzterFrost;
        }

        public String getGroup() {
            return winter;
        }


        public int getEistage() {
            return eistage;
        }

        public int getFrosttage() {
            return frosttage;
        }

        public int getSchneetage() {
            return schneetage;
        }


        public String getKaeltesummeS() {
            return String.format("%4.0f", kaeltesumme);
        }

        public double getKaeltesumme() {
            return kaeltesumme;
        }

        public int getBodenfrosttage() {
            return bodenfrosttage;
        }
    }

    public String calc() throws SQLException {

        Statement st;
        try {
            st = dbBean.getStatement();

            von = jahre.getVon();
            bis = jahre.getBis();

            String select = "";

            String stat = station.getStatKey();
            if (!stat.equals("")) {
                stat = "where stat=" + stat;
            } else {
                stat = "where (1=1)";
            }

            select = "select jahr+monat/7 as winter, count(nullif(tn>=0,1)), count(nullif(tx>=0,1)), -sum(tm*(tm<0)), count(nullif(schnee=0,1)), " +
                    " min( (jahr*10000+monat*100+tag) * (3-(tnb < 0)*2)), max( (jahr*10000+monat*100+tag) * (tnb < 0)), count(nullif(tnb>=0,1))" +
                    " from " + dbBean.getSchema() + "tageswerte "
                    + stat
                    + "  and jahr between "
                    + von
                    + " and "
                    + bis
                    + " group by winter order by winter";


            ResultSet rs = st.executeQuery(select);
            werte = new Vector<Tval>();

            while (rs.next()) {
                try {
                    Tval frost = new Tval();
                    frost.winter = (rs.getInt(1) - 1) + "-" + rs.getString(1);
                    frost.frosttage = rs.getInt(2);
                    frost.eistage = rs.getInt(3);
                    frost.kaeltesumme = rs.getFloat(4);
                    frost.schneetage = rs.getInt(5);
                    if (rs.getString(6).length() > 4 && rs.getInt(6) < 21000000) {
                        frost.ersterFrost = rs.getString(6).substring(4);
                        Log.d("frost", "efrost " + rs.getString(6));
                    }
                    if (rs.getString(7).length() > 4) {
                        frost.letzterFrost = rs.getString(7).substring(4);
                    }
                    frost.bodenfrosttage = rs.getInt(8);

                    werte.add(frost);

                } catch (SQLException ex) {
                    ex.printStackTrace();
                }
            }
            return "ok";
        } catch (SQLException ex) {
            ex.printStackTrace();
            return "nok";
        }
    }

    public void setWerte(Vector<Tval> werte) {
        this.werte = werte;
    }

    public Vector<Tval> getWerte() {
        if (werte == null) {
            try {
                calc();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return werte;
    }

    private String von;
    private String bis;

    private Vector<Tval> werte = null;

    public String getGroup() {
        return "Winter";
    }

    public String getVon() {
        return von;
    }

    public String getBis() {
        return bis;
    }

}
