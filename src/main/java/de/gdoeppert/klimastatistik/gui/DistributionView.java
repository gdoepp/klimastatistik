package de.gdoeppert.klimastatistik.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

import java.util.Vector;

import de.gdoeppert.klima.model.Tagesmittel;

/**
 * Created by gd on 17.01.16.
 */
public class DistributionView extends KlimaViewBase implements View.OnClickListener {

    private Vector<Double> tmMittelV;
    private double tmMittel;
    private double tmMittelDev;

    public DistributionView(Context context, AttributeSet attrs) {

        super(context, attrs);


        for (int j = 0; j < attrs.getAttributeCount(); j++) {

            // Log.d("Distribution", "attr " + attrs.getAttributeName(j) + " = " + attrs.getAttributeValue(j));
            if (attrs.getAttributeName(j).equals("background")) {
                background = attrs.getAttributeIntValue(j, Color.rgb(0x20, 0x20, 0x20));
            } else if (attrs.getAttributeName(j).equals("color")) {
                color = attrs.getAttributeIntValue(j, Color.rgb(255, 0, 255));
            } else if (attrs.getAttributeName(j).equals("rangecolor")) {
                rangecolor = attrs.getAttributeIntValue(j, Color.rgb(0, 255, 255));
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

        float ymin = (float) tempdist[0].wert;
        float ymax = (float) tempdist[n - 1].wert;

        float xmax = 0;
        float xmin = 0;

        for (int j = 0; j < n; j++) {
            if (xmax < tempdist[j].anz) {
                xmax = tempdist[j].anz;
            }
        }

        painter.reset();
        painter.setColor(color);
        painter.setStrokeWidth(strokewidth);
        painter.setTextSize(textsize);

        Rect bounds = new Rect();
        painter.getTextBounds("28.8°CM", 0, 7, bounds);
        float x0 = bounds.width();
        float x1 = w;

        if (tmMittelV != null) {
            x1 -= (w - x0) / 5f;
        }

        float dn = (h - 8 * strokewidth) / n;
        float dx = (x1 - x0) / (xmax - xmin);
        float y0 = (h - 4 * strokewidth);
        float dy = (h - 8 * strokewidth) / (ymax - ymin);


        path.reset();
        for (int j = 0; j < n; j++) {
            float x2 = x0 + tempdist[j].anz * dx;
            path.addRect(x0, y0 - (j + 1) * dn + 1, x2, y0 - j * dn - 1, Path.Direction.CCW);
            // Log.d("distrib","rect "+x0 + "," + (y0-j*dn+1) + "," + x2 +","+ (y0 - (j + 1) * dn - 1));

        }
        painter.setAlpha(60);
        painter.setStyle(Paint.Style.FILL);
        canvas.drawPath(path, painter);

        painter.setTextAlign(Paint.Align.LEFT);

        if (tmMittelV != null && tmMittelV.size() > 1) {

            painter.setColor(rangecolor);
            painter.setAlpha(40);
            float ytm1 = (float) (y0 - (tmMittel + tmMittelDev - ymin) * dy);
            float ytm2 = (float) (y0 - (tmMittel - tmMittelDev - ymin) * dy);
            canvas.drawRect(x1, ytm1, w, ytm2, painter);

            painter.setColor(color);
            painter.setAlpha(255);
            painter.setStyle(Paint.Style.STROKE);

            float dx1 = (w - x1) / 3f;
            for (int dec = 0; dec < tmMittelV.size(); dec++) {
                float ytm = (float) (y0 - (tmMittelV.get(dec) - ymin) * dy);
                canvas.drawLine(x1 + dec * dx1, ytm, x1 + (dec + 1) * dx1, ytm, painter);
            }

        }

        painter.setAlpha(255);
        painter.setStyle(Paint.Style.FILL);
        float ytm = (float) (y0 - (tmMittel - ymin) * dy);
        canvas.drawLine(x0, ytm, x0 + xmax * dx, ytm, painter);
        canvas.drawText(String.format("%4.1f°C", tmMittel),
                1,
                ytm + textsize / 2,
                painter);

        painter.setColor(axiscolor);

        painter.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(String.format("%2.0f°C", ymin), 1, h - textsize / 2, painter);
        canvas.drawLine(x0, y0, x0 - 10, y0, painter);

        painter.setTextAlign(Paint.Align.LEFT);
        canvas.drawText(String.format("%2.0f°C", ymax), 1, textsize, painter);
        canvas.drawLine(x0 - 10, y0 - n * dn, x0, y0 - n * dn, painter);

        painter.setStyle(Paint.Style.STROKE);
        canvas.drawRect(x0, y0 - (ymax - ymin) * dy, x1, y0, painter);

        if (tmMittelV != null) {
            canvas.drawRect(x1, y0 - (ymax - ymin) * dy, w, y0, painter);
        }
    }

    private int w;
    private int h;
    private Paint painter = new Paint();
    private Path path = new Path();

    private int background = 0xff202020;
    private int color = 0xffff00ff;
    private int rangecolor = 0xffff00ff;
    private float textsize = 18f;
    private float strokewidth = 2;
    private int axiscolor = Color.WHITE;

    private Tagesmittel.TvalDistrib[] tempdist = null;

    public void setTmMittelDec(Vector<Double> tmMittelDec) {
        this.tmMittelV = tmMittelDec;
    }

    public void setTmMittel(double tmMittel) {
        this.tmMittel = tmMittel;
    }

    @Override
    public void onClick(View v) {
        StringBuilder sb = new StringBuilder();
        if (tempdist != null) {
            int anz = 0;
            for (Tagesmittel.TvalDistrib td : tempdist) {
                anz += td.anz;
            }
            for (Tagesmittel.TvalDistrib td : tempdist) {
                sb.insert(0,String.format("%5.1f°C", td.wert) + ": \t" + String.format("%4d", td.anz) + "\t (" + String.format("%4.1f%%", 100.0 * td.anz / anz) + ")\n");
            }
            sb.insert(0,"Anzahl Tage mit:\n");
        }
        if (tmMittelV != null) {
            sb.append("\nMittel in Dekade:\n");
            for (int j = 0; j < tmMittelV.size(); j++) {
                sb.append((j + 1) + ": " + String.format("%5.1f°C\n", tmMittelV.get(j)));
            }
            sb.append("\nStandardabweichung: " + String.format("%4.1f°C\n", tmMittelDev));
            sb.append("\n1σ Intervall: " + String.format("[ %4.1f , %4.1f ]°C\n", tmMittel - tmMittelDev, tmMittel + tmMittelDev));
        }

        ((KlimaStatActivity) getContext()).showMessage("Verteilung Tagesmittel", sb.toString());
    }

    public void setTmMittelDev(double tmMittelDev) {
        this.tmMittelDev = tmMittelDev;
    }
}