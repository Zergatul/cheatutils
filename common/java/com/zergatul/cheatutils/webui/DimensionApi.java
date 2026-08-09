package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import net.minecraft.client.Minecraft;
import org.apache.http.HttpException;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DimensionApi extends ApiBase {

    @Override
    public String getRoute() {
        return "dimension";
    }

    @Override
    public String get() throws HttpException {
        try {
            return gson.toJson(ClientTickEndExecutor.instance.submit(() -> {
                Minecraft mc = Minecraft.getInstance();
                if (mc.level == null) {
                    return null;
                }
                return mc.level.dimension().location().toString();
            }).get(1, TimeUnit.SECONDS));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return gson.toJson(null);
        } catch (ExecutionException | TimeoutException e) {
            return gson.toJson(null);
        }
    }
}