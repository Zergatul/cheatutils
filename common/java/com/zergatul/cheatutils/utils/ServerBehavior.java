package com.zergatul.cheatutils.utils;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class ServerBehavior {

    private static final Minecraft mc = Minecraft.getInstance();

    public static Vec3 predictPlayerKnownMovement() {
        assert mc.player != null;

        if (mc.player.isFallFlying()) {
            return mc.player.getDeltaMovement();
        } else {
            return Vec3.ZERO;
        }
    }
}