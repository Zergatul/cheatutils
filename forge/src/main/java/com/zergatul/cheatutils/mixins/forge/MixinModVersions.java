package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.ModMain;
import net.minecraftforge.network.packets.ModVersions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(ModVersions.class)
public abstract class MixinModVersions {

    @Inject(at = @At("TAIL"), method = "create()Lnet/minecraftforge/network/packets/ModVersions;", remap = false)
    private static void onCreate(CallbackInfoReturnable<ModVersions> info) {
        info.getReturnValue().mods().remove(ModMain.MODID);
    }
}
