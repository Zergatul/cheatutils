package com.zergatul.cheatutils.mixins.common;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ContainerScreenCalculateHoveredSlotEvent;
import com.zergatul.cheatutils.common.events.ContainerScreenRenderEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ContainerButtonsConfig;
import com.zergatul.cheatutils.controllers.ContainerButtonsController;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

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

    @Inject(at = @At("TAIL"), method = "init()V")
    private void onInit(CallbackInfo info) {
        Screen self = this;
        if (!ContainerButtonsController.instance.isValidScreen(self)) {
            return;
        }

        ContainerButtonsConfig config = ConfigStore.instance.getConfig().containerButtonsConfig;
        int cursor = this.leftPos + this.imageWidth;
        int space = 4;
        if (config.showTakeAll) {
            int btnWidth = 72;
            int btnHeight = 20;
            cursor -= btnWidth;
            addRenderableWidget(
                    new Button.Builder(Component.translatable("button.take.all"), this::onTakeAllPress)
                            .bounds(cursor, this.topPos - btnHeight, btnWidth, btnHeight)
                            .build());
            cursor -= space;
        }
        if (config.showSmartPut) {
            int btnWidth = 72;
            int btnHeight = 20;
            cursor -= btnWidth;
            addRenderableWidget(
                    new Button.Builder(Component.translatable("button.smart.put"), this::onSmartPutPress)
                            .bounds(cursor, this.topPos - btnHeight, btnWidth, btnHeight)
                            .build());
            cursor -= space;
        }
        if (config.showDropAll) {
            int btnWidth = 72;
            int btnHeight = 20;
            cursor -= btnWidth;
            addRenderableWidget(
                    new Button.Builder(Component.translatable("button.drop.all"), this::onDropAllPress)
                            .bounds(cursor, this.topPos - btnHeight, btnWidth, btnHeight)
                            .build());
        }
    }

    @Inject(at = @At("TAIL"), method = "renderContents")
    private void onAfterRenderContents(GuiGraphics graphics, int slotIndex, int p_408205_, float p_408282_, CallbackInfo ci) {
        Events.ContainerScreenAfterRenderContents.trigger(
                new ContainerScreenRenderEvent(
                        (AbstractContainerScreen<?>) (Object) this,
                        graphics,
                        leftPos,
                        topPos,
                        imageWidth));
    }

    private void onTakeAllPress(Button button) {
        ContainerButtonsController.instance.takeAll(false);
    }

    private void onSmartPutPress(Button button) {
        ContainerButtonsController.instance.smartPut();
    }

    private void onDropAllPress(Button button) {
        ContainerButtonsController.instance.dropAll(false);
    }

    @Inject(at = @At("HEAD"), method = "getHoveredSlot", cancellable = true)
    private void onGetHoveredSlot(double x, double y, CallbackInfoReturnable<Slot> info) {
        ContainerScreenCalculateHoveredSlotEvent event = new ContainerScreenCalculateHoveredSlotEvent();
        if (Events.ContainerCalculateHoveredSlot.trigger(event)) {
            info.setReturnValue(event.getSlot());
        }
    }
}