package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.ClientWorldMixinInterface;
import net.minecraft.client.world.ClientEntityManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientWorld.class)
public abstract class MixinClientWorld implements ClientWorldMixinInterface {
    @Shadow
    @Final
    private ClientEntityManager<Entity> entityManager;

    @Override
    public ClientEntityManager<Entity> getEntityManager() {
        return this.entityManager;
    }
}