package com.zergatul.cheatutils.mixins.common.accessors;

import net.minecraft.client.gui.components.inventory.SlotWidget;
import net.minecraft.client.gui.screens.inventory.AbstractRecipeBookScreen;
import net.minecraft.world.inventory.ContainerInput;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AbstractRecipeBookScreen.class)
public interface AbstractRecipeBookScreenAccessor {

    @Invoker("slotClicked")
    void slotClicked_CU(@Nullable SlotWidget slotWidget, int slotId, int buttonNum, ContainerInput containerInput);
}