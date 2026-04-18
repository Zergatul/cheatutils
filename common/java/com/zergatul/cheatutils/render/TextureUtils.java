package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
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
    public static CompletableFuture<byte[]> toPng(GpuTexture texture) {
        CompletableFuture<byte[]> future = new CompletableFuture<>();

        int width = texture.getWidth(0);
        int height = texture.getHeight(0);
        int size = width * height * texture.getFormat().pixelSize();
        GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Texture dump buffer", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, size);
        CommandEncoder commandEncoder = RenderSystem.getDevice().createCommandEncoder();
        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(texture, buffer, 0L, () -> {
            try (GpuBuffer.MappedView read = commandEncoder.mapBuffer(buffer, true, false)) {
                try (NativeImage image = new NativeImage(width, height, true)) {
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            image.setPixelABGR(x, y, read.data().getInt((x + y * width) * texture.getFormat().pixelSize()));
                        }
                    }

                    future.complete(toPng(image));
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

    private static byte[] toPng(NativeImage image) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        STBImageWrite.stbi_write_png_to_func(
                (long context, long data, int size) -> {
                    ByteBuffer buffer = MemoryUtil.memByteBuffer(data, size);
                    byte[] chunk = new byte[size];
                    buffer.get(chunk);
                    stream.write(chunk, 0, size);
                },
                0,
                image.getWidth(), image.getHeight(), image.format().components(),
                MemoryUtil.memByteBuffer(image.getPointer(), image.getWidth() * image.getHeight() * image.format().components()),
                0);
        return stream.toByteArray();
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