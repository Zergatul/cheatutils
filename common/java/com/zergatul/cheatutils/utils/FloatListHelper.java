package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.render.AtlasTexture;
import it.unimi.dsi.fastutil.floats.FloatList;

public class FloatListHelper {

    public static void rect(
            FloatList list,
            float x, float y, float width, float height,
            AtlasTexture.Item item,
            float red, float green, float blue, float alpha) {
        quad(
                list,
                x, y, item.getU1(), item.getV1(),
                x, y + height, item.getU1(), item.getV2(),
                x + width, y + height, item.getU2(), item.getV2(),
                x + width, y, item.getU2(), item.getV1(),
                red, green, blue, alpha);
    }

    public static void quad(
            FloatList list,
            float x1, float y1, float u1, float v1,
            float x2, float y2, float u2, float v2,
            float x3, float y3, float u3, float v3,
            float x4, float y4, float u4, float v4,
            float r, float g, float b, float a
    ) {
        list.add(x1);
        list.add(y1);
        list.add(u1);
        list.add(v1);
        list.add(r);
        list.add(g);
        list.add(b);
        list.add(a);

        list.add(x2);
        list.add(y2);
        list.add(u2);
        list.add(v2);
        list.add(r);
        list.add(g);
        list.add(b);
        list.add(a);

        list.add(x3);
        list.add(y3);
        list.add(u3);
        list.add(v3);
        list.add(r);
        list.add(g);
        list.add(b);
        list.add(a);

        list.add(x1);
        list.add(y1);
        list.add(u1);
        list.add(v1);
        list.add(r);
        list.add(g);
        list.add(b);
        list.add(a);

        list.add(x4);
        list.add(y4);
        list.add(u4);
        list.add(v4);
        list.add(r);
        list.add(g);
        list.add(b);
        list.add(a);

        list.add(x3);
        list.add(y3);
        list.add(u3);
        list.add(v3);
        list.add(r);
        list.add(g);
        list.add(b);
        list.add(a);
    }
}