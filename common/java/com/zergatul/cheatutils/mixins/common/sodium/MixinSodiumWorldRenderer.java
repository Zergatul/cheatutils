package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(SodiumWorldRenderer.class)
public abstract class MixinSodiumWorldRenderer {

    @ModifyVariable(
            method = "setupTerrain",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0)
    private boolean onOverrideIsSpectator(boolean spectator) {
        if (FreeCam.instance.isActive()) {
            return true;
        } else {
            return spectator;
        }
    }
}