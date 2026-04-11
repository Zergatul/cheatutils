package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.vertex.VertexFormat;

public class VertexFormats {

    private static final GpuFormat POSITION_2D_FORMAT = GpuFormat.RG32_FLOAT;
    private static final GpuFormat POSITION_3D_FORMAT = GpuFormat.RGB32_FLOAT;
    private static final GpuFormat COLOR_FORMAT = GpuFormat.RGBA8_UNORM;
    private static final GpuFormat LINE_WIDTH_FORMAT = GpuFormat.R32_FLOAT;
    private static final GpuFormat UV_FORMAT = GpuFormat.RG32_FLOAT;

    public static final VertexFormat LINES_INSTANCED = VertexFormat.builder(1)
            .addAttribute("inPointA", GpuFormat.RGB32_FLOAT)
            .addAttribute("inPointB", GpuFormat.RGB32_FLOAT)
            .addAttribute("inColor", COLOR_FORMAT)
            .addAttribute("inLineWidth", LINE_WIDTH_FORMAT)
            .build();

    public static final VertexFormat CUBE_LINES_INSTANCED = VertexFormat.builder(1)
            .addAttribute("inOrigin", POSITION_3D_FORMAT)
            .addAttribute("inColor", COLOR_FORMAT)
            .addAttribute("inLineWidth", LINE_WIDTH_FORMAT)
            .build();

    public static final VertexFormat BLOCK_OVERLAY_INSTANCED = VertexFormat.builder(1)
            .addAttribute("inOrigin", POSITION_3D_FORMAT)
            .build();

    public static final VertexFormat POSITION_2D_COLOR = VertexFormat.builder(0)
            .addAttribute("InPosition", POSITION_2D_FORMAT)
            .addAttribute("InColor", COLOR_FORMAT)
            .build();

    public static final VertexFormat POSITION_3D_COLOR = VertexFormat.builder(0)
            .addAttribute("InPosition", POSITION_3D_FORMAT)
            .addAttribute("InColor", COLOR_FORMAT)
            .build();

    public static final VertexFormat POSITION_3D_TEXTURE = VertexFormat.builder(0)
            .addAttribute("InPosition", POSITION_3D_FORMAT)
            .addAttribute("InTexCoords", UV_FORMAT)
            .build();

    public static final VertexFormat POSITION_2D_TEXTURE_COLOR = VertexFormat.builder(0)
            .addAttribute("InPosition", POSITION_2D_FORMAT)
            .addAttribute("InTexCoords", UV_FORMAT)
            .addAttribute("InColor", COLOR_FORMAT)
            .build();
}