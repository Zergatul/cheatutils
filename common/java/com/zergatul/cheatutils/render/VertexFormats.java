package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class VertexFormats {

    public static final VertexFormat LINES = VertexFormat.builder()
            .add("inPointA", VertexFormatElement.POSITION)
            .add("inPointB", VertexFormatElements.LINE_POINT_B)
            .add("inColor", VertexFormatElement.COLOR)
            .add("inT", VertexFormatElements.LINE_T)
            .add("inSide", VertexFormatElements.LINE_SIDE)
            .add("inLineWidth", VertexFormatElements.LINE_WIDTH)
            .build();
}