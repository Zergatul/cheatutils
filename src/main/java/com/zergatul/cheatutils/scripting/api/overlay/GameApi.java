package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.scripting.api.HelpText;
import net.minecraft.SharedConstants;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.World;

public class GameApi {

    private final MinecraftClient mc = MinecraftClient.getInstance();

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

    @HelpText("Will not work in 1.19.4 version, use game.dimension.isOverworld()")
    public boolean dimension_isOverworld() {
        if (mc.world == null) {
            return false;
        }
        return mc.world.getRegistryKey() == World.OVERWORLD;
    }

    @HelpText("Will not work in 1.19.4 version, use game.dimension.isNether()")
    public boolean dimension_isNether() {
        if (mc.world == null) {
            return false;
        }
        return mc.world.getRegistryKey() == World.NETHER;
    }

    @HelpText("Will not work in 1.19.4 version, use game.dimension.isEnd()")
    public boolean dimension_isEnd() {
        if (mc.world == null) {
            return false;
        }
        return mc.world.getRegistryKey() == World.END;
    }
}