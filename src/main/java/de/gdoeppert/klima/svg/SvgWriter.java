package de.gdoeppert.klima.svg;

import java.io.IOException;

import de.gdoeppert.klima.guibase.IGraphicsWriterBase;
import de.gdoeppert.klima.guibase.IPath;

public abstract class SvgWriter implements IGraphicsWriterBase {

    abstract public void startElement(String string) throws IOException;

    abstract public void writeAttribute(String string, double x, String string2)
            throws IOException;

    abstract public void endElement(String string) throws IOException;

    abstract public void writeAttribute(String string, String path,
                                        String string2) throws IOException;

    abstract public void write(String content) throws IOException;

    public void writeRect(int x1, int y1, int x2, int y2, String col)
            throws IOException {
        startElement("rect");
        writeAttribute("x", x1, "x");
        writeAttribute("y", y1, "y");
        writeAttribute("width", x2, "width");
        writeAttribute("height", y2, "height");
        writeAttribute("style", "stroke:none;fill:" + col, "style");
        endElement("rect");
    }

    public void writeText(double x, double y, String content, String col,
                          int fontsize) throws IOException {
        startElement("text");
        writeAttribute("x", x, "x");
        writeAttribute("y", y, "y");
        writeAttribute("font-size", fontsize + "pt", "font-size");
        writeAttribute("style", "fill:" + col + ";stroke:none", "style");
        write(content);
        endElement("text");
    }

    public void finish() throws IOException {
        endElement("g");
        endElement("svg");
    }

    public void writeFilledPath(IPath path, String col, double opa, int lw)
            throws IOException {
        startElement("path");
        writeAttribute("d", ((Path) path).getPath(), "path");
        writeAttribute("style", "fill:" + col + ";fill-opacity:" + opa
                + ";stroke:" + col + ";stroke-opacity:" + opa
                + ";stroke-width=" + lw + "px", "style");
        endElement("path");
    }

    public void writePath(IPath path, String col, int lw) throws IOException {
        startElement("path");
        writeAttribute("d", ((Path) path).getPath(), "path");
        writeAttribute("style", "fill:none;stroke:" + col + ";stroke-width="
                + lw + "px", "style");
        endElement("path");
    }

    public void start() throws IOException {
        startElement("svg");
        writeAttribute("version", "1.1", "version");
        writeAttribute("xmlns", "http://www.w3.org/2000/svg", "namespace");
        writeAttribute("viewBox", "0 0 1600 900", "viewBox");
        startElement("g");
        writeAttribute("id", "db-symbol", "id");
        writeAttribute("style", "stroke:#000000;fill:white", "style");
    }

    @Override
    public IPath createPath() {
        return new Path();
    }

}
