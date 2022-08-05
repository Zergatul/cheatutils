package com.zergatul.cheatutils.interfaces;

import net.minecraft.client.Camera;

public interface GameRendererMixinInterface {
    double getFov(Camera camera, float partialTicks);
}