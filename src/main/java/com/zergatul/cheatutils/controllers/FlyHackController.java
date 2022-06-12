package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;

public class FlyHackController {

    public static final FlyHackController instance = new FlyHackController();

    private FlyHackController() {
        Minecraft.getInstance().player.setDeltaMovement(new Vec3(0,0,0));
    }
}
