package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.gui.Gui;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccessor {

    @Accessor("EFFECT_BACKGROUND_AMBIENT_SPRITE")
    static ResourceLocation getEffectBackgroundAmbientSprite() {
        throw new AssertionError();
    }

    @Accessor("EFFECT_BACKGROUND_SPRITE")
    static ResourceLocation getEffectBackgroundSprite() {
        throw new AssertionError();
    }
}