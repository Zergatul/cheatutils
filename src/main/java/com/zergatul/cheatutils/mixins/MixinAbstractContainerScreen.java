package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ContainerButtonsConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(AbstractContainerScreen.class)
public abstract class MixinAbstractContainerScreen<T extends AbstractContainerMenu> extends Screen {

    @Shadow
    protected int topPos;

    @Shadow
    protected int leftPos;

    @Shadow
    protected int imageWidth;

    @Shadow
    @Final
    protected T menu;

    protected MixinAbstractContainerScreen(Component component) {
        super(component);
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;init()V")
    private void onInit(CallbackInfo info) {
        Object self = this;
        if (self instanceof EnchantmentScreen) {
            return;
        }

        ContainerButtonsConfig config = ConfigStore.instance.getConfig().containerButtonsConfig;
        int cursor = this.leftPos + this.imageWidth;
        int space = 4;
        if (config.showTakeAll) {
            int btnWidth = 72;
            int btnHeight = 20;
            cursor -= btnWidth;
            addRenderableWidget(new Button(cursor, this.topPos - btnHeight, btnWidth, btnHeight, new TranslatableComponent("button.take.all"), this::onTakeAllPress));
            cursor -= space;
        }
        if (config.showSmartPut) {
            int btnWidth = 72;
            int btnHeight = 20;
            cursor -= btnWidth;
            addRenderableWidget(new Button(cursor, this.topPos - btnHeight, btnWidth, btnHeight, new TranslatableComponent("button.smart.put"), this::onSmartPutPress));
        }
    }

    private void onTakeAllPress(Button button) {
        NonNullList<Slot> slots = this.menu.slots;
        if (slots.size() > 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameMode == null || mc.player == null) {
                return;
            }
            Container container = slots.get(0).container;
            for (int i = 0; i < slots.size(); i++) {
                Slot slot = slots.get(i);
                if (slot.container != container) {
                    break;
                }
                if (slot.getItem().isEmpty()) {
                    continue;
                }
                mc.gameMode.handleInventoryMouseClick(this.menu.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
            }
        }
    }

    private void onSmartPutPress(Button button) {
        NonNullList<Slot> slots = this.menu.slots;
        if (slots.size() > 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gameMode == null || mc.player == null) {
                return;
            }
            Container container = slots.get(0).container;
            List<Item> containerItems = new ArrayList<>();
            for (int i = 0; i < slots.size(); i++) {
                Slot slot = slots.get(i);
                if (slot.getItem().isEmpty()) {
                    continue;
                }

                Item item = slot.getItem().getItem();
                if (slot.container == container) {
                    if (!containerItems.contains(item)) {
                        containerItems.add(item);
                    }
                } else {
                    if (containerItems.contains(item)) {
                        mc.gameMode.handleInventoryMouseClick(this.menu.containerId, i, 0, ClickType.QUICK_MOVE, mc.player);
                    }
                }
            }
        }
    }
}