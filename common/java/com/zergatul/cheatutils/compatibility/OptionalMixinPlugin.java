package com.zergatul.cheatutils.compatibility;

import com.zergatul.cheatutils.Constants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Lazy;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;
import org.spongepowered.asm.service.MixinService;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public abstract class OptionalMixinPlugin implements IMixinConfigPlugin {

    private final Logger logger = LogManager.getLogger(OptionalMixinPlugin.class);
    private final Lazy<Boolean> isEnabled = Lazy.pure(this::isEnabled);

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        return isEnabled.get();
    }

    private boolean isEnabled() {
        try {
            MixinService.getService().getBytecodeProvider().getClassNode(getDetectionClassName());
            logger.info("{} detected, applying {} mixins", getModName(), Constants.MOD_ID);
            return true;
        } catch (ClassNotFoundException | IOException e) {
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    protected abstract String getDetectionClassName();

    protected abstract String getModName();
}