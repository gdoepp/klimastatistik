package de.gdoeppert.klima.guibase;

public interface IPath {

    public abstract IPath lineTo(double x, double y);

    public abstract IPath moveTo(double x, double y);

    public abstract void resetPath();

}