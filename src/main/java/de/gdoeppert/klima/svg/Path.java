package de.gdoeppert.klima.svg;

import de.gdoeppert.klima.guibase.IPath;

public class Path implements IPath {

    private StringBuffer path = new StringBuffer();

    /*
     * (non-Javadoc)
     *
     * @see de.gdoeppert.klima.guibase.IPath#lineTo(double, double)
     */
    @Override
    public IPath lineTo(double x, double y) {
        path.append(" L" + x + " " + y);
        return this;
    }

    /*
     * (non-Javadoc)
     *
     * @see de.gdoeppert.klima.guibase.IPath#moveTo(double, double)
     */
    @Override
    public IPath moveTo(double x, double y) {
        path.append(" M" + x + " " + y);
        return this;
    }

    public String getPath() {
        return path.toString();
    }

    /*
     * (non-Javadoc)
     *
     * @see de.gdoeppert.klima.guibase.IPath#resetPath()
     */
    @Override
    public void resetPath() {
        path = new StringBuffer();
    }

}
