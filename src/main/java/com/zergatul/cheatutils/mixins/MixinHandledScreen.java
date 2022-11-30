package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ContainerButtonsConfig;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import net.minecraft.util.collection.DefaultedList;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

// Forge: MixinAbstractContainerScreen

@Mixin(HandledScreen.class)
public abstract class MixinHandledScreen<T extends ScreenHandler> extends Screen {

    @Shadow
    protected int x;

    @Shadow
    protected int y;

    @Shadow
    protected int backgroundWidth;

    @Shadow @Final protected T handler;

    protected MixinHandledScreen(Text title) {
        super(title);
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/gui/screen/ingame/HandledScreen;init()V")
    private void onInit(CallbackInfo info) {
        Object self = this;
        if (self instanceof EnchantmentScreen) {
            return;
        }
        if (self instanceof InventoryScreen) {
            return;
        }

        ContainerButtonsConfig config = ConfigStore.instance.getConfig().containerButtonsConfig;
        int cursor = this.x + this.backgroundWidth;
        int space = 4;
        if (config.showTakeAll) {
            int btnWidth = 72;
            int btnHeight = 20;
            cursor -= btnWidth;
            addDrawableChild(new ButtonWidget(cursor, this.y - btnHeight, btnWidth, btnHeight, Text.translatable("button.take.all"), this::onTakeAllPress));
            cursor -= space;
        }
        if (config.showSmartPut) {
            int btnWidth = 72;
            int btnHeight = 20;
            cursor -= btnWidth;
            addDrawableChild(new ButtonWidget(cursor, this.y - btnHeight, btnWidth, btnHeight, Text.translatable("button.smart.put"), this::onSmartPutPress));
        }
    }

    private void onTakeAllPress(ButtonWidget button) {
        DefaultedList<Slot> slots = this.handler.slots;
        if (slots.size() > 0) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.interactionManager == null || mc.player == null) {
                return;
            }
            Inventory inventory = slots.get(0).inventory;
            for (int i = 0; i < slots.size(); i++) {
                Slot slot = slots.get(i);
                if (slot.inventory != inventory) {
                    break;
                }
                if (slot.getStack().isEmpty()) {
                    continue;
                }
                mc.interactionManager.clickSlot(this.handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
            }
        }
    }

    private void onSmartPutPress(ButtonWidget button) {
        DefaultedList<Slot> slots = this.handler.slots;
        if (slots.size() > 0) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.interactionManager == null || mc.player == null) {
                return;
            }
            Inventory container = slots.get(0).inventory;
            List<Item> containerItems = new ArrayList<>();
            for (int i = 0; i < slots.size(); i++) {
                Slot slot = slots.get(i);
                if (slot.getStack().isEmpty()) {
                    continue;
                }

                Item item = slot.getStack().getItem();
                if (slot.inventory == container) {
                    if (!containerItems.contains(item)) {
                        containerItems.add(item);
                    }
                } else {
                    if (containerItems.contains(item)) {
                        mc.interactionManager.clickSlot(this.handler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
                    }
                }
            }
        }
    }
}