package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.vertex.VertexFormatElement;

public class VertexFormatElements {

    public static VertexFormatElement GRADIENT = VertexFormatElement.register(findFreeId(), 0, VertexFormatElement.Type.FLOAT, false, 1);
    public static VertexFormatElement COLOR4 = VertexFormatElement.register(findFreeId(), 0, VertexFormatElement.Type.FLOAT, false, 4);

    private static int findFreeId() {
        for (int i = 0; i < 32; i++) {
            if (VertexFormatElement.byId(i) == null) {
                return i;
            }
        }
        throw new IllegalStateException("No free VertexFormatElement slots.");
    }
}