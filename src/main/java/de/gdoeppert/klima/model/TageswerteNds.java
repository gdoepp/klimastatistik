package de.gdoeppert.klima.model;

public class TageswerteNds {
    public int monat;
    public int tag;
    public double rs;
    public double fm;
    public double nm;
    public double sd;
    public double hm;

    public double getFm() {
        return fm;
    }

    public double getHm() {
        return hm;
    }

    public int getMonat() {
        return monat;
    }

    public int getMonattag() {
        return monat * 100 + tag;
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

    public int getTag() {
        return tag;
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

}