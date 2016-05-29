package de.gdoeppert.klima.model;

import java.io.Serializable;

public class Jahre implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    private Periode periode = Periode.alle;

    public String getVon() {
        switch (periode) {
            case alle:
                return "0";
            case p6190:
                return "1961";
            case p8110:
                return "1981";
            case jahr:
                return von;
        }

        return von;
    }

    public void setVon(String von) {
        this.von = von;
    }

    public String getBis() {
        switch (periode) {
            case alle:
                return "2199";
            case p6190:
                return "1990";
            case p8110:
                return "2010";
            case jahr:
                return bis;
        }
        return bis;
    }

    public void setBis(String bis) {
        this.bis = bis;
    }

    public void setPeriode(Periode periode) {
        this.periode = periode;
    }

    public enum Periode {alle, p6190, p8110, jahr}

    ;

    String von;
    String bis;
}
