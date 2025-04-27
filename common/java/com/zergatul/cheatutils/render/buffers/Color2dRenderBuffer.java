package com.zergatul.cheatutils.render.buffers;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;

public class Color2dRenderBuffer {

    private final FloatList list = new FloatArrayList(32);

    public FloatList getList() {
        return list;
    }

    public void clear() {
        list.clear();
    }

    public void rect(float x, float y, float w, float h, int color) {
        float a = (color >> 24 & 255) / 255.0F;
        float r = (color >> 16 & 255) / 255.0F;
        float g = (color >> 8 & 255) / 255.0F;
        float b = (color & 255) / 255.0F;
        rect(x, y, w, h, r, g, b, a);
    }

    public void rect(float x, float y, float w, float h, float r, float g, float b, float a) {
        quad(
                x, y,
                x, y + h,
                x + w, y + h,
                x + w, y,
                r, g, b, a);
    }

    private void quad(
            float x1, float y1,
            float x2, float y2,
            float x3, float y3,
            float x4, float y4,
            float r, float g, float b, float a
    ) {
        vertex(x1, y1, r, g, b, a);
        vertex(x2, y2, r, g, b, a);
        vertex(x3, y3, r, g, b, a);

        vertex(x1, y1, r, g, b, a);
        vertex(x4, y4, r, g, b, a);
        vertex(x3, y3, r, g, b, a);
    }

    private void vertex(float x, float y, float r, float g, float b, float a) {
        list.add(x);
        list.add(y);
        list.add(r);
        list.add(g);
        list.add(b);
        list.add(a);
    }
}