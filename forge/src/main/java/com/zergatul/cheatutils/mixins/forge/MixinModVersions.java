package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.PrivacyConfig;
import net.minecraftforge.network.packets.ModVersions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModVersions.class)
public abstract class MixinModVersions {

    @Inject(at = @At("TAIL"), method = "create()Lnet/minecraftforge/network/packets/ModVersions;", remap = false)
    private static void onCreate(CallbackInfoReturnable<ModVersions> info) {
        PrivacyConfig config = ConfigStore.instance.getConfig().privacyConfig;
        if (config.hideFromModVersions) {
            info.getReturnValue().mods().remove(Constants.MOD_ID);
        }
    }
}