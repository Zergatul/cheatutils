package com.zergatul.cheatutils.mixins.neoforge;

import com.zergatul.cheatutils.common.LoaderBridge;
import com.zergatul.cheatutils.neoforge.NeoForgeLoaderBridge;
import com.zergatul.mixin.ReplaceMethod;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(LoaderBridge.class)
public interface MixinLoaderBridge {

    @ReplaceMethod(at = @At("HEAD"), method = "create")
    private static LoaderBridge create() {
        return NeoForgeLoaderBridge.INSTANCE;
    }
}