package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.modules.hacks.InvMove;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraftforge.client.extensions.IForgeKeyMapping;
import net.minecraftforge.client.settings.IKeyConflictContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = KeyMapping.class, remap = false)
public abstract class MixinKeyMapping implements IForgeKeyMapping {

    @Inject(
            method = "getKeyConflictContext",
            at = @At("HEAD"),
            cancellable = true)
    private void onGetKeyConflictContext(CallbackInfoReturnable<IKeyConflictContext> info) {
        if (InvMove.instance.shouldPassEvents(Minecraft.getInstance().screen)) {
            info.setReturnValue(new IKeyConflictContext() {
                @Override
                public boolean isActive() {
                    return true;
                }

                @Override
                public boolean conflicts(IKeyConflictContext other) {
                    return false;
                }
            });
        }
    }
}