package de.gdoeppert.klima.model;

import java.io.Serializable;

public class MonatValTN implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    public int monat;
    public double tm;
    public double nds;
    public int nTage;

    public int getnTage() {
        return nTage;
    }

    public void setnTage(int nTage) {
        this.nTage = nTage;
    }

    public int getMonat() {
        return monat;
    }

    public double getTm() {
        return tm;
    }

    public double getNds() {
        return nds;
    }

    public String getTmS() {
        return String.format("%-6.2f", tm);
    }

    public String getNdsS() {
        return String.format("%6.1f", nds);
    }
}