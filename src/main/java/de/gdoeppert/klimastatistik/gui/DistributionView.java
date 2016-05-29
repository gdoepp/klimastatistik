package de.gdoeppert.klimastatistik.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import de.gdoeppert.klima.model.Tagesmittel;

/**
 * Created by gd on 17.01.16.
 */
public class DistributionView extends KlimaViewBase implements View.OnClickListener {

    private double tmMittel;

    public DistributionView(Context context, AttributeSet attrs) {

        super(context, attrs);


        for (int j = 0; j < attrs.getAttributeCount(); j++) {

            // Log.d("Distribution", "attr " + attrs.getAttributeName(j) + " = " + attrs.getAttributeValue(j));
            if (attrs.getAttributeName(j).equals("background")) {
                background = attrs.getAttributeIntValue(j, Color.rgb(0x20, 0x20, 0x20));
            } else if (attrs.getAttributeName(j).equals("color")) {
                color = attrs.getAttributeIntValue(j, Color.rgb(255, 0, 255));
            } else if (attrs.getAttributeName(j).equals("axiscolor")) {
                axiscolor = attrs.getAttributeIntValue(j, Color.rgb(255, 255, 255));
            } else if (attrs.getAttributeName(j).equals("textsize")) {
                String val = attrs.getAttributeValue(j);
                textsize = fromDimen(val);
            } else if (attrs.getAttributeName(j).equals("strokewidth")) {
                String val = attrs.getAttributeValue(j);
                strokewidth = fromDimen(val);
            }
        }

        setOnClickListener(this);
    }

    public void setDistribution(Tagesmittel.TvalDistrib[] td) {

        this.tempdist = td;
        invalidate();
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        canvas.drawColor(background);

        if (tempdist == null || tempdist.length == 0) return;

        w = getWidth();
        h = getHeight();

        int n = tempdist.length;
        float nmax = n - 1;

        float xmin = (float) tempdist[0].wert;
        float xmax = (float) tempdist[n - 1].wert;

        float ymax = 0;
        int anz = 0;
        for (int j = 0; j < n; j++) {
            if (ymax < tempdist[j].anz) {
                ymax = tempdist[j].anz;
            }
            anz += tempdist[j].anz;
        }

        int anz6 = 0;
        float f1 = (float) tempdist[0].wert;
        for (int j = 0; j < n; j++) {
            if (j > 0 && anz6 + tempdist[j].anz > anz / 6f) {
                f1 = (float) (tempdist[j - 1].wert + (anz / 6f - tempdist[j - 1].anz) / (tempdist[j].anz - tempdist[j - 1].anz) * (tempdist[j].wert - tempdist[j - 1].wert));
                break;
            }
            anz6 += tempdist[j].anz;
        }
        anz6 = 0;
        float f2 = (float) tempdist[n - 1].wert;
        for (int j = n - 1; j >= 0; j--) {
            if (j < n - 1 && anz6 + tempdist[j].anz > anz / 6f) {
                f2 = (float) (tempdist[j + 1].wert + (anz / 6f - tempdist[j + 1].anz) / (tempdist[j].anz - tempdist[j + 1].anz) * (tempdist[j].wert - tempdist[j + 1].wert));
                break;
            }
            anz6 += tempdist[j].anz;
        }

        float x0 = 4 * strokewidth;
        float dn = (w - 8 * strokewidth) / n;
        float dx = (w - 8 * strokewidth) / (xmax - xmin);
        float y0 = h - 2 * textsize;
        float dy = (h - 2 * textsize) / ymax;

        painter.reset();
        painter.setColor(color);
        painter.setStrokeWidth(strokewidth);
        painter.setTextSize(textsize);

        if (false) {
            float x1 = x0;
            float y1 = y0 - tempdist[0].anz * dy;

            path.reset();
            path.moveTo(x1, y1);
            for (int j = 1; j < n; j++) {
                float x2 = (float) (x0 + (tempdist[j].wert - xmin) * dx);
                float y2 = y0 - tempdist[j].anz * dy;
                path.lineTo(x2, y2);

            }
            painter.setStrokeJoin(Paint.Join.ROUND);
            painter.setStrokeCap(Paint.Cap.ROUND);
            painter.setStyle(Paint.Style.STROKE);
            canvas.drawPath(path, painter);
        }
        {
            float x1 = x0;
            float y1 = y0;

            path.reset();
            path.moveTo(x1, y1);
            for (int j = 0; j < n; j++) {
                float y2 = y0 - tempdist[j].anz * dy;
                path.addRect(x0 + j * dn + 1, y2, x0 + (j + 1) * dn - 1, y0, Path.Direction.CCW);

            }
            painter.setAlpha(60);
            painter.setStyle(Paint.Style.FILL);
            canvas.drawPath(path, painter);
        }

        painter.setStyle(Paint.Style.FILL);
        float xtm = (float) (x0 + (tmMittel - xmin) * dx);

        painter.setAlpha(255);
        painter.setTextAlign(Paint.Align.CENTER);
        canvas.drawText(String.format("%4.1f°C", tmMittel),
                xtm,
                h - textsize / 2, painter);

        canvas.drawLine(xtm, y0, xtm, 0, painter);

        painter.setColor(axiscolor);

        painter.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(String.format("%2.0f°C", xmin), x0, h - textsize / 2, painter);


        painter.setTextAlign(Paint.Align.RIGHT);
        canvas.drawText(String.format("%2.0f°C", xmax), x0 + n * dn, h - textsize / 2, painter);

        canvas.drawLine(x0, y0, x0 + n * dn, y0, painter);

    }

    private int w;
    private int h;
    private Paint painter = new Paint();
    private Path path = new Path();

    private int background = 0xff202020;
    private int color = 0xffff00ff;
    private float textsize = 18f;
    private float strokewidth = 2;
    private int axiscolor = Color.WHITE;

    private Tagesmittel.TvalDistrib[] tempdist = null;

    public void setTmMittel(double tmMittel) {
        this.tmMittel = tmMittel;
    }

    @Override
    public void onClick(View v) {
        StringBuilder sb = new StringBuilder();
        if (tempdist != null) {
            for (Tagesmittel.TvalDistrib td : tempdist) {
                sb.append(String.format("%5.1f°C", td.wert) + ": \t" + String.format("%4d\n", td.anz));
            }
            ((KlimaStatActivity) getContext()).showMessage("Anzahl Tage", sb.toString());
        }
    }
}