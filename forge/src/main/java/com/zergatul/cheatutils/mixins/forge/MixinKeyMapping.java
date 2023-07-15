package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = KeyMapping.class, remap = false)
public abstract class MixinKeyMapping implements IForgeKeyMapping {

    @Override
    public boolean isConflictContextAndModifierActive() {
        if (ConfigStore.instance.getConfig().invMoveConfig.enabled) {
            return true;
        } else {
            return IForgeKeyMapping.super.isConflictContextAndModifierActive();
        }
    }
}