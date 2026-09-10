package com.zergatul.cheatutils.webui;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.zergatul.cheatutils.render.TextureUtils;
import com.zergatul.cheatutils.utils.ResourceLocationHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.ITextureObject;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.LayeredColorMaskTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.item.EnumDyeColor;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;

public class TexturesHandler implements HttpHandler {

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String id = exchange.getRequestURI().getPath().substring("/textures/".length());
        ResourceLocation location = ResourceLocationHelper.parseSafe(id);
        if (location == null) {
            exchange.sendResponseHeaders(HttpResponseCodes.NOT_FOUND, 0);
            return;
        }

        try {
            Minecraft mc = Minecraft.getMinecraft();
            byte[] png = mc.addScheduledTask(() -> {
                TextureManager textureManager = mc.getTextureManager();
                ITextureObject texture = textureManager.getTexture(location);
                if (texture == null && location.equals(SpecialBlockModels.BANNER_TEXTURE)) {
                    // Use vanilla's mask compositor so the cloth is dyed but the wood is not.
                    texture = new LayeredColorMaskTexture(new ResourceLocation("textures/entity/banner_base.png"),
                            Collections.singletonList("textures/entity/banner/base.png"),
                            Collections.singletonList(EnumDyeColor.BLACK));
                    textureManager.loadTexture(location, texture);
                    texture = textureManager.getTexture(location);
                }
                if (texture == null && location.getPath().endsWith(".png")) {
                    if (textureManager.loadTexture(location, new SimpleTexture(location))) {
                        texture = textureManager.getTexture(location);
                    }
                }
                if (texture instanceof AbstractTexture) {
                    return TextureUtils.toPng((AbstractTexture) texture);
                } else {
                    return null;
                }
            }).get();

            if (png == null) {
                exchange.sendResponseHeaders(HttpResponseCodes.NOT_FOUND, 0);
                return;
            }

            HttpHelper.setContentType(exchange, "texture.png");
            HttpHelper.setCacheControl(exchange);
            exchange.sendResponseHeaders(200, png.length);
            OutputStream os = exchange.getResponseBody();
            os.write(png);
            os.close();
        } catch (Throwable e) {
            WebHelper.sendException(exchange, e);
        }

        exchange.close();
    }
}