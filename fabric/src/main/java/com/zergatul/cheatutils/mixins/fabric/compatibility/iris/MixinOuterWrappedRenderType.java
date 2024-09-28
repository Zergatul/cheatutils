package com.zergatul.cheatutils.mixins.fabric.compatibility.iris;

import com.zergatul.cheatutils.compatibility.WrappedRenderType;
import net.irisshaders.iris.layer.OuterWrappedRenderType;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = OuterWrappedRenderType.class, remap = false)
public abstract class MixinOuterWrappedRenderType implements WrappedRenderType {}