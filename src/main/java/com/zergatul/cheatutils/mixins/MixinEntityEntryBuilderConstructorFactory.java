package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.utils.EntityUtils;
import com.zergatul.mixin.RethrowWithCondition;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Debug(export = true)
@Mixin(targets = "net.minecraftforge.fml.common.registry.EntityEntryBuilder$ConstructorFactory", remap = false)
public abstract class MixinEntityEntryBuilderConstructorFactory {

    @RethrowWithCondition(
            method = "apply(Lnet/minecraft/world/World;)Lnet/minecraft/entity/Entity;",
            at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;error(Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;)V"))
    private static boolean onEntityTypeFactoryException() {
        return EntityUtils.isInProgress();
    }
}