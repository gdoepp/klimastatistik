package de.gdoeppert.klima.model;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class MonatsmittelTemp {

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
        public int monat_i;
        public String monat;
        public double tm;
        public double tm_u;
        public double tm_o;
        public double tn;
        public double tx;
        public double tnk;
        public double txk;
        public double n_xx;
        public double n_nw;
        public double n_no;
        public double n_sw;
        public double n_so;

        public double getN_xxN() {
            return n_xx;
        }

        public double getN_swN() {
            return n_sw;
        }

        public double getN_soN() {
            return n_so;
        }

        public double getN_nwN() {
            return n_nw;
        }

        public double getN_noN() {
            return n_no;
        }

        public String getN_xx() {
            return String.format("%-6.0f", n_xx * 100);
        }

        public String getN_nw() {
            return String.format("%-6.0f", n_nw * 100);
        }

        public String getN_no() {
            return String.format("%-6.0f", n_no * 100);
        }

        public String getN_sw() {
            return String.format("%-6.0f", n_sw * 100);
        }

        public String getN_so() {
            return String.format("%-6.0f", n_so * 100);
        }

        public String getMonat() {
            return monat;
        }

        public int getMonatN() {
            return monat_i;
        }

        public double getTm() {
            return tm;
        }

        public double getTm_o() {
            return tm_o;
        }

        public double getTm_u() {
            return tm_u;
        }

        public double getTn() {
            return tn;
        }

        public double getTnk() {
            return tnk;
        }

        public double getTx() {
            return tx;
        }

        public double getTxk() {
            return txk;
        }

        public String getTmS() {
            return String.format("%-6.2f", tm);
        }

        public String getTm_oS() {
            return String.format("%-6.2f", tm_o);
        }

        public String getTm_uS() {
            return String.format("%6.2f", tm_u);
        }

        public String getTnS() {
            return String.format("%6.2f", tn);
        }

        public String getTnkS() {
            return String.format("%6.2f", tnk);
        }

        public String getTxS() {
            return String.format("%6.2f", tx);
        }

        public String getTxkS() {
            return String.format("%6.2f", txk);
        }

    }

    public String calc() throws SQLException {
        Statement st=null;
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

        } finally {
            try {
                if (st != null && !st.isClosed()) st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return "ok";

    }

    Vector<Tval> eval(Statement st, String stat, String von, String bis)
            throws SQLException {
        ResultSet rs = st
                .executeQuery("select monat, avg(tm) as tm, avg(tn), avg(tx), min(tn), max(tx)"
                        + " from " + dbBean.getSchema() + "tageswerte "
                        + stat
                        + " and jahr between "
                        + von
                        + " and "
                        + bis
                        + " group by monat, jahr order by monat,tm");

        int monat0 = 0;

        Vector<Tval> tage = new Vector<Tval>(12);
        Vector<Double> tmv = null;
        Vector<Double> tnv = null;
        Vector<Double> txv = null;
        Vector<Double> tnnv = null;
        Vector<Double> txxv = null;

        while (rs.next()) {
            try {
                int monat = rs.getInt(1);

                if (monat != monat0) {
                    if (tmv != null && tmv.size() > 0) {
                        addTag(tage, monat0, tmv, tnv, txv, tnnv, txxv);
                    }
                    tmv = new Vector<Double>(100);
                    tnv = new Vector<Double>(100);
                    txv = new Vector<Double>(100);
                    tnnv = new Vector<Double>(100);
                    txxv = new Vector<Double>(100);
                }
                if (rs.getString(2) != null) {
                    tmv.add(rs.getDouble(2));
                }
                if (rs.getString(3) != null && rs.getString(4) != null) {
                    tnv.add(rs.getDouble(3));
                    txv.add(rs.getDouble(4));
                }
                if (rs.getString(5) != null && rs.getString(6) != null) {
                    tnnv.add(rs.getDouble(5));
                    txxv.add(rs.getDouble(6));
                }
                monat0 = monat;
            } catch (Exception e) {
            }
        } /* while (rs.next()) */
        rs.close();
        addTag(tage, monat0, tmv, tnv, txv, tnnv, txxv);

        return tage;
    }

    void addTag(Vector<Tval> tage, int monat0, Vector<Double> tmv,
                Vector<Double> tnv, Vector<Double> txv, Vector<Double> tnnv,
                Vector<Double> txxv) {
        Tval t = new Tval();
        t.monat_i = monat0;
        t.monat = monatsnamen[monat0];

        double s = 0;
        double mn = 50;
        double mx = -50;
        for (int j = 0; j < tmv.size(); j++)
            s += tmv.get(j);
        t.tm = s / tmv.size();
        t.tm = Math.floor(t.tm * 10 + 0.5) / 10.0;

        s = 0;
        for (int j = 0; j < tnv.size(); j++) {
            s += tnv.get(j);
            mn = Math.min(mn, tnnv.get(j));
        }
        t.tn = s / tnv.size();
        t.tn = Math.floor(t.tn * 10 + 0.5) / 10.0;
        t.tnk = mn;

        s = 0;
        for (int j = 0; j < txv.size(); j++) {
            s += txv.get(j);
            mx = Math.max(mx, txxv.get(j));
        }
        t.tx = s / txv.size();
        t.tx = Math.floor(t.tx * 10 + 0.5) / 10.0;
        t.txk = mx;

        int n = (int) (tmv.size() * 5.0 / 6.0);
        t.tm_o = tmv.get(n);
        n = (int) (tmv.size() * 1.0 / 6.0);
        t.tm_u = tmv.get(n);

        tage.add(t);
    }

    void addWetterlage(Tval t, String von, String bis) {
        Statement st;
        try {
            st = dbBean.getStatement();
            ResultSet rsWetterlage = st
                    .executeQuery("select substr(kennung,1,2), count(*) "
                            + "from " + dbBean.getSchema() + "wetterlage_k where monat=" + t.monat_i
                            + " and jahr between " + von + " and " + bis
                            + " group by substr(kennung,1,2) ");
            int anzges = 0;
            while (rsWetterlage.next()) {
                String kennung = rsWetterlage.getString(1);
                int anz = rsWetterlage.getInt(2);
                if (kennung.equals("NW")) {
                    t.n_nw = anz;
                    anzges += t.n_nw;
                } else if (kennung.equals("NO")) {
                    t.n_no = anz;
                    anzges += t.n_no;
                } else if (kennung.equals("SW")) {
                    t.n_sw = anz;
                    anzges += t.n_sw;
                } else if (kennung.equals("SO")) {
                    t.n_so = anz;
                    anzges += t.n_so;
                } else if (kennung.equals("XX")) {
                    t.n_xx = anz;
                    anzges += t.n_xx;
                } else {
                    System.err.println("Wetterlage "
                            + rsWetterlage.getString(1));
                }
            }
            t.n_no /= anzges;
            t.n_nw /= anzges;
            t.n_sw /= anzges;
            t.n_so /= anzges;
            t.n_xx /= anzges;
            rsWetterlage.close();
            st.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void setTage(Vector<Tval> tage) {
        this.tage = tage;
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
