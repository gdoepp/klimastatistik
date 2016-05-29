package de.gdoeppert.klima.model;

import java.io.Serializable;

public class TrendParm implements Serializable {

    private int wertidx;

    public int getWertidx() {
        return wertidx;
    }

    public void setWertidx(int werttyp) {
        this.wertidx = werttyp;
    }

    private int fensterTrend = 0;

    public int getFensterTrend() {
        return fensterTrend;
    }

    public void setFensterTrend(int fenster) {
        this.fensterTrend = fenster;
    }

    static final String[] wertName = {"Niederschlag", "Windstärke",
            "Bewölkung", "Sonnenscheinstunden", "Feuchte", "Mitteltemperatur",
            "Minimaltemperatur", "Maximaltemperatur", "Luftdruck"};
    static final String[] wertAtt = {"rs", "fm", "nm", "sd", "um", "tm", "tn",
            "tx", "pm"};
}
