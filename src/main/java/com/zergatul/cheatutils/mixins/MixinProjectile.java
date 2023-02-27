package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.ProjectileMixinInterface;
import net.minecraft.world.entity.projectile.Projectile;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import javax.annotation.Nullable;
import java.util.UUID;

@Mixin(Projectile.class)
public abstract class MixinProjectile implements ProjectileMixinInterface {

    @Shadow
    @Nullable
    private UUID ownerUUID;

    @Override
    public UUID getOwnerUUID() {
        return this.ownerUUID;
    }
}