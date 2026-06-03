package com.zergatul.cheatutils.mixins.neoforge;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.modules.hacks.InvMove;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.extensions.IKeyMappingExtension;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(KeyMapping.class)
public abstract class MixinKeyMapping implements IKeyMappingExtension {

    @Override
    public boolean isActiveAndMatches(InputConstants.Key keyCode) {
        if (InvMove.instance.shouldIgnoreKeyContext((KeyMapping) (Object) this)) {
            return keyCode != InputConstants.UNKNOWN && keyCode.equals(this.getKey());
        } else {
            return IKeyMappingExtension.super.isActiveAndMatches(keyCode);
        }
    }

    @Override
    public boolean isConflictContextAndModifierActive() {
        if (InvMove.instance.shouldIgnoreKeyContext((KeyMapping) (Object) this)) {
            return true;
        } else {
            return IKeyMappingExtension.super.isConflictContextAndModifierActive();
        }
    }
}