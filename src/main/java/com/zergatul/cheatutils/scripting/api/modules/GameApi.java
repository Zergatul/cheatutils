package com.zergatul.cheatutils.scripting.api.modules;

import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

public class GameApi {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public DimensionApi dimension = new DimensionApi();

    public boolean isSinglePlayer() {
        return mc.isInSingleplayer();
    }

    public String getVersion() {
        return SharedConstants.getGameVersion().getName();
    }

    /*public String getUserName() {
        return mc.getUser().getName();
    }*/

    public int getTick() {
        if (mc.world == null) {
            return 0;
        }
        return (int) mc.world.getTime();
    }

    public static class DimensionApi {

        private final MinecraftClient mc = MinecraftClient.getInstance();

        public boolean isOverworld() {
            if (mc.world == null) {
                return false;
            }
            return mc.world.getRegistryKey() == World.OVERWORLD;
        }

        public boolean isNether() {
            if (mc.world == null) {
                return false;
            }
            return mc.world.getRegistryKey() == World.NETHER;
        }

        public boolean isEnd() {
            if (mc.world == null) {
                return false;
            }
            return mc.world.getRegistryKey() == World.END;
        }
    }
}