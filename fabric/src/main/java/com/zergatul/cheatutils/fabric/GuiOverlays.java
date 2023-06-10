package com.zergatul.cheatutils.fabric;

import com.zergatul.cheatutils.modules.visuals.ArmorOverlay;
import com.zergatul.cheatutils.modules.visuals.BetterStatusEffects;
import net.minecraft.client.gui.GuiGraphics;

public class GuiOverlays {

    public static void render(GuiGraphics graphics, int screenWidth, int screenHeight, int rightY) {
        ArmorOverlay.instance.render(graphics, screenWidth / 2 + 28, rightY + 4);
        BetterStatusEffects.instance.render(graphics, screenWidth, rightY - 34);
    }
}