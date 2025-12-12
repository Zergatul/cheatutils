package com.zergatul.cheatutils.mixins.fnf;

import com.zergatul.cheatutils.collections.TaggedArrayList;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PreRenderTooltipEvent;
import com.zergatul.mixin.ModifyMethodReturnValue;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2ic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(GuiGraphics.class)
public abstract class MixinGuiGraphics {

    @Inject(
            at = @At("HEAD"),
            method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Lnet/minecraft/world/item/ItemStack;)V", cancellable = true)
    private void onBeforeRenderTooltip(
            Font font,
            List<ClientTooltipComponent> components,
            int x, int y,
            ClientTooltipPositioner positioner,
            @Nullable Identifier location,
            ItemStack stack2,
            CallbackInfo info
    ) {
        if (components.isEmpty()) {
            return;
        }

        if (components instanceof TaggedArrayList<?,?>) {
            ItemStack itemStack = ((TaggedArrayList<ClientTooltipComponent, ItemStack>) components).getTag();
            if (Events.PreRenderTooltip.trigger(new PreRenderTooltipEvent((GuiGraphics) (Object) this, itemStack, x, y))) {
                info.cancel();
            }
        }
    }

    @ModifyMethodReturnValue(
            method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Lnet/minecraft/world/item/ItemStack;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;positionTooltip(IIIIII)Lorg/joml/Vector2ic;"))
    private static Vector2ic onTooltipPositioned(Vector2ic position) {
        Events.TooltipPositioned.trigger(position);
        return position;
    }

    @Inject(at = @At("TAIL"), method = "renderTooltip(Lnet/minecraft/client/gui/Font;Ljava/util/List;IILnet/minecraft/client/gui/screens/inventory/tooltip/ClientTooltipPositioner;Lnet/minecraft/resources/Identifier;Lnet/minecraft/world/item/ItemStack;)V")
    private void onAfterRenderTooltipInternal(
            Font font,
            List<ClientTooltipComponent> components,
            int x, int y,
            ClientTooltipPositioner positioner,
            @Nullable Identifier location,
            ItemStack stack2,
            CallbackInfo info
    ) {
        if (components.isEmpty()) {
            return;
        }

        if (components instanceof TaggedArrayList<?,?>) {
            Events.PostRenderTooltip.trigger();
        }
    }
}