package de.gdoeppert.klimastatistik.gui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.View;

import java.util.Vector;

import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 03.01.16.
 */
public abstract class ViewHandler extends KlimaHandler {

    public void doDraw0(Paint painter, Canvas canvas) {
    }

    @Override
    public void setViewParam(Vector<?> v) {

    }

    @Override
    public Vector<?> doWork() {
        return null;
    }

    @Override
    public boolean needsUpdate() {

        if (getActivity() == null || super.needsUpdate()) return true;

        return getActivity().getStation().getSelStat().getStat() != stat;
    }

    public abstract void doDraw(Paint painter, Canvas canvas);

    public boolean needsLandscape() {
        return true;
    }

    public int getLayoutId() {
        return R.layout.diagram;
    }

    public void setView(View v) {

        klimaView = (KlimaView) v;
        klimaView.setViewhandler(this);
    }

    public void postprocess() {
        klimaView.invalidate();
    }

    public boolean prefersFullpage() {
        return true;
    }

    protected KlimaView klimaView;


}

