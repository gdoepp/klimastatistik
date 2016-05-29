package de.gdoeppert.klimastatistik.gui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 06.01.16.
 */
public abstract class KlimaGridAdapter extends BaseAdapter {

    public KlimaGridAdapter(KlimaStatActivity act) {
        activity = act;
    }

    public abstract int getColumns();

    public abstract String getItem(int pos);

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            LayoutInflater vi = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = vi.inflate(R.layout.tableitem, null);
        }
        TextView txt = (TextView) view.findViewById(R.id.txtTableItem);
        txt.setText(getItem(i));

        return view;
    }

    protected KlimaStatActivity activity;
}
