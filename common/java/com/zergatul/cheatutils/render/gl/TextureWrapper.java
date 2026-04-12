package com.zergatul.cheatutils.render.gl;

import com.mojang.blaze3d.GpuFormat;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.render.gl.images.ImageSource;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class TextureWrapper {

    private final GpuTexture texture;
    private final GpuTextureView view;
    private final int width;
    private final int height;
    private boolean disposed;

    private TextureWrapper(GpuTexture texture, GpuTextureView view, int width, int height) {
        this.texture = texture;
        this.view = view;
        this.width = width;
        this.height = height;
    }

    public static TextureWrapper empty(int width, int height) {
        GpuTexture texture = RenderSystem.getDevice().createTexture(
                () -> ModMain.MODID + ": Font atlas",
                GpuTexture.USAGE_COPY_DST | GpuTexture.USAGE_TEXTURE_BINDING | GpuTexture.USAGE_RENDER_ATTACHMENT,
                GpuFormat.RGBA8_UNORM,
                width, height,
                1, 1);
        GpuTextureView view = RenderSystem.getDevice().createTextureView(texture);
        return new TextureWrapper(texture, view, width, height);
    }

    public GpuTexture getTexture() {
        return texture;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public void update(ImageSource image, int x, int y) {
        /*glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
        glPixelStorei(GL_UNPACK_SKIP_ROWS, 0);
        glPixelStorei(GL_UNPACK_SKIP_PIXELS, 0);

        GlStateManager._activeTexture(GL_TEXTURE0);
        GlStateManager._bindTexture(id);
        glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, image.getWidth(), image.getHeight(), GL_RGBA, GL_UNSIGNED_BYTE, image.getARGB());*/
//        try (RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
//                () -> ModMain.MODID + ": Font atlas update",
//                view,
//                OptionalInt.empty()
//        )) {
//
//        }

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
                        NativeImage.Format.RGBA,
                        0, 0,
                        x, y,
                        image.getWidth(),
                        image.getHeight());
    }

    public void dispose() {
        if (!disposed) {
            view.close();
            texture.close();
            disposed = true;
        }
    }
}