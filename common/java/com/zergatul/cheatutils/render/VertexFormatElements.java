package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class VertexFormatElements {

    public static final VertexFormatElement POSITION_2D = VertexFormatElement.register(findFreeId(), 0, GpuFormat.RG32_FLOAT);
    public static final VertexFormatElement LINE_POINT_B = VertexFormatElement.register(findFreeId(), 0, GpuFormat.RGB32_FLOAT);
    public static final VertexFormatElement LINE_PARAMETERS = VertexFormatElement.register(findFreeId(), 0, GpuFormat.R32_UINT);

    private static int findFreeId() {
        for (int i = 0; i < 32; i++) {
            if (VertexFormatElement.byId(i) == null) {
                return i;
            }
        }
        throw new IllegalStateException("No free VertexFormatElement slots.");
    }
}