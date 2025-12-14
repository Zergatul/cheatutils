package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.render.gl.TextureUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.CompletableFuture;

public class TexturesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().substring("/textures/".length());
        ResourceLocation location = ResourceLocation.parse(id);
        TextureManager textureManager = Minecraft.getInstance().getTextureManager();
        AbstractTexture texture = textureManager.getTexture(location);

        CompletableFuture<byte[]> pngFuture = new CompletableFuture<>();
        TickEndExecutor.instance.execute(() -> {
            try {
                pngFuture.complete(TextureUtils.toPng(texture));
            } catch (IOException e) {
                pngFuture.completeExceptionally(e);
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
            WebHelper.sendException(exchange, e);
        }

        exchange.close();
    }
}