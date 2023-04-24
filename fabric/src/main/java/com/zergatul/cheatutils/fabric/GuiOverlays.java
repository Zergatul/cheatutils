package com.zergatul.cheatutils.fabric;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.modules.visuals.ArmorOverlay;
import com.zergatul.cheatutils.modules.visuals.BetterStatusEffects;

public class GuiOverlays {

    public static void render(PoseStack poseStack, int screenWidth, int screenHeight, int rightY) {
        ArmorOverlay.instance.render(poseStack, screenWidth / 2 + 28, rightY + 4);
        BetterStatusEffects.instance.render(poseStack, screenWidth, rightY - 34);
    }
}