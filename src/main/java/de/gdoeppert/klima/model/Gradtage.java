package de.gdoeppert.klima.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class Gradtage implements Serializable {

    DbBase dbBean;

    Jahre jahre;

    GradtageParm gradtageParm;
    int wertTyp;

    public GradtageParm getGradtageParm() {
        return gradtageParm;
    }

    public void setGradtageParm(GradtageParm gradtageParm) {
        this.gradtageParm = gradtageParm;
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

    Station station;

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public class Tval {
        public String group;
        public double tm;
        public double tage;
        public int groupN;

        public String getGroup() {
            return group;
        }

        public int getGroupN() {
            return groupN;
        }

        public double getTm() {
            return tm;
        }

        public double getTage() {
            return tage;
        }

        public String getTmS() {
            return String.format("%6.0f", tm);
        }

        public String getTageS() {
            return String.format("%6.1f", tage);
        }

        public String getAnteil() {
            return String.format("%6.2f%%", tm * 100 / summe);
        }
    }

    public boolean calc(String w_s) throws SQLException {

        Statement st=null;
        try {
            st = dbBean.getStatement();

            von = jahre.getVon();
            bis = jahre.getBis();

            if (w_s != null) {
                wertTyp = Integer.parseInt(w_s);
                gradtageParm.setWerttyp(wertTyp);
            } else {
                wertTyp = gradtageParm.getWerttyp();
            }

            String group = "";
            String select = "";

            switch (wertTyp) {
                case 0:
                    group = "monat";
                    summe = 0;
                    break;
                case 1:
                    group = "jahr";
                    break;
                case 2:
                    group = "(jahr+ cast(monat / 7 as int))";
                    break;
            }

            String stat = station.getStatKey();
            if (!stat.equals("")) {
                stat = "where stat=" + stat;
            } else {
                stat = "where (1=1)";
            }

            select = "select " + group + ", sum(" + gradtageParm.getCut()
                    + "-tm), count(tag) from " + dbBean.getSchema() + "tageswerte " + stat
                    + " and tm < " + gradtageParm.getBasis()
                    + " and jahr between " + von + " and " + bis + " group by "
                    + group + " order by " + group;

            ResultSet rs = st
                    .executeQuery("select count(distinct ((jahr-1700)+stat*1000)),"
                            + " count(distinct (stat)) from " + dbBean.getSchema() + "tageswerte "
                            + stat + " and jahr between " + von + " and " + bis);

            int jahre = 0;
            int stationen = 0;
            if (rs.next()) {
                jahre = rs.getInt(1);
                stationen = rs.getInt(2);
            }
            rs.close();

            rs = st.executeQuery(select);

            werte = new Vector<Tval>(12);
            String[] monatsnamen = {"Ges", "Jan", "Feb", "Mar", "Apr", "Mai",
                    "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"};
            Tval sum = new Tval();

            while (rs.next()) {
                try {
                    Tval mon = new Tval();
                    mon.groupN = rs.getInt(1);
                    if (wertTyp == 0) {
                        mon.group = monatsnamen[rs.getInt(1)];
                        mon.tm = rs.getDouble(2) / jahre;
                        mon.tage = rs.getDouble(3) / jahre;
                        summe += mon.tm;

                        sum.tm += mon.tm;
                        sum.tage += mon.tage;

                    } else {

                        if (wertTyp == 1) {
                            mon.group = rs.getString(1);
                        } else {
                            mon.group = (rs.getInt(1) - 1) + "-" + rs.getString(1);
                        }
                        mon.tm = rs.getDouble(2) / stationen;
                        mon.tage = rs.getDouble(3) / stationen;
                    }
                    werte.add(mon);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } /* while (rs.next()) */
            rs.close();
            if (group.equals("monat")) {
                sum.group = "Ges";
                sum.groupN = 99;
                werte.add(sum);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (st != null && !st.isClosed()) st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return true;
    }

    public Vector<Tval> getWerte(String w_s) {
        if (werte == null || !w_s.equals(wertTyp)) {
            try {
                calc(w_s);
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
    private double summe;

    public void setWerttyp(int w) {
        wertTyp = w;
    }

    public String getGroup() {

        switch (wertTyp) {
            case 0:
                return "Monat";
            case 1:
                return "Jahr";
            case 2:
                return "Winter";
        }
        return "";
    }

    public String getVon() {
        return von;
    }

    public String getBis() {
        return bis;
    }

}
