package de.gdoeppert.klimastatistik.gui;

import android.util.Log;
import android.view.View;

import java.util.Vector;

/**
 * Created by gd on 06.01.16.
 */
public abstract class KlimaHandler {

    public int getHelpID() {
        return 0;
    }

    public abstract void setViewParam(Vector<?> v);

    public abstract Vector<?> doWork();

    protected void startWork() {
        if (activity != null && activity.getStation().getSelStat() != null) {
            stat = activity.getStation().getSelStat().getStat();
        }
        dirty = false;
    }

    public void setActivity(KlimaStatActivity act) {
        activity = act;
    }

    public KlimaStatActivity getActivity() {
        return activity;
    }

    public boolean needsUpdate() {
        Log.d("KlimaHandler", "check update, dirty: " + dirty + ", activity: " + (activity == null) + ", stat: " + stat);
        return dirty || activity == null || activity.getStation().getSelStat() == null || activity.getStation().getSelStat().getStat() != stat;
    }

    public abstract int getLayoutId();

    public abstract void postprocess();

    public abstract void setView(View v);

    public boolean prefersFullpage() {
        return false;
    }

    public void setMode(String s) {
    }

    public void setDirty() {
        dirty = true;
    }

    protected KlimaStatActivity activity;
    protected int stat = -1;
    protected boolean dirty = true;
}
