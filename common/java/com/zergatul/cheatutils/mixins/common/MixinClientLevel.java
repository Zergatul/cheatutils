package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.controllers.BlockEventsProcessor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.LevelEntityGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel {

    @Shadow protected abstract LevelEntityGetter<Entity> getEntities();

    @Inject(at = @At("HEAD"), method = "addEntity(Lnet/minecraft/world/entity/Entity;)V")
    private void onAddEntity(Entity entity, CallbackInfo info) {
        Events.EntityAdded.trigger(entity);
    }

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;setRemoved(Lnet/minecraft/world/entity/Entity$RemovalReason;)V"),
            method = "removeEntity(ILnet/minecraft/world/entity/Entity$RemovalReason;)V")
    private void onRemoveEntity(int id, Entity.RemovalReason reason, CallbackInfo info) {
        Events.EntityRemoved.trigger(this.getEntities().get(id));
    }

    @Inject(at = @At("HEAD"), method = "setServerVerifiedBlockState")
    private void onSetServerVerifiedBlockState(BlockPos pos, BlockState state, int unknown, CallbackInfo info) {
        BlockEventsProcessor.instance.onBlockUpdated(pos, state);
    }
}