package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.platform.NativeImage;
import org.lwjgl.stb.STBImageWrite;
import org.lwjgl.system.MemoryUtil;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public class ImageUtils {

    public static byte[] toPng(NativeImage image) {
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
}