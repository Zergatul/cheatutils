package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.zergatul.cheatutils.render.images.ImageSource;

import java.util.ArrayList;
import java.util.List;

public class AtlasTexture {

    private TextureWrapper texture;
    private List<Line> lines;

    public AtlasTexture() {
        this(64);
    }

    public AtlasTexture(int size) {
        texture = TextureWrapper.empty(size, size);
        lines = new ArrayList<>();
        lines.add(new Line(0, 0, 0));
    }

    public GpuTexture getTexture() {
        return texture.getTexture();
    }

    public GpuTextureView getTextureView() {
        return texture.getTextureView();
    }

    public Item add(ImageSource image) {
        Line last = lines.getLast();
        if (hasSpace(last, image)) {
            return addToLine(last, image);
        } else if (hasSpaceOnNewLine(last, image)) {
            lines.add(last = new Line(last.top + last.height, 0, 0));
            return addToLine(last, image);
        } else {
            resize();
            return add(image);
        }
    }

    public void dispose() {
        texture.dispose();
    }

    private Item addToLine(Line line, ImageSource image) {
        texture.update(image, line.width, line.top);

        float u1 = 1f * line.width / texture.getWidth();
        float v1 = 1f * line.top / texture.getHeight();
        float u2 = 1f * (line.width + image.getWidth()) / texture.getWidth();
        float v2 = 1f * (line.top + image.getHeight()) / texture.getHeight();
        Item item = new Item(line.width, line.top, image.getWidth(), image.getHeight(), u1, v1, u2, v2);

        line.width += image.getWidth();
        line.height = Math.max(line.height, image.getHeight());
        line.items.add(item);

        return item;
    }

    private boolean hasSpace(Line line, ImageSource image) {
        return hasSpace(line, image.getWidth(), image.getHeight());
    }

    private boolean hasSpace(Line line, Item item) {
        return hasSpace(line, item.width, item.height);
    }

    private boolean hasSpace(Line line, int width, int height) {
        return line.top + height <= texture.getHeight() && line.width + width <= texture.getWidth();
    }

    private boolean hasSpaceOnNewLine(Line line, ImageSource image) {
        return hasSpaceOnNewLine(line, image.getWidth(), image.getHeight());
    }

    private boolean hasSpaceOnNewLine(Line line, Item item) {
        return hasSpaceOnNewLine(line, item.width, item.height);
    }

    private boolean hasSpaceOnNewLine(Line line, int width, int height) {
        return line.top + line.height + height <= texture.getHeight() && width <= texture.getWidth();
    }

    private void resize() {
        TextureWrapper oldTexture = texture;
        texture = TextureWrapper.empty(texture.getWidth() * 2, texture.getHeight() * 2);
        List<Line> oldLines = lines;
        lines = new ArrayList<>();
        lines.add(new Line(0, 0, 0));

        for (Line line : oldLines) {
            for (Item item : line.items) {
                Line last = lines.getLast();
                if (hasSpace(last, item)) {
                    copyToLine(oldTexture, texture, last, item);
                } else if (hasSpaceOnNewLine(last, item)) {
                    lines.add(last = new Line(last.top + last.height, 0, 0));
                    copyToLine(oldTexture, texture, last, item);
                } else {
                    throw new IllegalStateException("Not enough space to copy atlas texture items.");
                }
            }
        }
    }

    private void copyToLine(TextureWrapper oldTexture, TextureWrapper newTexture, Line line, Item item) {
        RenderSystem.getDevice().createCommandEncoder().copyTextureToTexture(
                oldTexture.getTexture(),
                newTexture.getTexture(),
                0,
                line.width, line.top,
                item.x, item.y,
                item.width, item.height);

        item.x = line.width;
        item.y = line.top;
        item.u1 = 1f * line.width / texture.getWidth();
        item.v1 = 1f * line.top / texture.getHeight();
        item.u2 = 1f * (line.width + item.width) / texture.getWidth();
        item.v2 = 1f * (line.top + item.height) / texture.getHeight();

        line.width += item.width;
        line.height = Math.max(line.height, item.height);
        line.items.add(item);
    }

    public static class Item {

        private int x, y;
        private final int width, height;
        private float u1, v1, u2, v2;

        private Item(int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.u1 = u1;
            this.v1 = v1;
            this.u2 = u2;
            this.v2 = v2;
        }

        public float getU1() {
            return u1;
        }

        public float getV1() {
            return v1;
        }

        public float getU2() {
            return u2;
        }

        public float getV2() {
            return v2;
        }
    }

    private static class Line {

        public int top;
        public int width;
        public int height;
        public List<Item> items;

        public Line(int top, int width, int height) {
            this.top = top;
            this.width = width;
            this.height = height;
            this.items = new ArrayList<>();
        }
    }
}