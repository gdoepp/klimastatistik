package de.gdoeppert.klima.model;

import android.support.annotation.NonNull;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by gd on 12.04.16.
 */
public class Wertermittler {
    DbBase dbBean;
    Station station;

    double[] tageMonat = new double[]{31, 28.25, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
    Tval tv = null;

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

    Tval eval(Statement st, String stat, String von, String bis, String condition)
            throws SQLException {

        ResultSet rs = st
                .executeQuery("select monat, avg(tm) as tm, avg(tn), avg(tx), min(tn), max(tx), avg(rs)," +
                        " avg(nm), avg(sd), avg(schnee), avg(schnee>0), avg(rs>0), avg(sd>= ( 13-abs(monat-6) - (abs(monat-6)>3))), avg(sd<=1), count(*)  "
                                + " from " + dbBean.getSchema() + "tageswerte "
                                + stat
                                + " and jahr between "
                                + von
                                + " and "
                                + bis
                                + " and " + condition
                );

        Tval tv = new Tval();
        if (rs.next()) {
            try {

                int monat = rs.getInt(1);

                tv.monat = rs.getString(1);
                tv.monat_i = monat;

                double faktor = !condition.contains("tag") ? tageMonat[monat - 1] : 1;

                if (rs.getString(2) != null) {
                    tv.tm = rs.getDouble(2);
                }
                if (rs.getString(3) != null && rs.getString(4) != null) {
                    tv.tn = rs.getDouble(3);
                    tv.tx = rs.getDouble(4);
                }
                if (rs.getString(5) != null && rs.getString(6) != null) {
                    tv.tnk = rs.getDouble(5);
                    tv.txk = rs.getDouble(6);
                }
                if (rs.getString(7) != null) {
                    tv.rs = rs.getDouble(7) * faktor;
                }
                if (rs.getString(8) != null) {
                    tv.nm = rs.getDouble(8);
                }
                if (rs.getString(9) != null) {
                    tv.sd = rs.getDouble(9) * faktor;
                }
                if (rs.getString(10) != null) {
                    tv.schnee = rs.getDouble(10);
                }
                if (rs.getString(11) != null) {
                    tv.schneeAnt = rs.getDouble(11);
                }
                if (rs.getString(12) != null) {
                    tv.rsAnt = rs.getDouble(12);
                }
                if (rs.getString(13) != null) {
                    tv.heiterAnt = rs.getDouble(13);
                }
                if (rs.getString(14) != null) {
                    tv.truebAnt = rs.getDouble(14);
                }
                tv.tage = rs.getInt(15);
            } catch (Exception e) {
            }
        } /* while (rs.next()) */
        rs.close();


        return tv;
    }

    void addWetterlage(Statement st, int monat_i, String von, String bis, String condition) {

        try {

            ResultSet rsWetterlage = st
                    .executeQuery("select substr(kennung,1,2)||substr(kennung,5,1), count(*) "
                            + "from " + dbBean.getSchema() + "wetterlage inner join klasse using(nummer) where monat=" + monat_i
                            + " and jahr between " + von + " and " + bis
                            + " and nummer <>0 and " + condition
                            + " group by substr(kennung,1,2)||substr(kennung,5,1) ");
            int anzges = 0;
            Wetterlage wl = new Wetterlage();
            while (rsWetterlage.next()) {
                int anz = rsWetterlage.getInt(2);
                String kennung = rsWetterlage.getString(1);

                wl.percentages.put(kennung, (double) anz);

                Double d = wl.percentages.get(kennung.substring(0, 2));
                if (d == null) {
                    wl.percentages.put(kennung.substring(0, 2), (double) anz);
                } else {
                    wl.percentages.put(kennung.substring(0, 2), d + anz);
                }

                d = wl.percentages.get(kennung.substring(2, 3));
                if (d == null) {
                    wl.percentages.put(kennung.substring(2, 3), (double) anz);
                } else {
                    wl.percentages.put(kennung.substring(2, 3), d + anz);
                }

                anzges += anz;
            }

            for (Map.Entry<String, Double> e : wl.percentages.entrySet()) {
                e.setValue(1.0 * e.getValue() / anzges);
            }

            tv.wl = wl;
            rsWetterlage.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @NonNull
    protected TvalDistrib[] getTvalDistrib(Statement st, String stat, String von, String bis, String condition) throws SQLException {
        ResultSet rs = st
                .executeQuery("select count(tm), avg(tm) as tmm, cast(tm+( -(tm<0)+0.5) as integer) as grp "
                                + " from " + dbBean.getSchema() + "tageswerte "
                                + stat
                                + " and jahr between "
                                + von
                                + " and "
                                + bis
                                + " and tm not null and " + condition
                                + " group by grp  order by tmm"
                );


        int j = 0;
        int j0 = Integer.MAX_VALUE - 1;
        Vector<TvalDistrib> werte = new Vector<TvalDistrib>();
        while (rs.next()) {
            j = rs.getInt(3);

            for (int k = j0 + 1; k < j; k++) {
                TvalDistrib tr = new TvalDistrib();
                tr.anz = 0;
                tr.wert = k;
                werte.add(tr);
            }
            j0 = j;

            TvalDistrib tr = new TvalDistrib();
            tr.anz = rs.getInt(1);
            tr.wert = rs.getDouble(2);
            werte.add(tr);
        }
        return werte.toArray(new TvalDistrib[0]);
    }

    static public class Wetterlage {

        public Map<String, Double> percentages = new HashMap<String, Double>();


        public String format(double d) {
            return String.format("%3.0f%%", d * 100);
        }
    }

    static public class Tval {
        public int monat_i;
        public String monat;
        public int tag_i;
        public String tag;

        public double tm;
        public double tm_u;
        public double tm_o;
        public double tn;
        public double tx;
        public double tnk;
        public double txk;
        public double rs;
        public double schnee;
        public double schneeAnt;
        public double nm;
        public double sd;
        public double rsAnt;
        public double heiterAnt;
        public double truebAnt;
        public int tage;

        public Wetterlage wl;

        public TvalDistrib[] tmDist = null;

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
            return String.format("%5.1f", tm);
        }

        public String getTm_oS() {
            return String.format("%5.1f", tm_o);
        }

        public String getTm_uS() {
            return String.format("%5.1f", tm_u);
        }

        public String getTnS() {
            return String.format("%5.1f", tn);
        }

        public String getTnkS() {
            return String.format("%5.1f", tnk);
        }

        public String getTxS() {
            return String.format("%5.1f", tx);
        }

        public String getTxkS() {
            return String.format("%5.1f", txk);
        }


        public double getSchnee() {
            return schnee;
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

        public String getSchneeS() {
            return String.format("%4.1f", schnee);
        }

        public String getSchneeAntS() {
            return String.format("%4.1f%%", schneeAnt * 100);
        }

        public String getNdsAntS() {
            return String.format("%4.1f%%", rsAnt * 100);
        }

        public String getHeiterAntS() {
            return String.format("%4.1f%%", heiterAnt * 100);
        }

        public String getTruebAntS() {
            return String.format("%4.1f%%", truebAnt * 100);
        }


        public String getNmS() {
            return String.format("%5.1f", nm);
        }

        public String getRsS() {
            return String.format("%5.1f", rs);
        }

        public String getSdS() {
            return String.format("%5.1f", sd);
        }

    }

    static public class TvalDistrib {
        public int anz;
        public double wert;
    }
}
