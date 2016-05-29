package de.gdoeppert.klimastatistik.gui;

import android.util.Log;
import android.view.View;

import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 03.01.16.
 */
public abstract class TableHandler extends KlimaHandler {

    public abstract KlimaGridAdapter getAdapter();

    public int getLayoutId() {
        return R.layout.table;
    }

    @Override
    public void setView(View v) {
        table = (TableView) v;
    }

    public int getLineCount() {
        return getAdapter().getCount();
    }

    public int getColCount() {
        return getAdapter().getColumns();
    }


    @Override
    public void postprocess() {

        dirty = false;
        table.setAdapter(getAdapter());
        Log.i("TableHandler", "setAdapter");
        table.invalidate();

    }

    protected TableView table = null;
}
