package com.zergatul.cheatutils.schematics;

import com.mojang.blaze3d.vertex.VertexConsumer;
import org.jetbrains.annotations.NotNull;

public class ShadedVertexConsumerWrapper implements VertexConsumer {

    private final VertexConsumer inner;
    private final float shadeR;
    private final float shadeG;
    private final float shadeB;
    private final float shadeA;

    public ShadedVertexConsumerWrapper(VertexConsumer inner) {
        this(inner, 0.5f, 0.8f, 1.0f, 0.6f);
    }

    public ShadedVertexConsumerWrapper(VertexConsumer inner, float r, float g, float b, float a) {
        this.inner = inner;
        this.shadeR = r;
        this.shadeG = g;
        this.shadeB = b;
        this.shadeA = a;
    }

    @Override
    public @NotNull VertexConsumer addVertex(float x, float y, float z) {
        inner.addVertex(x, y, z);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setColor(int c) {
        return this.setColor((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, c >> 24);
    }

    @Override
    public @NotNull VertexConsumer setColor(int r, int g, int b, int a) {
        inner.setColor(Math.round(shadeR * r), Math.round(shadeG * g), Math.round(shadeB * g), Math.round(shadeA * a));
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv(float u, float v) {
        inner.setUv(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv1(int u, int v) {
        inner.setUv1(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setUv2(int u, int v) {
        inner.setUv2(u, v);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setNormal(float x, float y, float z) {
        inner.setNormal(x, y, z);
        return this;
    }

    @Override
    public @NotNull VertexConsumer setLineWidth(float f) {
        return this;
    }
}