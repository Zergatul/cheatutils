package com.zergatul.cheatutils.forge;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.modules.visuals.BetterStatusEffects;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

public class BetterStatusEffectsGuiOverlay implements IGuiOverlay {

    private final Minecraft mc = Minecraft.getInstance();

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int screenWidth, int screenHeight) {
        if (mc.player == null) {
            return;
        }
        if (!mc.options.hideGui && gui.shouldDrawSurvivalElements()) {
            gui.setupOverlayRenderState(true, false);
            int height = Math.max(gui.leftHeight, gui.rightHeight) + 24 + mc.font.lineHeight;
            int top = screenHeight - height;

            if (BetterStatusEffects.instance.render(poseStack, screenWidth, top)) {
                gui.leftHeight = gui.rightHeight = height;
            }
        }
    }
}