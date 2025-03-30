package com.zergatul.cheatutils.render.gl;

import com.mojang.blaze3d.opengl.GlTexture;
import net.minecraft.client.renderer.texture.AbstractTexture;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class TextureUtils {

    public static void saveAsPng(int id, String path) throws IOException {
        File file = new File(path);
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(toPng(id));
        stream.close();
    }

    public static byte[] toPng(AbstractTexture texture) throws IOException {
        return toPng(((GlTexture) texture.getTexture()).glId());
    }

    public static byte[] toPng(int id) throws IOException {
        GlStateTracker.save(GlStateTracker.TEXTURE);

        GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);
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

        GlStateTracker.restore(GlStateTracker.TEXTURE);

        return result;
    }
}