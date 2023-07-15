package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PreRenderTooltipEvent;
import com.zergatul.cheatutils.wrappers.Tooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics {

    @Inject(
            at = @At("HEAD"),
            method = "renderTooltipInternal(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;)V",
            cancellable = true)
    private void onRenderTooltipInternal(Font font, List<ClientTooltipComponent> list, int x, int y, ClientTooltipPositioner p_262920_, CallbackInfo info) {
        GuiGraphics graphics = (GuiGraphics) (Object) this;
        ItemStack itemStack = Tooltip.getCurrentItemStack(graphics);
        if (itemStack == null) {
            return;
        }

        if (list.isEmpty()) {
            return;
        }

        if (Events.PreRenderTooltip.trigger(new PreRenderTooltipEvent(graphics, itemStack, x, y))) {
            info.cancel();
        }
    }
}