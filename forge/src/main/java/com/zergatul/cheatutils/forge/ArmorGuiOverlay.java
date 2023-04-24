package com.zergatul.cheatutils.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class ArmorGuiOverlay implements IGuiOverlay {

    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (mc.player == null) {
            return;
        }
        if (!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false);
            int left = screenWidth / 2 + 28;
            int top = screenHeight - gui.rightHeight - 6;

            if (com.zergatul.cheatutils.modules.visuals.ArmorOverlay.instance.render(poseStack, left, top)) {
                gui.rightHeight += 12;
            }
        }
    }
}