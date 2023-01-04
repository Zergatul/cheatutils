package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.RegistryEntryReferenceMixinInterface;
import net.minecraft.registry.entry.RegistryEntry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(RegistryEntry.Reference.class)
public abstract class MixinRegistryEntryReference<T> implements RegistryEntryReferenceMixinInterface<T> {

    @Shadow
    @Nullable
    private T value;

    @Override
    public void setValue(T value) {
        this.value = value;
    }
}