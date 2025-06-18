package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ContainerRenderLabelsEvent;
import com.zergatul.cheatutils.common.events.ContainerScreenCalculateHoveredSlotEvent;
import com.zergatul.cheatutils.common.events.PreRenderTooltipEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.ShulkerTooltipConfig;
import com.zergatul.cheatutils.utils.ItemUtils;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix3x2fStack;
import org.joml.Vector2ic;

public class ShulkerTooltip {

    public static ShulkerTooltip instance = new ShulkerTooltip();

    private static final ResourceLocation CONTAINER_TEXTURE = ResourceLocation.parse("textures/gui/container/shulker_box.png");
    private static final int ImageWidth = 176;
    //private static final int ImageHeight = 166;

    private static final ResourceLocation SLOT_HIGHLIGHT_BACK_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_back");
    private static final ResourceLocation SLOT_HIGHLIGHT_FRONT_SPRITE = ResourceLocation.withDefaultNamespace("container/slot_highlight_front");

    private final Minecraft mc = Minecraft.getInstance();
    private boolean locked = false;
    private boolean allowTooltip = false;
    private ItemStack lockedStack;
    private int lockedX, lockedY;
    private PreRenderTooltipEvent currentEvent;
    private boolean renderAfter;
    private boolean renderToTheLeft;

    private ShulkerTooltip() {
        Events.PreRenderTooltip.add(this::onPreRenderTooltip);
        Events.TooltipPositioned.add(this::onTooltipPositioned);
        Events.PostRenderTooltip.add(this::onPostRenderTooltip);
        Events.ContainerScreenAfterRenderContents.add(this::onContainerRenderContents);
        Events.ContainerCalculateHoveredSlot.add(this::onCalculateHoveredSlot);
    }

    private void onPreRenderTooltip(PreRenderTooltipEvent event) {
        currentEvent = event;
        renderAfter = false;

        if (locked && !allowTooltip) {
            event.cancel();
            return;
        }

        if (allowTooltip) {
            allowTooltip = false;
            return;
        }

        ShulkerTooltipConfig config = ConfigStore.instance.getConfig().shulkerTooltipConfig;
        if (!config.enabled) {
            clearLocked();
            return;
        }

        if (!ItemUtils.isShulkerBox(event.getItemStack())) {
            clearLocked();
            return;
        }

        if (config.showOriginal) {
            renderAfter = true;
        } else {
            event.cancel();
            renderShulkerTooltip();
        }
    }

    private void onTooltipPositioned(Vector2ic position) {
        if (renderAfter) {
            // render shulker tooltip on the opposite side from vanilla tooltip
            renderToTheLeft = position.x() > currentEvent.getX();
        }
    }

    private void onPostRenderTooltip() {
        if (renderAfter) {
            renderShulkerTooltip();
        }
    }

    private void onContainerRenderContents(ContainerRenderLabelsEvent event) {
        if (locked) {
            if (Screen.hasControlDown()) {
                Matrix3x2fStack poseStack = event.getGuiGraphics().pose();

                poseStack.pushMatrix();
                poseStack.identity();

                int x = lockedX;
                int y = lockedY;
                int mx = event.getMouseX();
                int my = event.getMouseY();
                int hovered = getHoveredSlot(x, y, mx, my);

                renderShulkerInventory(event.getGuiGraphics(), lockedStack, x, y, hovered);
                renderTooltip(event.getGuiGraphics(), x, y, mx, my, hovered);

                poseStack.popMatrix();
            } else {
                clearLocked();
            }
        }
    }

    private void onCalculateHoveredSlot(ContainerScreenCalculateHoveredSlotEvent event) {
        if (locked) {
            event.setSlot(null);
        }
    }

    private void renderShulkerTooltip() {
        // 12 pixels margin from DefaultTooltipPositioner and 4 pixels are vanilla border
        int x, y;
        if (renderAfter) {
            // if we render together with vanilla tooltip there is no choice to render left or right
            if (renderToTheLeft) {
                x = currentEvent.getX() - ImageWidth - 8;
                y = currentEvent.getY() - 16;
            } else {
                x = currentEvent.getX() + 8;
                y = currentEvent.getY() - 16;
            }
        } else {
            x = currentEvent.getX() - ImageWidth - 8;
            y = currentEvent.getY() - 16;
            if (x < 0) {
                // show to the right
                x = currentEvent.getX() + 8;
            }
        }

        if (Screen.hasControlDown()) {
            locked = true;
            lockedStack = currentEvent.getItemStack();
            lockedX = x;
            lockedY = y;
        }

        renderShulkerInventory(currentEvent.getGraphics(), currentEvent.getItemStack(), x, y, -1);
    }

    private void renderShulkerInventory(GuiGraphics graphics, ItemStack itemStack, int x, int y, int hovered) {
        graphics.nextStratum();

        graphics.blit(
                RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE,
                x, y,
                0, 0,
                ImageWidth, 6,
                256, 256);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE,
                x, y + 6,
                0, 14,
                ImageWidth, 60,
                256, 256);
        graphics.blit(
                RenderPipelines.GUI_TEXTURED, CONTAINER_TEXTURE,
                x, y + 66,
                0, 160,
                ImageWidth, 6,
                256, 256);

        if (hovered >= 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_BACK_SPRITE, x + 4 + 18 * (hovered % 9), y + 6 + 18 * (hovered / 9), 24, 24);
        }

        NonNullList<ItemStack> content = ItemUtils.getShulkerContent(itemStack);
        for (int i = 0; i < content.size(); i++) {
            ItemStack slot = content.get(i);
            int slotX = i % 9;
            int slotY = i / 9;
            if (!slot.isEmpty()) {
                renderSlot(graphics, slot, x + 8 + 18 * slotX, y + 10 + 18 * slotY);
            }
        }

        if (hovered >= 0) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, SLOT_HIGHLIGHT_FRONT_SPRITE, x + 4 + 18 * (hovered % 9), y + 6 + 18 * (hovered / 9), 24, 24);
        }
    }

    private void renderTooltip(GuiGraphics graphics, int x, int y, int mouseX, int mouseY, int hovered) {
        if (hovered < 0) {
            return;
        }

        NonNullList<ItemStack> content = ItemUtils.getShulkerContent(lockedStack);
        ItemStack slot = content.get(hovered);
        if (slot.isEmpty()) {
            return;
        }

        graphics.nextStratum();
        allowTooltip = true;
        graphics.renderTooltip(
                mc.font,
                Screen.getTooltipFromItem(mc, slot).stream()
                        .map(Component::getVisualOrderText)
                        .map(ClientTooltipComponent::create)
                        .collect(Util.toMutableList()),
                mouseX, mouseY,
                DefaultTooltipPositioner.INSTANCE,
                slot.get(DataComponents.TOOLTIP_STYLE));
    }

    private void renderSlot(GuiGraphics graphics, ItemStack itemStack, int x, int y) {
        graphics.renderFakeItem(itemStack, x, y);
        graphics.renderItemDecorations(mc.font, itemStack, x, y);
    }

    private int getHoveredSlot(int x, int y, int mouseX, int mouseY) {
        for (int i = 0; i < 27; i++) {
            int slotX = i % 9;
            int slotY = i / 9;
            if (x + 8 + 18 * slotX <= mouseX && mouseX < x + 8 + 18 * slotX + 16) {
                if (y + 10 + 18 * slotY <= mouseY && mouseY < y + 10 + 18 * slotY + 16) {
                    return i;
                }
            }
        }
        return -1;
    }

    private void clearLocked() {
        locked = false;
        lockedStack = null;
    }
}