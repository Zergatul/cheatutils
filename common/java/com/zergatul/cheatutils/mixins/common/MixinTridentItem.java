package com.zergatul.cheatutils.mixins.common;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.zergatul.cheatutils.modules.hacks.TridentHack;
import net.minecraft.world.item.TridentItem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

@Mixin(TridentItem.class)
public abstract class MixinTridentItem {

    @ModifyExpressionValue(
            method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private boolean onReleaseModifyIsInWaterOrRain(boolean original) {
        return TridentHack.INSTANCE.shouldOverrideIsInWater() || original;
    }

    @ModifyArgs(
            method = "releaseUsing",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;push(DDD)V"))
    private void onModifyPlayerPush(Args args) {
        double multiplier = TridentHack.INSTANCE.getPushMultiplier();
        if (multiplier != 1) {
            args.set(0, (double) args.get(0) * multiplier);
            args.set(1, (double) args.get(1) * multiplier);
            args.set(2, (double) args.get(2) * multiplier);
        }
    }

    @ModifyExpressionValue(
            method = "use",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;isInWaterOrRain()Z"))
    private boolean onUseModifyIsInWaterOrRain(boolean original) {
        return TridentHack.INSTANCE.shouldOverrideIsInWater() || original;
    }
}