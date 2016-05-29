package de.gdoeppert.klima.model;

import java.io.Serializable;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

public class JahresverlaufNds implements Serializable {

    DbBase dbBean;

    Jahre jahre;

    Station station;

    JahresVergleich jahresvergleich;

    public JahresVergleich getJahresvergleich() {
        return jahresvergleich;
    }

    public void setJahresvergleich(JahresVergleich jahresvergleich) {
        this.jahresvergleich = jahresvergleich;
    }

    JahresVerlauf jahresverlauf;

    public JahresVerlauf getJahresverlauf() {
        return jahresverlauf;
    }

    public void setJahresverlauf(JahresVerlauf jahresverlauf) {
        this.jahresverlauf = jahresverlauf;
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

    public Station getStation() {
        return station;
    }

    public void setStation(Station station) {
        this.station = station;
    }

    public String calc(boolean doVgl, String vgl) throws SQLException {

        Integer fenster = 1;
        if (vgl != null && vgl.equals("true")) {
            fenster = jahresverlauf.getFensterVgl();
            vgl = jahresvergleich.getVglJahr();
        } else {
            fenster = jahresverlauf.getFensterVerl();
        }

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

            if (doVgl) {
                tage_vgl = eval(st, stat, vgl, vgl, fenster);
            } else {
                tage = eval(st, stat, von, bis, fenster);
            }
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "failure";
        }
        return "ok";
    }

    Vector<TageswerteNds> eval(Statement st, String stat, String von,
                               String bis, int fenster) throws SQLException {
        ResultSet rs = st
                .executeQuery("select monat, tag, avg(rs), avg(nm), avg(sd), avg(um), avg(fm) from " + dbBean.getSchema() + "tageswerte "
                        + stat
                        + " and jahr between "
                        + von
                        + " and "
                        + bis
                        + " group by monat, tag order by monat,tag");

        Vector<TageswerteNds> tage = new Vector<TageswerteNds>(366);

        while (rs.next()) {
            int monat = rs.getInt(1);
            int tag = rs.getInt(2);
            if (monat == 2 && tag == 29)
                continue;

            TageswerteNds tv = new TageswerteNds();
            tv.monat = monat;
            tv.tag = tag;
            if (rs.getString(3) != null) {
                tv.rs = rs.getDouble(3);
            }
            if (rs.getString(4) != null) {
                tv.nm = rs.getDouble(4);
            }
            if (rs.getString(5) != null) {
                tv.sd = rs.getDouble(5);
            }
            if (rs.getString(6) != null) {
                tv.hm = rs.getDouble(6);
            }
            if (rs.getString(7) != null) {
                tv.fm = rs.getDouble(7);
            }
            tage.add(tv);

        } /* while (rs.next()) */
        rs.close();

        if (fenster > 0) {
            Vector<TageswerteNds> tage2 = new Vector<TageswerteNds>(366);
            for (int j = 0; j < tage.size(); j++) {
                TageswerteNds tv = new TageswerteNds();
                for (int k = -fenster; k <= fenster; k++) {
                    TageswerteNds tv0 = tage.get((j + k + tage.size())
                            % tage.size());
                    double f = (k == 0 ? 2 : 1);

                    tv.fm += tv0.fm * f;
                    tv.rs += tv0.rs * f;
                    tv.nm += tv0.nm * f;
                    tv.sd += tv0.sd * f;
                    tv.hm += tv0.hm * f;
                }
                tv.fm /= 2 * fenster + 2;
                tv.rs /= 2 * fenster + 2;
                tv.nm /= 2 * fenster + 2;
                tv.sd /= 2 * fenster + 2;
                tv.hm /= 2 * fenster + 2;
                tv.monat = tage.get(j).monat;
                tv.tag = tage.get(j).tag;
                tage2.add(tv);
            }
            tage = tage2;
        }
        return tage;
    }

    public Vector<TageswerteNds> getTage(String vgl) {
        if (tage == null) {
            try {
                calc(false, vgl);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return tage;
    }

    public Vector<TageswerteNds> getTageVgl(String vgl) {
        if (tage_vgl == null) {
            try {
                calc(true, vgl);
            } catch (SQLException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return tage_vgl;
    }

    private Vector<TageswerteNds> tage = null;
    private Vector<TageswerteNds> tage_vgl = null;
}
