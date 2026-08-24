package com.zergatul.cheatutils.webui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL21;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.CompletableFuture;

public class TexturesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            String id = exchange.getRequestURI().getPath().substring("/textures/".length());
            ResourceLocation location = ResourceLocation.tryParse(id);
            if (location == null) {
                throw new IllegalArgumentException("Invalid texture id: " + id);
            }

            Minecraft minecraft = Minecraft.getInstance();
            AbstractTexture texture = minecraft.getTextureManager().getTexture(location);
            CompletableFuture<byte[]> future = new CompletableFuture<>();

            RenderSystem.recordRenderCall(() -> {
                byte[] result = null;
                int previousTexture = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
                try {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.getId());
                    int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
                    int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
                    int format = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_INTERNAL_FORMAT);
                    if (width > 0 && height > 0 && isRgba(format)) {
                        ByteBuffer buffer = BufferUtils.createByteBuffer(Math.multiplyExact(Math.multiplyExact(width, height), 4));
                        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

                        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                int offset = (y * width + x) * 4;
                                int r = buffer.get(offset) & 0xFF;
                                int g = buffer.get(offset + 1) & 0xFF;
                                int b = buffer.get(offset + 2) & 0xFF;
                                int a = buffer.get(offset + 3) & 0xFF;
                                image.setRGB(x, y, a << 24 | r << 16 | g << 8 | b);
                            }
                        }

                        try (ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
                            ImageIO.write(image, "png", stream);
                            result = stream.toByteArray();
                        }
                    }
                } catch (Throwable throwable) {
                    future.completeExceptionally(throwable);
                    return;
                } finally {
                    GL11.glBindTexture(GL11.GL_TEXTURE_2D, previousTexture);
                }
                future.complete(result);
            });

            byte[] png = future.get();
            if (png == null) {
                throw new IllegalStateException("Unsupported texture format.");
            }

            HttpHelper.setContentType(exchange, "texture.png");
            HttpHelper.setCacheControl(exchange);
            exchange.sendResponseHeaders(HttpResponseCodes.OK, png.length);
            try (OutputStream stream = exchange.getResponseBody()) {
                stream.write(png);
            }
        } catch (Throwable throwable) {
            WebHelper.sendException(exchange, HttpResponseCodes.INTERNAL_SERVER_ERROR, throwable);
        } finally {
            exchange.close();
        }
    }

    private static boolean isRgba(int format) {
        return format == GL11.GL_RGBA ||
                format == GL11.GL_RGBA8 ||
                format == GL21.GL_SRGB8_ALPHA8;
    }
}