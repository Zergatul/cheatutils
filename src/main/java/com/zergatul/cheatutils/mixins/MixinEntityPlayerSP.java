package com.zergatul.cheatutils.mixins;

import com.mojang.authlib.GameProfile;
import com.zergatul.cheatutils.modules.esp.FreeCam;
import com.zergatul.cheatutils.modules.visuals.FullBright;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {

    private MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
        throw new AssertionError();
    }

    @Inject(at = @At("HEAD"), method = "getLook(F)Lnet/minecraft/util/math/Vec3d;", cancellable = true)
    private void onGetLook(float partialTicks, CallbackInfoReturnable<Vec3d> info) {
        FreeCam freeCam = FreeCam.INSTANCE;
        if (freeCam.shouldOverrideCameraEntityForPicking((EntityPlayerSP) (Object) this)) {
            info.setReturnValue(freeCam.getTargetLookVector());
        }
    }

    @Override
    public boolean isPotionActive(Potion potionIn) {
        if (potionIn == MobEffects.NIGHT_VISION && FullBright.INSTANCE.isActive()) {
            return true;
        }
        return super.isPotionActive(potionIn);
    }

    @Override
    public @Nullable PotionEffect getActivePotionEffect(Potion potionIn) {
        if (potionIn == MobEffects.NIGHT_VISION && FullBright.INSTANCE.isActive()) {
            return new PotionEffect(MobEffects.NIGHT_VISION, 1000);
        }
        return super.getActivePotionEffect(potionIn);
    }
}