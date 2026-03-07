package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(GuiGraphics.class)
public interface GuiGraphicsAccessor {

    @Accessor("guiRenderState")
    GuiRenderState getGuiRenderState_CU();
}