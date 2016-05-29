package de.gdoeppert.klimastatistik.gui;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.gdoeppert.klima.guibase.IGraphicsWriterBase;
import de.gdoeppert.klima.guibase.IPath;

/**
 * Created by gd on 02.01.16.
 */
class AndroidGraphicsWriter implements IGraphicsWriterBase {

    final static Map<String, Integer> colors = new HashMap<String, Integer>();
    final private Paint painter;
    private final Canvas canvas;
    private float textsize;

    public AndroidGraphicsWriter(float txtsz, Paint painter, Canvas canvas) {
        this.painter = painter;
        this.canvas = canvas;
        textsize = txtsz;
    }

    @Override
    public void writeRect(int x1, int y1, int x2, int y2, String col) throws IOException {
        painter.setColor(colors.get(col));
        painter.setStyle(Paint.Style.FILL);
        canvas.drawRect(x1, y1, x2, y2, painter);
    }

    @Override
    public void writeText(double x, double y, String content, String col, int fontsize) throws IOException {
        painter.setColor(colors.get(col));
        painter.setStyle(Paint.Style.FILL);
        painter.setStrokeWidth(1);
        painter.setTextSize(textsize * fontsize / 10f);
        canvas.drawText(content, (float) x, (float) y, painter);
    }

    @Override
    public Rect calcRectText(String content, int fontsize) {
        painter.setStyle(Paint.Style.FILL);
        painter.setStrokeWidth(1);
        painter.setTextSize(textsize * fontsize / 10f);
        android.graphics.Rect bounds = new android.graphics.Rect();
        painter.getTextBounds(content, 0, content.length(), bounds);
        AndroidGraphicsWriter.Rect r = new AndroidGraphicsWriter.Rect();
        r.w = bounds.width();
        r.h = bounds.height();
        return r;
    }

    @Override
    public void finish() throws IOException {

    }

    @Override
    public void writeFilledPath(IPath path, String col, double opa, int lw) throws IOException {
        painter.setColor(colors.get(col));
        painter.setStrokeWidth(lw);
        AndroidPath p = (AndroidPath) path;
        painter.setStyle(Paint.Style.FILL);
        painter.setAlpha((int) (opa * 255));
        canvas.drawPath(p.getPath(), painter);
    }

    @Override
    public void writePath(IPath path, String col, int lw) throws IOException {
        painter.setColor(colors.get(col));
        painter.setStrokeWidth(lw);
        painter.setStyle(Paint.Style.STROKE);
        AndroidPath p = (AndroidPath) path;
        canvas.drawPath(p.getPath(), painter);
    }

    @Override
    public void start() throws IOException {

    }

    @Override
    public IPath createPath() {
        return new AndroidPath();
    }


    static {
        colors.put("green", Color.rgb(0, 150, 0));
        colors.put("blue", Color.BLUE);
        colors.put("red", Color.RED);
        colors.put("violet", Color.rgb(100, 0, 110));
        colors.put("orange", Color.rgb(180, 140, 0));
        colors.put("cyan", Color.rgb(0, 150, 150));
        colors.put("yellow", Color.YELLOW);
        colors.put("magenta", Color.MAGENTA);
        colors.put("brown", Color.rgb(100, 70, 0));
        colors.put("pink", Color.rgb(255, 90, 100));
        colors.put("black", Color.BLACK);
        colors.put("#0", Color.BLACK);
        colors.put("white", Color.rgb(200, 200, 200));
    }


}
