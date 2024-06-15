package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ContainerRenderLabelsEvent;
import com.zergatul.cheatutils.common.events.PreRenderTooltipEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.mixins.common.accessors.AbstractContainerScreenAccessor;
import com.zergatul.cheatutils.render.ItemRenderHelper;
import com.zergatul.cheatutils.utils.ItemUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;

// TODO: optimize
public class ShulkerTooltipController {

    public static ShulkerTooltipController instance = new ShulkerTooltipController();

    private static final ResourceLocation CONTAINER_TEXTURE = ResourceLocation.parse("textures/gui/container/shulker_box.png");
    private static final int ImageWidth = 176;
    private static final int ImageHeight = 166;
    private static final int TranslateZ = 250;

    private boolean locked = false;
    private boolean allowTooltip = false;
    private ItemStack lockedStack;
    private Matrix4f lockedPose;
    private int lockedX, lockedY;

    private ShulkerTooltipController() {
        Events.PreRenderTooltip.add(this::onPreRenderTooltip);
        Events.ContainerRenderLabels.add(this::onContainerRenderLabels);
    }

    private void onPreRenderTooltip(PreRenderTooltipEvent event) {
        if (locked && !allowTooltip) {
            event.cancel();
            return;
        }

        if (allowTooltip) {
            allowTooltip = false;
            return;
        }

        if (!ConfigStore.instance.getConfig().shulkerTooltipConfig.enabled) {
            clearLocked();
            return;
        }

        if (!ItemUtils.isShulkerBox(event.getItemStack())) {
            clearLocked();
            return;
        }

        event.cancel();

        PoseStack poseStack = event.getGraphics().pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, TranslateZ);
        RenderSystem.applyModelViewMatrix();

        int x, y;
        x = event.getX() - ImageWidth - 16;
        y = event.getY() - 4;
        if (x < 0) {
            // show to the right
            x = event.getX() + 16;
        }

        if (Screen.hasControlDown()) {
            locked = true;
            lockedPose = poseStack.last().pose();
            lockedStack = event.getItemStack();
            lockedX = x;
            lockedY = y;
        }

        renderShulkerInventory(event.getGraphics(), event.getItemStack(), poseStack.last().pose(), x, y);

        poseStack.popPose();
        RenderSystem.applyModelViewMatrix();
    }

    private void onContainerRenderLabels(ContainerRenderLabelsEvent event) {
        if (locked) {
            if (Screen.hasControlDown()) {
                PoseStack poseStack = event.getGuiGraphics().pose();

                poseStack.pushPose();
                poseStack.setIdentity();
                poseStack.mulPose(lockedPose);
                RenderSystem.applyModelViewMatrix();

                int x = lockedX;
                int y = lockedY;

                PoseStack.Pose pose = poseStack.last();
                renderShulkerInventory(event.getGuiGraphics(), lockedStack, pose.pose(), x, y);

                int mx = event.getMouseX();
                int my = event.getMouseY();
                renderTooltip(event.getGuiGraphics(), x, y, mx, my);

                poseStack.popPose();
                RenderSystem.applyModelViewMatrix();
            } else {
                clearLocked();
            }
        }
    }

    private int globalToScreenX(int x, AbstractContainerScreen<?> screen) {
        return x - Minecraft.getInstance().getWindow().getGuiScaledWidth() / 2 + ((AbstractContainerScreenAccessor) screen).getWidth_CU() / 2;
    }

    private int globalToScreenY(int y, AbstractContainerScreen<?> screen) {
        return y - Minecraft.getInstance().getWindow().getGuiScaledHeight() / 2 + ((AbstractContainerScreenAccessor) screen).getHeight_CU() / 2;
    }

    private void renderShulkerInventory(GuiGraphics graphics, ItemStack itemStack, Matrix4f matrix, int x, int y) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, CONTAINER_TEXTURE);

        drawTexture(matrix, x, y, ImageWidth, 6, 100, 0, 0, ImageWidth, 6, 256, 256);
        drawTexture(matrix, x, y + 6, ImageWidth, 60, 100, 0, 14, ImageWidth, 60, 256, 256);
        drawTexture(matrix, x, y + 66, ImageWidth, 6, 100, 0, 160, ImageWidth, 6, 256, 256);

        NonNullList<ItemStack> content = ItemUtils.getShulkerContent(itemStack);
        for (int i = 0; i < content.size(); i++) {
            ItemStack slot = content.get(i);
            int slotX = i % 9;
            int slotY = i / 9;
            if (!slot.isEmpty()) {
                renderSlot(graphics, slot, x + 8 + 18 * slotX, y + 10 + 18 * slotY);
            }
        }
    }

    private void renderTooltip(GuiGraphics graphics, int x, int y, int mouseX, int mouseY) {
        NonNullList<ItemStack> content = ItemUtils.getShulkerContent(lockedStack);
        for (int i = 0; i < content.size(); i++) {
            ItemStack slot = content.get(i);
            int slotX = i % 9;
            int slotY = i / 9;
            if (!slot.isEmpty()) {
                if (x + 8 + 18 * slotX <= mouseX && mouseX < x + 8 + 18 * slotX + 16) {
                    if (y + 10 + 18 * slotY <= mouseY && mouseY < y + 10 + 18 * slotY + 16) {
                        allowTooltip = true;
                        graphics.renderTooltip(Minecraft.getInstance().font, slot, mouseX, mouseY);
                    }
                }
            }
        }
    }

    private void drawTexture(Matrix4f matrix, int x, int y, int width, int height, int z, int texX, int texY, int texWidth, int texHeight, int texSizeX, int texSizeY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.enableDepthTest();
        BufferBuilder bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(matrix, x, y, z).setUv(1F * texX / texSizeX, 1F * texY / texSizeY);
        bufferbuilder.addVertex(matrix, x, y + height, z).setUv(1F * texX / texSizeX, 1F * (texY + texHeight) / texSizeY);
        bufferbuilder.addVertex(matrix, x + width, y + height, z).setUv(1F * (texX + texWidth) / texSizeX, 1F * (texY + texHeight) / texSizeY);
        bufferbuilder.addVertex(matrix, x + width, y, z).setUv(1F * (texX + texWidth) / texSizeX, 1F * texY / texSizeY);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    private void renderSlot(GuiGraphics graphics, ItemStack itemStack, int x, int y) {
        RenderSystem.enableDepthTest();
        ItemRenderHelper.renderItem(graphics, itemStack, x, y);
    }

    private void clearLocked() {
        locked = false;
        lockedPose = null;
        lockedStack = null;
    }
}