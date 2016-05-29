package de.gdoeppert.klima.model;

import java.io.Serializable;

public class GradtageParm implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int werttyp;
    private double cut = 20;
    private double basis = 15;

    public int getWerttyp() {
        return werttyp;
    }

    public void setWerttyp(int werttyp) {
        this.werttyp = werttyp;
    }

    public double getCut() {
        return cut;
    }

    public void setCut(double cut) {
        this.cut = cut;
    }

    public double getBasis() {
        return basis;
    }

    public void setBasis(double basis) {
        this.basis = basis;
    }

}
