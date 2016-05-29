package de.gdoeppert.klima.guibase;

import java.io.IOException;

public interface IGraphicsWriterBase {

    class Rect {
        public float w;
        public float h;
    }


    void writeRect(int x1, int y1, int x2, int y2, String color)
            throws IOException;

    void writeText(double x, double y, String content, String color, int fontsize)
            throws IOException;

    Rect calcRectText(String content, int fontsize);

    void finish() throws IOException;

    void writeFilledPath(IPath path, String color, double opaque, int linewidth)
            throws IOException;

    void writePath(IPath path, String color, int linewidth) throws IOException;

    void start() throws IOException;

    IPath createPath();

}
