package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.gui.Hud;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hud.class)
public interface HudAccessor {

    @Accessor("EFFECT_BACKGROUND_AMBIENT_SPRITE")
    static Identifier getEffectBackgroundAmbientSprite_CU() {
        throw new AssertionError();
    }

    @Accessor("EFFECT_BACKGROUND_SPRITE")
    static Identifier getEffectBackgroundSprite_CU() {
        throw new AssertionError();
    }
}