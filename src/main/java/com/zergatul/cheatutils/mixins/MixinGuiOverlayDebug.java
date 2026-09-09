package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.common.Events;
import net.minecraft.client.gui.GuiOverlayDebug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(GuiOverlayDebug.class)
public abstract class MixinGuiOverlayDebug {

    @Inject(at = @At("RETURN"), method = "call()Ljava/util/List;")
    private void onGetLeft(CallbackInfoReturnable<List<String>> info) {
        Events.DebugInfoLeft.trigger(info.getReturnValue());
    }
}