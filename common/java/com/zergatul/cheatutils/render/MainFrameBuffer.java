package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL30;

import static com.zergatul.cheatutils.render.GlHelper.*;

public class MainFrameBuffer {

    public static void bind() {
        GlStateManager._glBindFramebuffer(GL30.GL_FRAMEBUFFER, getFBO());
    }

    private static int getFBO() {
        RenderTarget target = Minecraft.getInstance().getMainRenderTarget();
        GpuTexture colorTexture = target.getColorTexture();
        GpuTexture depthTexture = target.getDepthTexture();
        return getGlTexture(colorTexture).getFbo(getGlDevice(RenderSystem.getDevice()).directStateAccess(), getGlTexture(depthTexture));
    }
}