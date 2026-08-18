package com.zergatul.cheatutils.common;

import com.zergatul.cheatutils.Constants;
import org.spongepowered.asm.mixin.Mixins;

public class MixinPlugin extends com.zergatul.mixin.MixinPlugin {

    @Override
    public void onLoad(String s) {
        super.onLoad(s);

        if (isStrictMixinsEnabled()) {
            Mixins.registerErrorHandlerClass(DevelopmentMixinErrorHandler.class.getCanonicalName());
        }
    }

    public static boolean isStrictMixinsEnabled() {
        return Boolean.getBoolean(Constants.MOD_ID + ".strictMixins");
    }
}