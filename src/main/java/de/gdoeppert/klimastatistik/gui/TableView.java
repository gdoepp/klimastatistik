package de.gdoeppert.klimastatistik.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Region;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

/**
 * Created by gd on 03.01.16.
 */
public class TableView extends KlimaViewBase {


    protected final Paint painter = new Paint();
    private int background = 0xff202020;
    private int background1 = 0xff404040;
    private int textcolor = 0xffffffff;
    private Float textsize = 18f;
    private int padding = 10;

    protected TableRect rect;

    protected int view_w;
    protected int view_h;
    KlimaGridAdapter adapter = null;
    private float mPreviousX;
    private float mPreviousY;

    protected int offset_row = 0;
    protected int offset_col = 0;

    public TableView(Context context, AttributeSet attrs) {

        super(context, attrs);

        for (int j = 0; j < attrs.getAttributeCount(); j++) {


            Log.i("TableView", "attr: " + attrs.getAttributeName(j) + " : " + attrs.getAttributeValue(j));

            if (attrs.getAttributeName(j).equals("background")) {
                background = attrs.getAttributeIntValue(j, Color.rgb(0x20, 0x20, 0x20));
            } else if (attrs.getAttributeName(j).equals("background1")) {
                background1 = (int) fromDimen(attrs.getAttributeValue(j));
            } else if (attrs.getAttributeName(j).equals("cellpadding")) {
                padding = (int) fromDimen(attrs.getAttributeValue(j));
            } else if (attrs.getAttributeName(j).equals("textcolor")) {
                textcolor = attrs.getAttributeIntValue(j, Color.WHITE);
            } else if (attrs.getAttributeName(j).equals("textsize")) {
                String val = attrs.getAttributeValue(j);
                textsize = fromDimen(val);
            }

        }
    }

    int[] cellw;
    int cellh;


    @Override
    protected void onDraw(final Canvas canvas) {

        view_w = getWidth();
        view_h = getHeight();
        Log.i("TableView", "view w: " + view_w + ", h: " + view_h);

        canvas.drawColor(background);
        if (adapter == null) return;

        if (rect == null || cellw == null) {
            rect = new TableRect().invoke();
            offset_row = 0;
            offset_col = 0;
            Log.i("TableView", "w: " + rect.getW() + ", h: " + rect.getH());
        }

        painter.reset();
        painter.setTextSize(textsize);
        painter.setColor(textcolor);
        painter.setTextAlign(Paint.Align.RIGHT);

        float x0 = 0;
        float y0 = 0;

        for (int i = 0; i < adapter.getCount(); i++) {

            int r = i / adapter.getColumns();
            int c = i % adapter.getColumns();

            if (c == 0) {
                x0 = 0;
            }
            y0 = 5 + textsize + (cellh + padding) * r;

            float x = x0;
            float y = y0;

            x0 += (cellw[c] + padding);

            if (c > 0) x -= offset_col;
            if (r > 0) y -= offset_row;

            canvas.clipRect(0, 0, view_w, view_h, Region.Op.REPLACE);
            if (c > 0 && x < cellw[0] + padding) {
                canvas.clipRect(cellw[0] + padding, 0, view_w, view_h, Region.Op.INTERSECT);
                //   continue;
            }
            if (r > 0 && y < 5 + textsize + cellh + padding) {
                canvas.clipRect(0, 5 + cellh + padding, view_w, view_h, Region.Op.INTERSECT);
                //   continue;
            }

            //  Log.i("KlimaView", "x: " + x + ", y: " + y);

            String s = (String) adapter.getItem(i);

            if (r % 2 == 1) {
                painter.setColor(background1);
                canvas.drawRect(x, y - cellh - padding / 2, x + cellw[c] + padding, y + padding / 2, painter);
            }
            painter.setColor(textcolor);
            canvas.drawText(s, x + cellw[c], y, painter);

        }

    }


    public void setAdapter(KlimaGridAdapter ad) {

        cellw = null;
        adapter = ad;
    }

    private class TableRect {
        private int h;
        private int w;

        public int getH() {
            return h;
        }

        public int getW() {
            return w;
        }

        public TableRect invoke() {
            painter.setTextSize(textsize);
            cellw = new int[adapter.getColumns()];
            cellh = 0;
            for (int i = 0; i < adapter.getCount(); i++) {
                int r = i / adapter.getColumns();
                int c = i % adapter.getColumns();

                String s = (String) adapter.getItem(i);
                Rect bounds = new Rect();
                painter.getTextBounds(s, 0, s.length(), bounds);
                cellw[c] = Math.max(cellw[c], bounds.width());
                cellh = Math.max(cellh, bounds.height());

            }

            h = 0;
            w = 0;
            for (int j = 0; j < cellw.length; j++) {
                w += (cellw[j] + padding);
            }
            h = adapter.getCount() / adapter.getColumns() * (cellh + padding);
            return this;
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        getParent().requestDisallowInterceptTouchEvent(true);

        if (event.getPointerCount() > 1 || event.getPointerCount() < 1) {
            return true;
        }


        // MotionEvent reports input details from the touch screen
        // and other input controls. In this case, you are only
        // interested in events where the touch position changed.

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPreviousX = x;
                mPreviousY = y;

                break;

            case MotionEvent.ACTION_MOVE:

                float dx = x - mPreviousX;
                float dy = y - mPreviousY;

                mPreviousX = x;
                mPreviousY = y;

                int o_col_0 = offset_col;
                int o_row_0 = offset_row;

                offset_col -= dx;
                offset_row -= dy;

                if (rect == null) {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    return false;
                }
                if (offset_col > rect.getW() - view_w) offset_col = rect.getW() - view_w;
                if (offset_row > rect.getH() - view_h) offset_row = rect.getH() - view_h;

                if (offset_col < 0) offset_col = 0;
                if (offset_row < 0) offset_row = 0;

                // Log.d("TableView", "dx: " + offset_col + ", dy: "+ offset_row);

                if (o_col_0 != offset_col || Math.abs(dy) > Math.abs(dx) || Math.abs(dx) < 10) {
                    invalidate();
                } else {
                    getParent().requestDisallowInterceptTouchEvent(false);
                    // allow ViewPager to take care
                    return false;
                }
        }

        return true;
    }

    public boolean prefersFullpage() {
        return true;
    }


}
