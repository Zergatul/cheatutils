package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccessor {

    @Accessor("GUI_ICONS_LOCATION")
    static ResourceLocation getGuiIconsLocation_CU() {
        throw new AssertionError();
    }
}