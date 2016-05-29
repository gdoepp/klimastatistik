package de.gdoeppert.klima.model;

import java.io.Serializable;

public class JahresVerlauf implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int fensterVgl;

    public int getFensterVgl() {
        return fensterVgl;
    }

    public void setFensterVgl(int fenster) {
        this.fensterVgl = fenster;
    }

    private int fensterVerl;

    public int getFensterVerl() {
        return fensterVerl;
    }

    public void setFensterVerl(int fenster) {
        this.fensterVerl = fenster;
    }

}
