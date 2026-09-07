package com.zergatul.cheatutils.render;

import net.minecraft.client.renderer.texture.AbstractTexture;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;

public final class TextureUtils {

    public static byte[] toPng(AbstractTexture texture) {
        int prevTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        try {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getGlTextureId());
            int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
            int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
            int format = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT);
            if (format == GL11.GL_RGBA) {
                IntBuffer buffer = BufferUtils.createIntBuffer(width * height);
                GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
                BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int value = buffer.get(y * width + x);
                        image.setRGB(x, y, swapBytes02(value));
                    }
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                try {
                    ImageIO.write(image, "png", stream);
                } catch (IOException e) {
                    return null;
                }

                return stream.toByteArray();
            } else {
                return null;
            }
        } finally {
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, prevTexture);
        }
    }

    private static int swapBytes02(int value) {
        int byte0 = value & 0xFF;
        int byte2 = (value >> 16) & 0xFF;
        value &= ~((0xFF << 16) | 0xFF);
        value |= (byte0 << 16) | (byte2);
        return value;
    }
}