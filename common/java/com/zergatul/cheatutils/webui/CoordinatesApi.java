package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.ExecutionException;

public class CoordinatesApi extends ApiBase {

    @Override
    public String getRoute() {
        return "coordinates";
    }

    @Override
    public String get() throws ExecutionException, InterruptedException {
        return gson.toJson(ClientTickEndExecutor.instance.submit(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.player == null) {
                return null;
            }
            Vec3 pos = mc.player.getPosition(1.0f);
            return new Response(pos);
        }).get());
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