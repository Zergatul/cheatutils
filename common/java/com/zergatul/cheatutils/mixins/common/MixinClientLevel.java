package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientLevel.class)
public abstract class MixinClientLevel extends Level {

    private MixinClientLevel(
            WritableLevelData levelData,
            ResourceKey<Level> dimension,
            RegistryAccess registryAccess,
            Holder<DimensionType> dimensionTypeRegistration,
            boolean isClientSide,
            boolean isDebug,
            long biomeZoomSeed,
            int maxChainedNeighborUpdates
    ) {
        super(levelData, dimension, registryAccess, dimensionTypeRegistration, isClientSide, isDebug, biomeZoomSeed, maxChainedNeighborUpdates);
    }

    @Inject(at = @At("TAIL"), method = "addEntity(Lnet/minecraft/world/entity/Entity;)V")
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
    private void onSetServerVerifiedBlockState(BlockPos pos, BlockState state, int updateFlag, CallbackInfo info) {
        LevelChunk chunk = this.getChunkAt(pos);
        Events.RawBlockUpdated.trigger(new BlockUpdateEvent(chunk, pos.immutable(), state));
    }
}