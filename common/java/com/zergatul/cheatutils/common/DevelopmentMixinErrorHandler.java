package com.zergatul.cheatutils.common;

import org.spongepowered.asm.mixin.extensibility.IMixinConfig;
import org.spongepowered.asm.mixin.extensibility.IMixinErrorHandler;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

public class DevelopmentMixinErrorHandler implements IMixinErrorHandler {

    @Override
    public ErrorAction onPrepareError(IMixinConfig config, Throwable th, IMixinInfo mixin, ErrorAction action) {
        if (mixin.getClassName().startsWith("com.zergatul.cheatutils.mixins.")) {
            return ErrorAction.ERROR;
        }
        return action;
    }

    @Override
    public ErrorAction onApplyError(String targetClassName, Throwable th, IMixinInfo mixin, ErrorAction action) {
        if (mixin.getClassName().startsWith("com.zergatul.cheatutils.mixins.")) {
            return ErrorAction.ERROR;
        }
        return action;
    }
}