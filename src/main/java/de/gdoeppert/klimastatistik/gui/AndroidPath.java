package de.gdoeppert.klimastatistik.gui;

import android.graphics.Path;

import de.gdoeppert.klima.guibase.IPath;

/**
 * Created by gd on 02.01.16.
 */
class AndroidPath implements IPath {
    private Path path = new Path();

    public Path getPath() {
        return path;
    }

    @Override
    public IPath lineTo(double x, double y) {
        path.lineTo((float) x, (float) y);
        return this;
    }

    @Override
    public IPath moveTo(double x, double y) {
        path.moveTo((float) x, (float) y);
        return this;
    }

    @Override
    public void resetPath() {
        path.reset();

    }
}
