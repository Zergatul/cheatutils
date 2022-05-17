package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.client.gui.IIngameOverlay;
import net.minecraftforge.client.gui.OverlayRegistry;

import java.util.List;

public class ArmorOverlayController implements IIngameOverlay {

    public static final ArmorOverlayController instance = new ArmorOverlayController();

    private final Minecraft mc = Minecraft.getInstance();

    private ArmorOverlayController() {

    }

    public void register() {
        OverlayRegistry.registerOverlayTop("ArmorOverlay", this);
    }

    @Override
    public void render(ForgeIngameGui gui, PoseStack poseStack, float partialTick, int width, int height) {
        if (mc.player == null) {
            return;
        }
        if (!mc.options.hideGui && gui.shouldDrawSurvivalElements() && ConfigStore.instance.getConfig().armorOverlayConfig.enabled) {
            gui.setupOverlayRenderState(true, false);
            int left = width / 2 + 28;
            int top = height - gui.right_height - 6;
            gui.right_height += 24;

            List<ItemStack> armor = mc.player.getInventory().armor;
            renderItem(armor.get(3), left, top);
            left += 16;
            renderItem(armor.get(2), left, top);
            left += 16;
            renderItem(armor.get(1), left, top);
            left += 16;
            renderItem(armor.get(0), left, top);
        }
    }

    private void renderItem(ItemStack itemStack, int x, int y) {
        mc.getItemRenderer().renderAndDecorateItem(mc.player, itemStack, x, y, 123);
        mc.getItemRenderer().renderGuiItemDecorations(mc.font, itemStack, x, y, null);
    }
}
