package com.zergatul.cheatutils.schematics;

import com.mojang.blaze3d.vertex.VertexConsumer;

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
    public VertexConsumer vertex(double x, double y, double z) {
        inner.vertex(x, y, z);
        return this;
    }

    @Override
    public VertexConsumer color(int r, int g, int b, int a) {
        inner.color(
                Math.round(shadeR * r),
                Math.round(shadeG * g),
                Math.round(shadeB * b),
                Math.round(shadeA * a));
        return this;
    }

    @Override
    public VertexConsumer uv(float u, float v) {
        inner.uv(u, v);
        return this;
    }

    @Override
    public VertexConsumer overlayCoords(int u, int v) {
        inner.overlayCoords(u, v);
        return this;
    }

    @Override
    public VertexConsumer uv2(int u, int v) {
        inner.uv2(u, v);
        return this;
    }

    @Override
    public VertexConsumer normal(float x, float y, float z) {
        inner.normal(x, y, z);
        return this;
    }

    @Override
    public void endVertex() {
        inner.endVertex();
    }

    @Override
    public void defaultColor(int r, int g, int b, int a) {
        inner.defaultColor(r, g, b, a);
    }

    @Override
    public void unsetDefaultColor() {
        inner.unsetDefaultColor();
    }
}