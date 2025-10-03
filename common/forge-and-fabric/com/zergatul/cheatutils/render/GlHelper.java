package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuTexture;

public class GlHelper {

    public static GlDevice getGlDevice(GpuDevice device) {
        return (GlDevice) device;
    }

    public static GlTexture getGlTexture(GpuTexture texture) {
        return (GlTexture) texture;
    }
}