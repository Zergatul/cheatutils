package com.zergatul.cheatutils.mixins.fabric;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(Screen.class)
public abstract class MixinScreen {

    @Inject(at = @At("TAIL"), method = "getTooltipFromItem", cancellable = true)
    private static void onGetTooltipFromItem(Minecraft mc, ItemStack itemStack, CallbackInfoReturnable<List<Component>> info) {
        List<Component> list = new ArrayList<>();
        Events.GatherTooltipComponents.trigger(new GatherTooltipComponentsEvent(itemStack, list));
        if (!list.isEmpty()) {
            List<Component> original = info.getReturnValue();
            List<Component> result = new ArrayList<>(original.size() + list.size());
            result.addAll(original);
            result.addAll(list);
            info.setReturnValue(result);
        }
    }
}