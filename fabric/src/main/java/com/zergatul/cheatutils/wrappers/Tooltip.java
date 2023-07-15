package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.fabric.GuiGraphicsHelper;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class Tooltip {

    public static ItemStack getCurrentItemStack(GuiGraphics graphics) {
        return GuiGraphicsHelper.tooltipItemStack;
    }
}