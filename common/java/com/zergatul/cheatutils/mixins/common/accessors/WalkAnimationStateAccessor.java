package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.world.entity.WalkAnimationState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(WalkAnimationState.class)
public interface WalkAnimationStateAccessor {

    @Accessor("speedOld")
    void setSpeedOld_CU(float value);

    @Accessor("position")
    void setPosition_CU(float value);
}