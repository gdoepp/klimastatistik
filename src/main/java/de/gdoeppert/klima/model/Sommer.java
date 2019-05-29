package de.gdoeppert.klima.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class Sommer {

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
            jahr = "";
        }

        public String jahr;
        public int sommertage;
        public int tropentage;
        public int heissetage;
        public double sonnenstunden;
        public int sonnentage;


        public String getGroup() {
            return jahr;
        }

        public int getSommertage() {
            return sommertage;
        }

        public int getTropentage() {
            return tropentage;
        }

        public int getHeissetage() {
            return heissetage;
        }

        public int getSonnenstunden() {
            return (int) sonnenstunden;
        }

        public int getSonnentage() {
            return sonnentage;
        }
    }

    public String calc() throws SQLException {

        Statement st=null;
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

            select = "select jahr, count(nullif(tx<25, 1)), count(nullif(tx<30,1)), count(nullif(tn<20,1)),  " +
                    " sum(sd*(monat>4 and monat<9)), count(nullif(sd<=12,1)) from " + dbBean.getSchema() + "tageswerte "
                    + stat
                    + "  and jahr between "
                    + von
                    + " and "
                    + bis
                    + " group by jahr order by jahr";

            ResultSet rs = st.executeQuery(select);
            werte = new Vector<Tval>();
            while (rs.next()) {
                try {
                    Tval sommerwerte = new Tval();
                    sommerwerte.jahr = rs.getString(1);
                    sommerwerte.sommertage = rs.getInt(2);
                    sommerwerte.heissetage = rs.getInt(3);
                    sommerwerte.tropentage = rs.getInt(4);
                    sommerwerte.sonnenstunden = rs.getInt(5);
                    sommerwerte.sonnentage = rs.getInt(6);

                    werte.add(sommerwerte);

                } catch (SQLException e) {
                    throw e;
                }

            } /* while (rs.next()) */
            rs.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (st != null && !st.isClosed()) st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "ok";
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
    private String basis_s;
    private String cut_s;
    private Vector<Tval> werte = null;

    public String getVon() {
        return von;
    }

    public String getBis() {
        return bis;
    }


}
