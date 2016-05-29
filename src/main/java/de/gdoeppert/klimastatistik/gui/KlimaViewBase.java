package de.gdoeppert.klimastatistik.gui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import de.gdoeppert.klimastatistik.R;

/**
 * Created by gd on 16.01.16.
 */
public abstract class KlimaViewBase extends View {
    public KlimaViewBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        sp20 = context.getResources().getDimension(R.dimen.sp20);
        dp20 = context.getResources().getDimension(R.dimen.dp20);
    }

    public KlimaViewBase(Context context) {
        super(context);
    }

    protected float fromDimen(String val) {
        float unit = 1;
        if (val != null) {
            if (val.endsWith("sp")) {
                val = val.replace("sp", "");
                unit = sp20 / 20;
            } else if (val.endsWith("dip")) {
                val = val.replace("dip", "");
                unit = dp20 / 20;
            } else if (val.endsWith("px")) {
                val = val.replace("px", "");
            }
            return Float.valueOf(val) * unit;
        }
        return 0;
    }


    protected float sp20;
    protected float dp20;
}
