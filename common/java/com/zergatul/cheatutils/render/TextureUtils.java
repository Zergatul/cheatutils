package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.backend.opengl.GlStateManager;
import org.lwjgl.opengl.GL30;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.concurrent.CompletableFuture;

public class TextureUtils {

    public static void saveAsPng(int id, String path) throws IOException {
        File file = new File(path);
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(glTextureToPng(id));
        stream.close();
    }

    // logic copied from Screenshot.takeScreenshot
    // current bug in Mojang code cause OpenGL error being logged in snapshot 6, verify if this is fixed later
    public static CompletableFuture<byte[]> toPng(GpuTexture texture) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        int width = texture.getWidth(0);
        int height = texture.getHeight(0);
        int size = width * height * texture.getFormat().blockSize();
        GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Texture dump buffer", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, size);
        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(texture, buffer, 0L, () -> {
            try (GpuBufferSlice.MappedView read = buffer.map(true, false);) {
                try (NativeImage image = new NativeImage(width, height, true)) {
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            image.setPixelABGR(x, y, read.data().getInt((x + y * width) * texture.getFormat().blockSize()));
                        }
                    }

                    future.complete(ImageUtils.toPng(image));
                }
            }

            buffer.close();
        }, 0);

        return future;
    }

    public static byte[] glTextureToPng(int id) throws IOException {
        GlStateManager._bindTexture(id);

        pushState();
        try {
            int width = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_WIDTH);
            int height = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_HEIGHT);
            int format = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_INTERNAL_FORMAT);

            byte[] result;
            if (format == GL30.GL_RGBA || format == GL30.GL_RGBA8) {
                ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
                try {
                    GL30.glGetTexImage(GL30.GL_TEXTURE_2D, 0, GL30.GL_BGRA, GL30.GL_UNSIGNED_BYTE, buffer);
                    IntBuffer intBuf = buffer.asIntBuffer();
                    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            image.setRGB(x, y, intBuf.get(y * width + x));
                        }
                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", stream);
                    result = stream.toByteArray();
                } finally {
                    MemoryUtil.memFree(buffer);
                }
            } else {
                throw new IOException("Pixel format is not supported.");
            }

            return result;
        } finally {
            popState();
        }
    }

    private static int prevPackBuffer;
    private static int prevPackAlignment;

    private static void pushState() {
        prevPackBuffer = GL30.glGetInteger(GL30.GL_PIXEL_PACK_BUFFER_BINDING);
        prevPackAlignment = GL30.glGetInteger(GL30.GL_PACK_ALIGNMENT);

        GL30.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, 0);
        GL30.glPixelStorei(GL30.GL_PACK_ALIGNMENT, 1);
    }

    private static void popState() {
        GL30.glPixelStorei(GL30.GL_PACK_ALIGNMENT, prevPackAlignment);
        GL30.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, prevPackBuffer);
    }
}