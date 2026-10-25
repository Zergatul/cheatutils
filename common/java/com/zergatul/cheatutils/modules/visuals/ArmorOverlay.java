package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.configs.SettingHandle2;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.ArmorOverlayConfig;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ArmorOverlay {

    public static final ArmorOverlay INSTANCE = new ArmorOverlay();

    private final SettingHandle2<ArmorOverlayConfig> settings = Settings.get(ArmorOverlayConfig.class);
    private final Minecraft mc = Minecraft.getInstance();

    private ArmorOverlay() {
        WebApiRegistry.INSTANCE.register("armor-overlay", settings, ArmorOverlayConfig.class);
    }

    public SettingHandle2<ArmorOverlayConfig> getSettings() {
        return settings;
    }

    public void render(GuiGraphicsExtractor graphics, Player player, int left, int top) {
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.HEAD), left, top);
        left += 16;
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.CHEST), left, top);
        left += 16;
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.LEGS), left, top);
        left += 16;
        renderItem(graphics, player.getItemBySlot(EquipmentSlot.FEET), left, top);
    }

    private void renderItem(GuiGraphicsExtractor graphics, ItemStack itemStack, int left, int top) {
        graphics.item(itemStack, left, top);
        graphics.itemDecorations(mc.font, itemStack, left, top);
    }
}