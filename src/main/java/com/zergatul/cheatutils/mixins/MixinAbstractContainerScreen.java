package com.zergatul.cheatutils.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ContainerButtonsConfig;
import com.zergatul.cheatutils.controllers.ContainerSummaryController;
import com.zergatul.cheatutils.utils.GuiUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.EnchantmentScreen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.EnchantmentMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

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
        if (self instanceof InventoryScreen) {
            return;
        }

        ContainerButtonsConfig config = ConfigStore.instance.getConfig().containerButtonsConfig;
        int cursor = this.leftPos + this.imageWidth;
        int space = 4;
        if (config.showTakeAll) {
            int btnWidth = 72;
            int btnHeight = 20;
            cursor -= btnWidth;
            addRenderableWidget(new Button(cursor, this.topPos - btnHeight, btnWidth, btnHeight, Component.translatable("button.take.all"), this::onTakeAllPress));
            cursor -= space;
        }
        if (config.showSmartPut) {
            int btnWidth = 72;
            int btnHeight = 20;
            cursor -= btnWidth;
            addRenderableWidget(new Button(cursor, this.topPos - btnHeight, btnWidth, btnHeight, Component.translatable("button.smart.put"), this::onSmartPutPress));
        }
    }

    @Inject(at = @At("TAIL"), method = "Lnet/minecraft/client/gui/screens/inventory/AbstractContainerScreen;render(Lcom/mojang/blaze3d/vertex/PoseStack;IIF)V")
    private void onRender(PoseStack poseStack, int slot, int k, float l1, CallbackInfo info) {
        if (!ConfigStore.instance.getConfig().containerSummaryConfig.enabled) {
            return;
        }

        NonNullList<Slot> slots = this.menu.slots;
        if (slots.size() == 0) {
            return;
        }
        if (slots.get(0).container instanceof SimpleContainer container) {
            List<ItemStack> items = new ArrayList<>();
            for (int i = 0; i < container.getContainerSize(); i++) {
                ItemStack itemStack = container.getItem(i);
                if (!itemStack.isEmpty()) {
                    items.add(itemStack);
                }
            }

            List<ContainerSummaryController.ItemDrawable> list = ContainerSummaryController.instance.groupItems(items);


            this.setBlitOffset(100);
            this.itemRenderer.blitOffset = 100.0F;
            RenderSystem.enableDepthTest();

            boolean group = !Screen.hasAltDown();

            list.forEach(d -> d.initDraw(this.font, group));
            List<ContainerSummaryController.ItemsColumn> columns = ContainerSummaryController.instance.split(list);
            columns.forEach(ContainerSummaryController.ItemsColumn::calculateWidth);

            int cursor = this.leftPos + this.imageWidth + 2;
            for (ContainerSummaryController.ItemsColumn column: columns) {
                cursor += column.draw(poseStack, this.font, this.itemRenderer, this.minecraft.player, cursor, this.topPos);
            }

            this.itemRenderer.blitOffset = 0.0F;
            this.setBlitOffset(0);
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