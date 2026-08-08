package com.zergatul.cheatutils.webui;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.apache.http.HttpException;

public class CoordinatesApi extends ApiBase {

    @Override
    public String getRoute() {
        return "coordinates";
    }

    @Override
    public String get() throws HttpException {
        Vec3 pos = ClientThreadDispatcher.call(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return null;
            }
            return mc.player.getPosition(1.0f);
        });
        if (pos == null) {
            return gson.toJson(null);
        } else {
            return gson.toJson(new Response(pos));
        }
    }

    public static class Response {
        public double x;
        public double y;
        public double z;

        public Response(Vec3 pos) {
            this.x = pos.x;
            this.y = pos.y;
            this.z = pos.z;
        }
    }
}