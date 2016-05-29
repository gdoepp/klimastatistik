package de.gdoeppert.klimastatistik.model;

import android.os.NetworkOnMainThreadException;
import android.util.Log;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

import de.gdoeppert.klima.model.DbBase;
import de.gdoeppert.klima.model.StationUpdater;

public class StationUpdaterFromINet extends StationUpdater implements Closeable {

    private FTPClient ftpClient;

    public StationUpdaterFromINet(DbBase dbBean) {
        super(dbBean);

        ftpClient = new FTPClient();
        try {
            ftpClient.connect("ftp-cdc.dwd.de");
            ftpClient.login("anonymous", "gd");
            ftpClient.setKeepAlive(true);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        } catch (NetworkOnMainThreadException ne) {
            ftpClient = null;
            return;
        }
        int rc = ftpClient.getReplyCode();
        Log.i("UpdaterNet", "ftp login rc = " + rc);
        ftpClient.enterLocalPassiveMode();
        Log.i("UpdaterNet", "ftp passv rc = " + rc);
    }

    public void insertStationen() throws IOException {
        int rc;
        if (ftpClient == null) return;
        ftpClient.setFileType(FTPClient.ASCII_FILE_TYPE);
        rc = ftpClient.getReplyCode();
        System.err.println("ftp ascii rc = " + rc);
        ftpClient.setBufferSize(102400);

        InputStream finList = ftpClient.retrieveFileStream(
                "/pub/CDC/observations_germany/climate/daily/kl/recent/KL_Tageswerte_Beschreibung_Stationen.txt");
        rc = ftpClient.getReplyCode();
        Log.i("UpdaterNet", "ftp retrieve rc = " + rc);
        if (rc == 150) {
            insertStationList(finList);
            finList.close();
            ftpClient.completePendingCommand();
            rc = ftpClient.getReplyCode();
            Log.i("UpdaterNet", "ftp complete rc = " + rc);
        }

    }

    public void insertWetterlagen() {
        String pathname = "https://www.dwd.de/DE/leistungen/wetterlagenklassifikation/online_wlkdaten.txt?view=nasPublication";

        try {
            URL url = new URL(pathname);
            HttpURLConnection httpClient = (HttpURLConnection) url.openConnection();
            httpClient.setRequestProperty("Accept", "*/*");
            httpClient.setRequestProperty("Accept-Encoding", "identity");
            httpClient.setRequestProperty("User-Agent", "Wget/1.17.1");
            InputStream inp = httpClient.getInputStream();
            int rc = httpClient.getResponseCode();
            Log.d("http", "rc=" + rc);
            if (rc < 300) {
                insertWetterlage(inp);
            }
            httpClient.disconnect();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String insertTageswerte(boolean historical, int statNr0) {
        int rc;
        String filename = null;

        Vector<Integer> stationen = new Vector<Integer>();
        Vector<Integer> stationen_upd = new Vector<Integer>();

        if (ftpClient == null) return "NOK";

        if (statNr0 < 0) {

            Statement st;
            try {
                st = dbBean.getDb().createStatement();
                ResultSet rs = st
                        .executeQuery("select stat from " + dbBean.getSchema() + "station where aktuell is not null");
                while (rs.next()) {
                    stationen.add(rs.getInt(1));
                }
                rs.close();
                st.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }

        } else {

            stationen.add(statNr0);
        }

        try {
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
        } catch (IOException e) {
            e.printStackTrace();
            return "";
        }

        ftpClient.setBufferSize(102400);

        for (int statNr : stationen) {
            String statid = String.format("%05d", statNr);


            if (historical) {
                String pathname = "/pub/CDC/observations_germany/climate/daily/kl/historical";

                // /pub/CDC/observations_germany/climate/daily/kl/recent/tageswerte_KL_02627_akt.zip

                try {
                    ftpClient.changeWorkingDirectory(pathname);
                    FTPFile[] files = ftpClient.listFiles();
                    filename = null;
                    for (FTPFile f : files) {
                        if (f.getName().startsWith("tageswerte_" + statid)) {
                            filename = f.getName();
                            break;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }

            } else {
                String pathname = "/pub/CDC/observations_germany/climate/daily/kl/recent";
                try {
                    ftpClient.changeWorkingDirectory(pathname);
                } catch (IOException e) {
                    e.printStackTrace();
                    continue;
                }
                filename = "tageswerte_KL_" + statid + "_akt.zip";
            }

            if (filename == null) {
                continue;
            }

            InputStream fin;
            try {
                fin = ftpClient.retrieveFileStream(filename);
                rc = ftpClient.getReplyCode();
                Log.i("UpdaterNet", "ftp retrieve rc = " + rc);
                if (fin != null) {
                    updateZip(fin, statNr);
                    stationen_upd.add(statNr);
                    fin.close();
                    ftpClient.completePendingCommand();

                    rc = ftpClient.getReplyCode();
                    Log.i("UpdaterNet", "ftp complete rc = " + rc);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        StringBuffer upd_ok = new StringBuffer();
        for (int sid : stationen_upd) {
            if (upd_ok.length() > 0) {
                upd_ok.append(", ");
            }
            upd_ok.append(sid);
        }
        return upd_ok.toString();
    }

    @Override
    public void close() {
        if (ftpClient != null && ftpClient.isConnected())
            try {
                ftpClient.logout();
                ftpClient.disconnect();
            } catch (IOException e) { // nothing
            }

    }

}
