package de.gdoeppert.klima.model;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Vector;

import javax.sql.DataSource;

public class DbBase implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public DbBase() {
    }

    IDSProvider dsowner = null;

    public DbBase(IDSProvider dso) {
        dsowner = dso;
    }

    public void setup(String n, Connection conn) throws Exception {

        dbname = n;
        schema = "";

        if (conn == null) {
            message = "no database connection";
            throw new SQLException("no connection");
        } else {
            db = conn;
        }

        if (getDb() != null && dbname.equals("DB2")) {
            Statement st = getDb().createStatement();
            st.execute("set current sqlid='DWD'");
            schema = "DWD.";
        }

        if (getDb() != null && dbname.equals("PG")) {
            schema = "dwd.";
        }


        if (getDb() == null) {
            message = "no database connection";
            throw new SQLException("no database connection");
        }
    }

    public Statement getStatement() throws SQLException {

        if (getDb() == null) {
            if (dsowner != null) {
                try {
                    DataSource ds = dsowner.getDS();
                    if (ds != null) {
                        setup("PG", ds.getConnection());
                    } else {
                        setup("PG", dsowner.getConnection());
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        Statement st = getDb().createStatement();
        return st;

    }

    public void evalQueryV(ResultSet rs, String attr, int cols)
            throws SQLException {
        Vector<String> tflist = new Vector<String>();
        int cols_m = rs.getMetaData().getColumnCount();
        if (cols_m < cols)
            cols = cols_m;

        while (rs.next()) {
            try {
                String val = "";
                for (int sp = 1; sp <= cols; sp++) {
                    int ct = rs.getMetaData().getColumnType(sp);
                    if (dbname.equals("PG")
                            && (ct == Types.CHAR || ct == Types.VARCHAR))
                        val += new String(rs.getBytes(sp), "UTF-8");
                    else
                        val += rs.getString(sp);
                }
                tflist.add(val);
            } catch (Exception e) {
                tflist.add("???");
            }
        } /* while (rs.next()) */
        rs.close();
    }

    public String getCol(ResultSet rs, int sp) {
        try {
            return rs.getString(sp);
            /*
             * int ct = rs.getMetaData().getColumnType(sp);
			 * 
			 * if (dbname.equals("PG") && (ct == Types.CHAR || ct ==
			 * Types.VARCHAR) ) return rs.getString(sp); // return new
			 * String(rs.getBytes(sp),"UTF-8"); else return rs.getString(sp);
			 */
        } catch (Exception e) {
            return "???";
        }
    }

    public String getMessage() {
        return message;
    }

    public java.sql.Connection getDb() {
        return db;
    }


    public String getSchema() {
        return schema;
    }

    protected String message;
    private java.sql.Connection db;
    protected String dbname;
    private String schema = "";
}
