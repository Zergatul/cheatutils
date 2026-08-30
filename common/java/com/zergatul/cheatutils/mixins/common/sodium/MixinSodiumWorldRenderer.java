package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(value = SodiumWorldRenderer.class, remap = false)
public abstract class MixinSodiumWorldRenderer {

    @ModifyVariable(
            method = "setupTerrain",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0,
            remap = false)
    private boolean onOverrideSpectator(boolean spectator) {
        return FreeCam.instance.isActive() || spectator;
    }
}