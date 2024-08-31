package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.mixins.neoforge.accessors.GuiGraphicsAccessor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;

public class Tooltip {

    public static ItemStack getCurrentItemStack(GuiGraphics graphics) {
        return ((GuiGraphicsAccessor) graphics).getTooltipStack_CU();
    }
}