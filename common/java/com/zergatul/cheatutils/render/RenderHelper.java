package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.systems.RenderSystem;

public class RenderHelper {
    public static boolean isOpenGL() {
        return RenderSystem.getDevice().getDeviceInfo().backendName().equals("OpenGL");
    }
}