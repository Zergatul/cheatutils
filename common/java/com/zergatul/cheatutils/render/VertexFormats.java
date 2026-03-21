package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class VertexFormats {

    public static final VertexFormat POSITION_GRADIENT = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Gradient", VertexFormatElements.GRADIENT)
            .build();

    public static final VertexFormat POSITION_COLOR_GRADIENT_LINE_WIDTH = VertexFormat.builder()
            .add("Position", VertexFormatElement.POSITION)
            .add("Color", VertexFormatElements.COLOR4)
            .add("Gradient", VertexFormatElements.GRADIENT)
            .add("LineWidth", VertexFormatElement.LINE_WIDTH)
            .build();
}