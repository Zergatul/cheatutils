package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.modules.hacks.InvMove;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = KeyMapping.class, remap = false)
public abstract class MixinKeyMapping implements IForgeKeyMapping {

    @Override
    public boolean isActive() {
        if (InvMove.instance.shouldIgnoreForgeKeyContext((KeyMapping) (Object) this)) {
            return true;
        } else {
            return IForgeKeyMapping.super.isActive();
        }
    }

    @Override
    public boolean isConflictContextAndModifierActive() {
        if (InvMove.instance.shouldIgnoreForgeKeyContext((KeyMapping) (Object) this)) {
            return true;
        } else {
            return IForgeKeyMapping.super.isConflictContextAndModifierActive();
        }
    }
}