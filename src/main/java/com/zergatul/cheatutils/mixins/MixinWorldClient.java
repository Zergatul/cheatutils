package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WorldClient.class)
public abstract class MixinWorldClient {

    @Inject(method = "invalidateRegionAndSetBlock", at = @At("RETURN"))
    private void onBlockStateUpdated(BlockPos pos, IBlockState state, CallbackInfoReturnable<Boolean> info) {
        Events.RawBlockUpdated.trigger(new BlockUpdateEvent(pos, state));
    }
}