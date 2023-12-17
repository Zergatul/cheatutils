package com.zergatul.cheatutils.webui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.utils.UnsafeUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import sun.misc.Unsafe;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

public class TexturesHandler implements HttpHandler {

    private final MemoryUtil.MemoryAllocator ALLOCATOR = MemoryUtil.getAllocator(false);
    private final Unsafe UNSAFE = UnsafeUtil.get();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().substring("/textures/".length());
        ResourceLocation location = new ResourceLocation(id);
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture texture = textureManager.getTexture(location);

        CompletableFuture<byte[]> pngFuture = new CompletableFuture<>();
        RenderSystem.recordRenderCall(() -> {
            byte[] result = null;
            try {
                int prevTexture = GL30.glGetInteger(GL30.GL_TEXTURE_BINDING_2D);

                GL30.glBindTexture(GL30.GL_TEXTURE_2D, texture.getId());
                int width = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_WIDTH);
                int height = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_HEIGHT);
                int format = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_INTERNAL_FORMAT);
                if (format == GL30.GL_RGBA) {
                    long address = ALLOCATOR.malloc(width * height * 4);
                    try {
                        GL30.glGetTexImage(GL30.GL_TEXTURE_2D, 0, GL30.GL_RGBA, GL30.GL_UNSIGNED_BYTE, address);
                        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                int value = UNSAFE.getInt(address + (y * width + x) * 4);
                                image.setRGB(x, y, swapBytes02(value));
                            }
                        }

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        try {
                            ImageIO.write(image, "png", stream);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        result = stream.toByteArray();
                    } finally {
                        ALLOCATOR.free(address);
                    }
                }

                GL30.glBindTexture(GL30.GL_TEXTURE_2D, prevTexture);
            } finally {
                pngFuture.complete(result);
            }
        });

        try {
            byte[] png = pngFuture.get();
            if (png == null) {
                exchange.sendResponseHeaders(500, 0);
            } else {
                HttpHelper.setContentType(exchange, "1.png");
                HttpHelper.setCacheControl(exchange);
                exchange.sendResponseHeaders(200, png.length);
                OutputStream os = exchange.getResponseBody();
                os.write(png);
                os.close();
            }
        } catch (Throwable e) {
            e.printStackTrace();
            exchange.sendResponseHeaders(500, 0);
        }

        exchange.close();
    }

    private static int swapBytes02(int value) {
        int byte0 = value & 0xFF;
        int byte2 = (value >> 16) & 0xFF;
        value &= ~((0xFF << 16) | 0xFF);
        value |= (byte0 << 16) | (byte2);
        return value;
    }
}