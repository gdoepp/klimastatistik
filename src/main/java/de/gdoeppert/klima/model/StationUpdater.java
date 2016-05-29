package de.gdoeppert.klima.model;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.sql.BatchUpdateException;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class StationUpdater {

    protected DbBase dbBean;
    String schema;

    private DbBase getDbBean() {
        return dbBean;
    }

    public StationUpdater(DbBase dbBean2) {

        if (schema == null) {
            schema = "";
        }
        if (!schema.isEmpty() && !schema.endsWith(".")) {
            schema = schema + ".";
        }
        this.schema = dbBean2.getSchema();

        this.dbBean = dbBean2;

    }
    // 164 19470101 20151223 54 53.0316 13.9907 Angermünde

    public void initDB() {
        try {
            // ResultSet x = dbBean.db.getMetaData().getTables(null, null,
            // "tageswerte", null);
            if (/* !x.next() || */ true) {

                Statement st = dbBean.getStatement();
                try {
                    st.executeUpdate("drop table " + schema + "station");
                } catch (SQLException e) {

                }
                try {
                    st.executeUpdate("drop table " + schema + "tageswerte");
                } catch (SQLException e) {

                }
                try {
                    st.executeUpdate("drop table " + schema + "wetterlage");
                } catch (SQLException e) {

                }

                st.execute("create table " + schema + "station (stat int, name varchar(100) unique,hoehe numeric(5),"
                        + "aktiv_von date, aktiv_bis date, latitude numeric(7,4), longitude numeric(7,4), aktuell date, primary key(stat))");

                st.execute("create table " + schema
                        + "wetterlage (jahr int, monat int, tag int, nummer int, primary key(jahr, monat, tag))");

                st.execute("CREATE TABLE " + schema + "tageswerte (" + "stat integer NOT NULL,"
                        + "jahr integer NOT NULL," + "monat integer NOT NULL," + "tag integer NOT NULL,"
                        + "tm numeric(3,1)," + "tm0 numeric(3,1)," + "tx numeric(3,1)," + "tn numeric(3,1),"
                        + "tnb numeric(5,2)," + "pm numeric(6,1)," + "rs numeric(4,1)," + "fm numeric(4,1),"
                        + "nm numeric(4,1)," + "sd numeric(3,1)," + "um numeric(3,0)," + "schnee integer, "
                        + "qual integer, " + "primary key(stat, monat, tag, jahr)" + ")");

                st.execute("CREATE INDEX dwd_tgw_j ON " + schema + "tageswerte (stat, jahr)");
                st.execute("CREATE INDEX dwd_tgw_tn ON " + schema + "tageswerte (stat, tn)");
                st.execute("CREATE INDEX dwd_tgw_tx ON " + schema + "tageswerte (stat, tx)");
                st.execute("CREATE INDEX dwd_tgw_rs ON " + schema + "tageswerte (stat, rs)");
                st.execute("CREATE INDEX dwd_tgw_sd ON " + schema + "tageswerte (stat, sd)");
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void insertWetterlage(InputStream inp) {
        // 03.2014 24 4 24 6 15 1 1 1 1 1 2 2 1 5 10 10 5 9 10 4 29 29 15 15 11 33 33 23 3 21 5

        String sql = "select max(jahr*10000+monat*100+tag) from " + schema + "wetterlage";
        int maxDat = 0;
        ResultSet rs;
        try {
            Statement st = dbBean.getDb().createStatement();
            rs = st.executeQuery(sql);
            if (rs.next()) {
                maxDat = rs.getInt(1);
                Log.d("insWetterlage", "maxDat=" + maxDat);
            }
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        sql = "insert into " + schema + "wetterlage(jahr, monat, tag, nummer) values(?,?,?,?)";
        PreparedStatement pst;
        try {
            boolean supportsbatch = dbBean.getDb().getMetaData().supportsBatchUpdates();
            supportsbatch = false;
            pst = dbBean.getDb().prepareStatement(sql);

            Log.d("insWetterlage", "reading...");

            BufferedReader rd = new BufferedReader(new InputStreamReader(inp, "ISO8859-1"));

            while (true) {
                String line = rd.readLine();
                if (line == null) break;
                //Log.d("insWetterlage", "line="+line);
                if (line.matches("[0-9]+[.][0-9]+[ ].*")) {
                    String[] fields = line.split("[ ]+");
                    String[] mmjjjj = fields[0].split("[.]");
                    int monat = Integer.valueOf(mmjjjj[0]);
                    int jahr = Integer.valueOf(mmjjjj[1]);
                    if (jahr*10000+monat*100+31 < maxDat ) {
                        continue;
                    }
                    for (int t = 1; t < fields.length; t++) {
                        if (jahr * 10000 + monat * 100 + t > maxDat) {
                            pst.setInt(1, jahr);
                            pst.setInt(2, monat);
                            pst.setInt(3, t);
                            pst.setInt(4, Integer.valueOf(fields[t]));
                            if (supportsbatch) {
                                pst.addBatch();
                            } else {
                                pst.executeUpdate();
                            }
                        }
                    }
                    if (supportsbatch) {
                        pst.executeBatch();
                    }
                }
            }

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NumberFormatException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    protected void insertStationList(InputStream inp) {
        try {
            Statement st0 = getDbBean().getStatement();
            st0.execute("delete from " + schema + "station)");
            st0.close();

            String sql = "insert into " + schema
                    + "station(stat, name, hoehe, aktiv_von, aktiv_bis, latitude, longitude) values(?,?,?,?,?,?,?)";

            PreparedStatement st = dbBean.getDb().prepareStatement(sql);

            BufferedReader rd = new BufferedReader(new InputStreamReader(inp, "ISO8859-1"));

            int lineNr = 0;

            while (rd.ready()) {

                String line = rd.readLine();

                if (line.charAt(0) != ' ') {
                    continue;
                }

                int statId = Integer.valueOf(line.substring(5, 11).trim());

                Date aktiv_von = toDate(line.substring(12, 20));
                Date aktiv_bis = toDate(line.substring(21, 29));

                double lat = Double.valueOf(line.substring(48, 56));
                double lng = Double.valueOf(line.substring(58, 66));

                int hoehe = Integer.valueOf(line.substring(40, 44).trim());
                String name = line.substring(67, 108).trim();

                st.setInt(1, statId);
                st.setString(2, name);
                st.setInt(3, hoehe);
                st.setDate(4, aktiv_von);
                st.setDate(5, aktiv_bis);
                st.setDouble(6, lat);
                st.setDouble(7, lng);

                st.addBatch();

                lineNr++;
                if (lineNr % 100 == 0) {
                    execBatch(st);
                    System.err.println("read " + lineNr + " lines");
                }
            }

            execBatch(st);
            st.close();

        } catch (BatchUpdateException e) {
            e.getNextException().printStackTrace();
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    protected void update(InputStream inp, int stat) {

        int maxDat = 0;

        boolean autocommit = true;
        boolean supportsbatch = false;
        try {
            Statement st;
            st = dbBean.getDb().createStatement();
            ResultSet rs = st.executeQuery("select max(jahr*10000+monat*100+tag) from " + schema + "tageswerte " +
                    "where stat=" + stat + " and qual>1");
            if (rs.next()) {
                maxDat = rs.getInt(1);
                Log.d("update tageswerte", "maxDat=" + maxDat);
            }
            rs.close();
            st.execute("delete from " + schema + "tageswerte where stat=" + stat + " and jahr*10000+monat*100+tag > " + maxDat);
            Log.d("Updater", "repeat after " + maxDat);
            st.close();
        } catch (SQLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }

        PreparedStatement pst = null;
        try {
            String sql = "insert into " + schema + "tageswerte(stat, jahr, monat, tag, "
                    + "tm, tm0, tx, tn, tnb, pm, rs, fm, nm, sd, um, schnee, qual) values(?,?,?,?,"
                    + "?,?,?,?,?,?,?,?,?,?,?,?,?)";

            autocommit = dbBean.getDb().getAutoCommit();
            supportsbatch = dbBean.getDb().getMetaData().supportsBatchUpdates();
            supportsbatch = false; // Workaround for sqldroid
            dbBean.getDb().setAutoCommit(false);

            pst = dbBean.getDb().prepareStatement(sql);

            String[] fnames = {"STATIONS_ID", "MESS_DATUM", null, null, "LUFTTEMPERATUR", null,
                    "LUFTTEMPERATUR_MAXIMUM", "LUFTTEMPERATUR_MINIMUM", "LUFTTEMP_AM_ERDB_MINIMUM",
                    "LUFTDRUCK_STATIONSHOEHE", "NIEDERSCHLAGSHOEHE", "WINDGESCHWINDIGKEIT", "BEDECKUNGSGRAD",
                    "SONNENSCHEINDAUER", "REL_FEUCHTE", "SCHNEEHOEHE", "QUALITAETS_NIVEAU"};

            int[] fieldId = null;
            int[] typeId = new int[17 + 1];
            for (int j = 0; j < 17; j++) {
                typeId[j + 1] = (j < 4 || j > 14 ? Types.INTEGER : Types.NUMERIC);
            }

            BufferedReader rd = new BufferedReader(new InputStreamReader(inp));

            boolean title = true;
            String[] fields;
            String[] values;
            int lineNr = 0;

            dayloop:
            while (true) {

                String line = rd.readLine();
                if (line == null) break;

                line = line.replaceAll("^[ ]*", "");

                if (title) {
                    fields = line.split("[,; ]+");
                    title = false;
                    int k = 0;
                    fieldId = new int[fields.length];
                    for (String f : fields) {
                        fieldId[k] = -1;
                        for (int j = 0; j < fnames.length; j++) {
                            if (fnames[j] != null && fnames[j].equals(f)) {
                                fieldId[k] = j + 1;
                                break;
                            }
                        }
                        k++;
                    }

                } else {

                    values = line.split("[,; ]+");

                    if (values.length < 5)
                        continue;

                    for (int k = 0; k < values.length; k++) {
                        if (fieldId[k] == 2) { // mess_datum
                            int jmt = Integer.valueOf(values[k]);
                            if (jmt <= maxDat) {
                                continue dayloop;
                            }
                        }
                    }

                    for (int k = 0; k < values.length; k++) {

                        if (fieldId[k] < 0)
                            continue;

                        if (fieldId[k] == 2) { // mess_datum
                            int jmt = Integer.valueOf(values[k]);
                            int j = jmt / 10000;
                            int m = (jmt % 10000) / 100;
                            int t = (jmt % 100);
                            pst.setInt(2, j);
                            pst.setInt(3, m);
                            pst.setInt(4, t);
                            //Log.d("Updater", String.format("%s %d.%d.%d", values[0], t, m, j));

                            continue;
                        }

                        int mt = typeId[fieldId[k]];

                        String val = values[k].trim();

                        if (fieldId[k] == 5) {
                            if (val.equals("-999")) {
                                pst.setNull(6, Types.DOUBLE);
                            } else {
                                pst.setDouble(6, Double.valueOf(values[k]));
                            }
                        }

                        if (Types.DOUBLE == mt || Types.DECIMAL == mt || Types.NUMERIC == mt) {
                            if (val.equals("-999")) {
                                pst.setNull(fieldId[k], Types.DOUBLE);
                            } else {
                                pst.setDouble(fieldId[k], Double.valueOf(values[k]));
                            }
                        } else if (Types.INTEGER == mt) {
                            if (val.equals("-999")) {
                                pst.setNull(fieldId[k], Types.INTEGER);
                            } else {
                                pst.setInt(fieldId[k], Integer.valueOf(values[k]));
                            }
                        } else if (Types.DATE == mt) {
                            pst.setDate(fieldId[k], Date.valueOf(values[k]));
                        } else if (Types.VARCHAR == mt || Types.CHAR == mt) {
                            pst.setString(fieldId[k], values[k]);
                        } else {
                            System.err.println("type not found: " + mt);
                        }
                    }
                    if (supportsbatch) {
                        pst.addBatch();
                    } else {
                        pst.executeUpdate();
                    }
                    lineNr++;
                    if (supportsbatch && lineNr % 100 == 0) execBatch(pst);
                }
            }
            if (supportsbatch) {
                execBatch(pst);
            }
            dbBean.getDb().commit();

        } catch (BatchUpdateException e) {
            rollback();
            e.getNextException().printStackTrace();
            e.printStackTrace();
        } catch (SQLException e) {
            rollback();
            e.printStackTrace();
        } catch (IOException e) {
            rollback();
            e.printStackTrace();
        } finally {
            try {
                if (pst != null) {
                    pst.close();
                }
                getDbBean().getDb().setAutoCommit(autocommit);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    private void rollback() {
        try {
            getDbBean().getDb().rollback();
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    private void execBatch(PreparedStatement pst) throws SQLException {
        int[] rc = pst.executeBatch();
        int sum = 0;
        for (int r : rc) {
            if (r > 0) sum += r;
        }
        Log.d("Updater", "inserted " + sum + " lines");
        pst.clearBatch(); // Workaround for sqldroid
    }

    public void updateStationAktuell() {
        try {
            Statement st = dbBean.getDb().createStatement();
            st.execute("update " + schema +
                    "station set aktuell =  ( select max( cast (jahr || '-' || monat || '-' || tag as date)) from " +
                    schema + "tageswerte  where tageswerte.stat = station.stat)");
            st.close();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    protected void updateZip(InputStream fin, int stat) throws IOException {
        ZipInputStream zin = new ZipInputStream(fin);
        ZipEntry zen = null;
        while ((zen = zin.getNextEntry()) != null) {
            if (zen.getName().startsWith("produkt")) {
                update(zin, stat);
                break;
            }
        }
        zin.close();
    }

    private Date toDate(String d) {
        Calendar cal = new GregorianCalendar();
        cal.set(Integer.valueOf(d.substring(0, 4)), Integer.valueOf(d.substring(4, 6)), Integer.valueOf(d.substring(6, 8)));
        return new Date(cal.getTimeInMillis());
    }

    public String deleteTageswerte(int statid) {
        int rc = 0;
        Statement st = null;
        try {
            st = dbBean.getDb().createStatement();
            rc = st.executeUpdate("delete from " + schema +
                    "tageswerte where stat = " + statid);

            Log.d("StationUpdater", "deleted " + rc);

        } catch (SQLException e) {
        } finally {
            if (st != null) {
                try {
                    st.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                    // ignore
                }
            }
        }

        return "del " + rc;
    }
}
