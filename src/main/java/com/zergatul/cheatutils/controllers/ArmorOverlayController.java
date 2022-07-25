package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.List;

public class ArmorOverlayController implements IGuiOverlay {

    public static final ArmorOverlayController instance = new ArmorOverlayController();

    private final Minecraft mc = Minecraft.getInstance();

    private ArmorOverlayController() {

    }

    public void onRegister(RegisterGuiOverlaysEvent event) {
        event.registerAboveAll("armor", this);
    }

    @Override
    public void render(ForgeGui gui, PoseStack poseStack, float partialTick, int width, int height) {
        if (mc.player == null) {
            return;
        }
        if (!mc.options.hideGui && gui.shouldDrawSurvivalElements() && ConfigStore.instance.getConfig().armorOverlayConfig.enabled) {
            gui.setupOverlayRenderState(true, false);
            int left = width / 2 + 28;
            int top = height - gui.rightHeight - 6;
            gui.rightHeight += 24;

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
