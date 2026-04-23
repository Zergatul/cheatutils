package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;

public class VertexFormats {

    public static final VertexFormat LINES_INSTANCED = VertexFormat.builder()
            .add("inPointA", VertexFormatElements.LINE_POINT_A)
            .add("inPointB", VertexFormatElements.LINE_POINT_B)
            .add("inColor", VertexFormatElement.COLOR)
            .add("inLineWidth", VertexFormatElement.LINE_WIDTH)
            .build();

    public static final VertexFormat CUBE_LINES_INSTANCED = VertexFormat.builder()
            .add("inOrigin", VertexFormatElement.POSITION)
            .add("inColor", VertexFormatElement.COLOR)
            .add("inLineWidth", VertexFormatElement.LINE_WIDTH)
            .build();

    public static final VertexFormat POSITION_2D_COLOR = VertexFormat.builder()
            .add("InPosition", VertexFormatElements.POSITION_2D)
            .add("InColor", VertexFormatElement.COLOR)
            .build();

    public static final VertexFormat POSITION_3D_COLOR = VertexFormat.builder()
            .add("InPosition", VertexFormatElement.POSITION)
            .add("InColor", VertexFormatElement.COLOR)
            .build();

    public static final VertexFormat POSITION_2D_TEXTURE_COLOR = VertexFormat.builder()
            .add("InPosition", VertexFormatElements.POSITION_2D)
            .add("InTexCoords", VertexFormatElement.UV)
            .add("InColor", VertexFormatElement.COLOR)
            .build();

    public static boolean isInstanced(VertexFormat format) {
        return format == LINES_INSTANCED || format == CUBE_LINES_INSTANCED;
    }
}