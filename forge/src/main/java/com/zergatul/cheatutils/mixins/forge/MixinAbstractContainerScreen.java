package com.zergatul.cheatutils.mixins.forge;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PreRenderTooltipEvent;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen {

    @Shadow
    @Nullable
    protected Slot hoveredSlot;

    @Inject(
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;Ljava/util/Optional;Lnet/minecraft/world/item/ItemStack;II)V"),
            method = "renderTooltip(Lnet/minecraft/client/gui/GuiGraphics;II)V",
            cancellable = true)
    private void onBeforeRenderTooltip(GuiGraphics graphics, int x, int y, CallbackInfo info) {
        ItemStack itemStack = this.hoveredSlot.getItem();
        if (Events.PreRenderTooltip.trigger(new PreRenderTooltipEvent(graphics, itemStack, x, y))) {
            info.cancel();
        }
    }
}
