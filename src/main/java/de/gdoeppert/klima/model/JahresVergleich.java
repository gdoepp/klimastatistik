package de.gdoeppert.klima.model;

import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class JahresVergleich implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;

    public String getVglJahr() {
        return vglJahr;
    }

    public void setVglJahr(String vglJahr) {
        this.vglJahr = vglJahr;
    }

    String vglJahr = String.valueOf(GregorianCalendar.getInstance().get(
            Calendar.YEAR));
}
