package de.gdoeppert.klima.model;

import java.io.Serializable;

public class Stat implements Serializable {
    int stat;
    String name;
    int jahr1;
    int jahr2;
    double hoehe;

    public Stat() {
        stat = 0;
        name = "Gesamt";
        jahr1 = 0;
        jahr2 = 2999;
    }

    public int getStat() {
        return stat;
    }

    public String getName() {
        return name;
    }

    public int getJahr1() {
        return jahr1;
    }

    public int getJahr2() {
        return jahr2;
    }

    public double getHoehe() {
        return hoehe;
    }
    
    @Override
    public String toString() {
        return name;
    }
}