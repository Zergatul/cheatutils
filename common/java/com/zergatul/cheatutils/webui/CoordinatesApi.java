package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import org.apache.http.HttpException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class CoordinatesApi extends ApiBase {

    @Override
    public String getRoute() {
        return "coordinates";
    }

    @Override
    public String get() throws HttpException {
        Vec3 pos;
        try {
            pos = ClientTickEndExecutor.instance.submit(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) {
                    return null;
                }
                return mc.player.getPosition(1.0f);
            }).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            pos = null;
        } catch (ExecutionException | TimeoutException e) {
            pos = null;
        }
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