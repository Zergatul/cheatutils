package com.zergatul.cheatutils.modules.visuals;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class ArmorOverlay {

    public static final ArmorOverlay instance = new ArmorOverlay();

    private final Minecraft mc = Minecraft.getInstance();

    private ArmorOverlay() {

    }

    public void render(GuiGraphics graphics, Player player, int left, int top) {
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.HEAD), left, top);
        left += 16;
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.CHEST), left, top);
        left += 16;
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.LEGS), left, top);
        left += 16;
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.FEET), left, top);
    }

    private void renderItem(GuiGraphics graphics, ItemStack itemStack, int left, int top) {
        graphics.renderItem(itemStack, left, top);
        graphics.renderItemDecorations(mc.font, itemStack, left, top);
    }
}