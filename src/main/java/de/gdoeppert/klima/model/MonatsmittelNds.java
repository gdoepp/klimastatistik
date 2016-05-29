package de.gdoeppert.klima.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class MonatsmittelNds {

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
        public String monat;
        public int monat_i;
        public double rs;
        public double fm;
        public double nm;
        public double sd;
        public double hm;
        public double pm;
        public double n_f;
        public double n_t;

        public String getN_f() {
            return String.format("%-6.0f", n_f * 100);
        }

        public String getN_t() {
            return String.format("%-6.0f", n_t * 100);
        }

        public double getN_fN() {
            return n_f;
        }

        public double getN_tN() {
            return n_t;
        }

        public double getFm() {
            return fm;
        }

        public double getHm() {
            return hm;
        }

        public int getMonatN() {
            return monat_i;
        }

        public String getMonat() {
            return monat;
        }

        public double getNm() {
            return nm;
        }

        public double getRs() {
            return rs;
        }

        public double getSd() {
            return sd;
        }

        public double getPm() {
            return pm;
        }

        public String getFmS() {
            return String.format("%5.2f", fm);
        }

        public String getHmS() {
            return String.format("%6.2f", hm);
        }

        public String getNmS() {
            return String.format("%5.2f", nm);
        }

        public String getRsS() {
            return String.format("%5.2f", rs);
        }

        public String getSdS() {
            return String.format("%5.2f", sd);
        }

        public String getPmS() {
            return String.format("%7.1f", pm);
        }

    }

    public String calc() throws SQLException {
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

            tage = eval(st, stat, von, bis);
            for (Tval t : tage) {
                addWetterlage(t, von, bis);
            }
        } catch (Exception ex) {

        }
        return "ok";
    }

    Vector<Tval> eval(Statement st, String stat, String von, String bis)
            throws SQLException {
        ResultSet rs = st
                .executeQuery("select monat, avg(rs), avg(nm), avg(sd), avg(um), avg(fm), avg(pm)"
                        + " from " + dbBean.getSchema() + "tageswerte "
                        + stat
                        + " and jahr between "
                        + von
                        + " and "
                        + bis
                        + " group by monat order by monat");

        Vector<Tval> tage = new Vector<Tval>(12);

        while (rs.next()) {
            try {
                int monat = rs.getInt(1);

                Tval tv = new Tval();
                tv.monat_i = monat;
                tv.monat = monatsnamen[monat];

                if (rs.getString(2) != null) {
                    tv.rs = rs.getDouble(2);
                }
                if (rs.getString(3) != null) {
                    tv.nm = rs.getDouble(3);
                }
                if (rs.getString(4) != null) {
                    tv.sd = rs.getDouble(4);
                }
                if (rs.getString(5) != null) {
                    tv.hm = rs.getDouble(5);
                }
                if (rs.getString(6) != null) {
                    tv.fm = rs.getDouble(6);
                }
                if (rs.getString(7) != null) {
                    tv.pm = rs.getDouble(7);
                }
                tage.add(tv);
            } catch (Exception e) {
            }
        } /* while (rs.next()) */
        rs.close();

        return tage;
    }

    void addWetterlage(Tval t, String von, String bis) {
        Statement st;
        try {
            st = dbBean.getStatement();
            ResultSet rsWetterlage = st
                    .executeQuery("select substr(kennung,5,1), count(*) "
                            + "from " + dbBean.getSchema() + "wetterlage_k where monat=" + t.monat_i
                            + " and jahr between " + von + " and " + bis
                            + " group by substr(kennung,5,1) ");
            int anzges = 0;
            while (rsWetterlage.next()) {
                String kennung = rsWetterlage.getString(1);
                int anz = rsWetterlage.getInt(2);
                if (kennung.equals("T")) {
                    t.n_t = anz;
                    anzges += t.n_t;
                } else if (kennung.equals("F")) {
                    t.n_f = anz;
                    anzges += t.n_f;
                } else {
                    System.err.println("Wetterlage "
                            + rsWetterlage.getString(1));
                }
            }
            t.n_t /= anzges;
            t.n_f /= anzges;
            rsWetterlage.close();
            st.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public int compareRs(Tval t1, Tval t2) {
        if (t1.rs == t2.rs)
            return 0;
        if (t1.rs < t2.rs)
            return -1;
        return 1;
    }

    public Vector<Tval> getTage() {
        if (tage == null) {
            try {
                calc();
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return tage;
    }

    private Vector<Tval> tage = null;
    private final String[] monatsnamen = {"Ges", "Jan", "Feb", "Mar", "Apr",
            "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"};
}
