package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import net.minecraft.client.Minecraft;

import java.util.concurrent.ExecutionException;

public class DimensionApi extends ApiBase {

    @Override
    public String getRoute() {
        return "dimension";
    }

    @Override
    public String get() throws ExecutionException, InterruptedException {
        return gson.toJson(ClientTickEndExecutor.instance.submit(() -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.level == null) {
                return null;
            }
            return mc.level.dimension().identifier().toString();
        }).get());
    }
}