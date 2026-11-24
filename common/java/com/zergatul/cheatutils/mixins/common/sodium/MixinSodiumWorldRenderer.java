package com.zergatul.cheatutils.mixins.common.sodium;

import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.modules.visuals.Fog;
import net.caffeinemc.mods.sodium.client.render.SodiumWorldRenderer;
import net.caffeinemc.mods.sodium.client.util.FogParameters;
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
    private FogParameters onOverrideFogParameters(FogParameters original) {
        if (!Fog.INSTANCE.getSettings().get().enabled()) {
            return original;
        }

        return new FogParameters(
                original.red(),
                original.green(),
                original.blue(),
                original.alpha(),
                Float.MAX_VALUE,
                Float.MAX_VALUE,
                Float.MAX_VALUE,
                Float.MAX_VALUE);
    }

    @ModifyVariable(
            method = "setupTerrain",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 0)
    private boolean onOverrideUseOcclusionCulling(boolean useOcclusionCulling) {
        if (FreeCam.INSTANCE.isActive()) {
            return false;
        } else {
            return useOcclusionCulling;
        }
    }
}