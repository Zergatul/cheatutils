package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.render.TextureUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;

import java.io.IOException;
import java.io.OutputStream;

public class TexturesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().substring("/textures/".length());
        Identifier location = Identifier.parse(id);

        try {
            Minecraft mc = Minecraft.getInstance();
            byte[] png = mc.submit(() -> {
                TextureManager textureManager = mc.getTextureManager();
                AbstractTexture texture = textureManager.getTexture(location);
                return TextureUtils.toPng(texture.getTexture());
            }).get().get();

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