package de.gdoeppert.klima.model;

public class TagesWerteT {
    public int monat;
    public int tag;
    public double tm;
    public double tm_u;
    public double tm_o;
    public double tn;
    public double tx;
    public double tnk;
    public double txk;

    public int getMonat() {
        return monat;
    }

    public int getMonattag() {
        return monat * 100 + tag;
    }

    public int getTag() {
        return tag;
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
        return String.format("%-6.2f", tm_u);
    }

    public String getTnS() {
        return String.format("%-6.2f", tn);
    }

    public String getTnkS() {
        return String.format("%-6.2f", tnk);
    }

    public String getTxS() {
        return String.format("%-6.2f", tx);
    }

    public String getTxkS() {
        return String.format("%-6.2f", txk);
    }

}