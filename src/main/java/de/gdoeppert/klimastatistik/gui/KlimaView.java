package de.gdoeppert.klimastatistik.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by gd on 03.01.16.
 */
public class KlimaView extends KlimaViewBase {


    protected final Paint painter = new Paint();

    protected int w;
    protected int h;
    private ViewHandler viewHandler = null;

    public KlimaView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setViewhandler(ViewHandler h) {

        viewHandler = h;
    }


    protected void doDraw(Paint painter, Canvas canvas) {

    }

    @Override
    protected void onDraw(final Canvas canvas) {

        if (viewHandler == null) return;

        canvas.drawColor(Color.LTGRAY);

        if (viewHandler != null) {
            viewHandler.doDraw0(painter, canvas);
        }


        w = getWidth();
        h = getHeight();

        painter.reset();

        Log.i("KlimaView", "w: " + w + ", h: " + h);

        if (viewHandler.needsLandscape() && h > w) {
            canvas.rotate(-90);
            canvas.translate(-h, 0);
            int t = w;
            w = h;
            h = t;
        }

        float scalex = w / 1600f;
        float scaley = h / 900f;

        if (scalex > scaley * 1.1f) scalex = scaley * 1.1f;
        else if (scalex * 1.1f < scaley) scaley = scalex * 1.1f;

        canvas.scale(scalex, scaley);

        painter.reset();
        if (viewHandler != null) {
            viewHandler.doDraw(painter, canvas);
        }

    }

    public ViewHandler getViewHandler() {
        return viewHandler;
    }
}
