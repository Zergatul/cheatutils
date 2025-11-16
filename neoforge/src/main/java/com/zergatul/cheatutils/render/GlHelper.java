package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.opengl.GlDevice;
import com.mojang.blaze3d.opengl.GlTexture;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.textures.GpuTexture;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuDevice;
import net.neoforged.neoforge.client.blaze3d.validation.ValidationGpuTexture;

public class GlHelper {

    public static GlDevice getGlDevice(GpuDevice device) {
        if (device instanceof GlDevice glDevice) {
            return glDevice;
        } else {
            return (GlDevice) ((ValidationGpuDevice) device).getRealDevice();
        }
    }

    public static GlTexture getGlTexture(GpuTexture texture) {
        if (texture instanceof GlTexture glTexture) {
            return glTexture;
        } else {
            return (GlTexture) ((ValidationGpuTexture) texture).getRealTexture();
        }
    }
}
