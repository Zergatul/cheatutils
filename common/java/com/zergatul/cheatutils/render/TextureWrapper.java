package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.render.images.ImageSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TextureWrapper {

    private final GpuTexture texture;
    private final GpuTextureView textureView;
    private final int width;
    private final int height;
    private boolean disposed;

    private TextureWrapper(GpuTexture texture, GpuTextureView textureView, int width, int height) {
        this.texture = texture;
        this.textureView = textureView;
        this.width = width;
        this.height = height;
    }

    public static TextureWrapper empty(int width, int height) {
        GpuTexture texture = RenderSystem.getDevice().createTexture(
                () -> Constants.MOD_ID + ": Font atlas",
                GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_COPY_SRC | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
                GpuFormat.RGBA8_UNORM,
                width, height,
                1, 1);
        GpuTextureView view = RenderSystem.getDevice().createTextureView(texture);
        return new TextureWrapper(texture, view, width, height);
    }

    public GpuTexture getTexture() {
        return texture;
    }

    public GpuTextureView getTextureView() {
        return textureView;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void update(ImageSource image, int x, int y) {
        int[] data = image.getARGB();
        ByteBuffer buffer = ByteBuffer.allocateDirect(data.length * 4);
        buffer.order(ByteOrder.nativeOrder());
        buffer.asIntBuffer().put(data);
        buffer.rewind();

        RenderSystem.getDevice()
                .createCommandEncoder()
                .writeToTexture(
                        texture,
                        buffer,
                        0,
                        0,
                        x, y,
                        image.getWidth(),
                        image.getHeight());
    }

    public void dispose() {
        if (!disposed) {
            textureView.close();
            texture.close();
            disposed = true;
        }
    }
}