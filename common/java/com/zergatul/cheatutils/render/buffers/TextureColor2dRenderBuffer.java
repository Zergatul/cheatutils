package com.zergatul.cheatutils.render.buffers;

import com.zergatul.cheatutils.render.gl.AtlasTexture;
import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.floats.FloatList;

public class TextureColor2dRenderBuffer {

    private final FloatList list = new FloatArrayList(32);

    public FloatList getList() {
        return list;
    }

    public void clear() {
        list.clear();
    }

    public void rect(
            float x, float y, float w, float h,
            AtlasTexture.Item item,
            float r, float g, float b, float a
    ) {
        quad(
                x, y, item.getU1(), item.getV1(),
                x, y + h, item.getU1(), item.getV2(),
                x + w, y + h, item.getU2(), item.getV2(),
                x + w, y, item.getU2(), item.getV1(),
                r, g, b, a);
    }

    public void quad(
            float x1, float y1, float u1, float v1,
            float x2, float y2, float u2, float v2,
            float x3, float y3, float u3, float v3,
            float x4, float y4, float u4, float v4,
            float r, float g, float b, float a
    ) {
        vertex(x1, y1, u1, v1, r, g, b, a);
        vertex(x2, y2, u2, v2, r, g, b, a);
        vertex(x3, y3, u3, v3, r, g, b, a);

        vertex(x1, y1, u1, v1, r, g, b, a);
        vertex(x4, y4, u4, v4, r, g, b, a);
        vertex(x3, y3, u3, v3, r, g, b, a);
    }

    private void vertex(
            float x, float y, float u, float v,
            float r, float g, float b, float a
    ) {
        list.add(x);
        list.add(y);
        list.add(u);
        list.add(v);
        list.add(r);
        list.add(g);
        list.add(b);
        list.add(a);
    }
}