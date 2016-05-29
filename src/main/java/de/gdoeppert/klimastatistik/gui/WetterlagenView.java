package de.gdoeppert.klimastatistik.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;

import java.util.HashMap;
import java.util.Map;

import de.gdoeppert.klima.model.Tagesmittel;

/**
 * Created by gd on 10.01.16.
 */
public class WetterlagenView extends KlimaViewBase {

    public WetterlagenView(Context context, AttributeSet attrs) {

        super(context, attrs);


        for (int j = 0; j < attrs.getAttributeCount(); j++) {


            //  Log.d("WetterlagenView", "attr: " + attrs.getAttributeName(j) + " : " + attrs.getAttributeValue(j));

            if (attrs.getAttributeName(j).equals("background")) {
                background = attrs.getAttributeIntValue(j, Color.rgb(0x20, 0x20, 0x20));
            } else if (attrs.getAttributeName(j).equals("color_dir")) {
                direction = attrs.getAttributeIntValue(j, Color.rgb(255, 0, 255));
            } else if (attrs.getAttributeName(j).equals("color_hum")) {
                humid = attrs.getAttributeIntValue(j, Color.rgb(255, 0, 255));
            } else if (attrs.getAttributeName(j).equals("color_dry")) {
                dry = attrs.getAttributeIntValue(j, Color.rgb(255, 0, 255));
            } else if (attrs.getAttributeName(j).equals("textsize")) {
                String val = attrs.getAttributeValue(j);
                textsize = fromDimen(val);
            }
        }

    }

    public void setPercentages(Tagesmittel.Wetterlage wl) {
        this.wl = wl;
    }

    @Override
    protected void onDraw(final Canvas canvas) {

        canvas.drawColor(background);

        w = getWidth();
        h = getHeight();
        float r = w;
        if (w > h) {
            r = h;
        }
        float r1 = r * 0.39f;
        float r2 = r * 0.43f;
        float r3 = r * 0.15f;
        float r4 = r * 0.22f;
        float r5 = r * 0.19f;

        r *= 0.5;

        float x = r;
        float y = r;

        painter.reset();
        painter.setColor(Color.WHITE);
        painter.setTextAlign(Paint.Align.CENTER);
        painter.setTextSize(textsize);
        painter.setStyle(Paint.Style.STROKE);

        canvas.drawCircle(x, y, r1, painter);
        canvas.drawCircle(x, y, r3, painter);

        canvas.drawLine(x + r3, y, x + r1, y, painter);
        canvas.drawLine(x, y + r3, x, y + r1, painter);
        canvas.drawLine(x - r3, y, x - r1, y, painter);
        canvas.drawLine(x, y - r3, x, y - r1, painter);

        canvas.drawText("N", x, y - r2 + textsize / 2, painter);
        canvas.drawText("S", x, y + r2 + textsize / 2, painter);
        canvas.drawText("W", x - r2, y + textsize / 2, painter);
        canvas.drawText("O", x + r2, y + textsize / 2, painter);


        if (wl != null) {
            for (Map.Entry e : wl.percentages.entrySet()) {
                String key = (String) e.getKey();
                if (key.length() < 2) continue;

                double rad = r4;

                if (key.equals("XX")) rad = 0;
                else if (key.startsWith("XX")) rad = r4 * 0.4;
                else if (key.length() > 2) rad = r4 * 1.4;

                if (key.length() == 2) {
                    painter.setTextSize(textsize * 1.4f);
                    painter.setColor(direction);
                } else if (key.endsWith("T")) {
                    painter.setTextSize(textsize);
                    painter.setColor(dry);
                } else if (key.endsWith("F")) {
                    painter.setTextSize(textsize);
                    painter.setColor(humid);
                }

                float x1 = (float) (x + rad * Math.sin((Double) angles.get(key)));
                float y1 = (float) (y - rad * Math.cos((Double) angles.get(key)));

                canvas.drawText(wl.format((Double) e.getValue()), x1, y1 + textsize / 2, painter);
            }
        }
    }

    private int w;
    private int h;
    private Paint painter = new Paint();
    static Map<String, Double> angles = new HashMap<String, Double>();

    static {
        angles.put("NW", -45.0 / 180.0 * Math.PI);
        angles.put("NO", 45.0 / 180.0 * Math.PI);
        angles.put("SW", -135.0 / 180.0 * Math.PI);
        angles.put("SO", 135.0 / 180.0 * Math.PI);

        angles.put("NWT", -68.0 / 180.0 * Math.PI);
        angles.put("NOT", 68.0 / 180.0 * Math.PI);
        angles.put("SWT", -158.0 / 180.0 * Math.PI);
        angles.put("SOT", 158.0 / 180.0 * Math.PI);

        angles.put("NWF", -23.0 / 180.0 * Math.PI);
        angles.put("NOF", 23.0 / 180.0 * Math.PI);
        angles.put("SWF", -113.0 / 180.0 * Math.PI);
        angles.put("SOF", 113.0 / 180.0 * Math.PI);

        angles.put("XX", 0.0);
        angles.put("XXT", 180.0 / 180.0 * Math.PI);
        angles.put("XXF", 0.0);


    }

    private int background = 0xff202020;
    private int direction = 0xffff00ff;
    private int humid = 0xff00d0ff;
    private int dry = 0xffffd000;
    private float textsize = 18f;
    private Tagesmittel.Wetterlage wl;

}