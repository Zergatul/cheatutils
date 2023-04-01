package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.scripting.api.HelpText;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;

public class GameApi {

    private final Minecraft mc = Minecraft.getInstance();

    public DimensionApi dimension = new DimensionApi();

    public boolean isSinglePlayer() {
        return mc.getSingleplayerServer() != null;
    }

    public String getVersion() {
        return SharedConstants.getCurrentVersion().getName();
    }

    public String getUserName() {
        return mc.getUser().getName();
    }

    public int getTick() {
        if (mc.level == null) {
            return 0;
        }
        return (int) mc.level.getGameTime();
    }

    @HelpText("In ticks. Cycles from 0 to 24000.")
    public int getDayTime() {
        if (mc.level == null) {
            return 0;
        }
        return (int) (mc.level.getDayTime() % 24000);
    }

    public static class DimensionApi {

        private static final Minecraft mc = Minecraft.getInstance();

        public boolean isOverworld() {
            if (mc.level == null) {
                return false;
            }
            return mc.level.dimension() == Level.OVERWORLD;
        }

        public boolean isNether() {
            if (mc.level == null) {
                return false;
            }
            return mc.level.dimension() == Level.NETHER;
        }

        public boolean isEnd() {
            if (mc.level == null) {
                return false;
            }
            return mc.level.dimension() == Level.END;
        }
    }
}