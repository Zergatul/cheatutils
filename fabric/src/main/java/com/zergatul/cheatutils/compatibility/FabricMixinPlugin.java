package com.zergatul.cheatutils.compatibility;

import com.zergatul.mixin.MixinPlugin;
import net.fabricmc.loader.impl.FabricLoaderImpl;
import net.fabricmc.loader.impl.ModContainerImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

public class FabricMixinPlugin extends MixinPlugin {

    private final Logger logger = LogManager.getLogger(FabricMixinPlugin.class);

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("com.zergatul.cheatutils.mixins.fabric.compatibility.sodium.")) {
            List<ModContainerImpl> mods = FabricLoaderImpl.INSTANCE.getModsInternal();
            boolean sodium = mods.stream().anyMatch(m -> m.getMetadata().getId().equals("sodium"));
            if (sodium) {
                logger.info("Sodium detected. Will apply {}.", mixinClassName);
                return true;
            } else {
                return false;
            }
        }

        if (mixinClassName.startsWith("com.zergatul.cheatutils.mixins.fabric.compatibility.iris.")) {
            List<ModContainerImpl> mods = FabricLoaderImpl.INSTANCE.getModsInternal();
            boolean iris = mods.stream().anyMatch(m -> m.getMetadata().getId().equals("iris"));
            if (iris) {
                logger.info("Iris detected. Will apply {}.", mixinClassName);
                return true;
            } else {
                return false;
            }
        }

        return super.shouldApplyMixin(targetClassName, mixinClassName);
    }
}