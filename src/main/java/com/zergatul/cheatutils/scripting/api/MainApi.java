package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Comparator;
import java.util.Locale;
import java.util.stream.StreamSupport;

public class MainApi {

    public void toggleEsp() {
        ConfigStore.instance.getConfig().esp = !ConfigStore.instance.getConfig().esp;
        ConfigStore.instance.requestWrite();
    }

    public void chat(String text) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Minecraft.getInstance().player.chat(text);
        }
    }

    public void systemMessage(String text) {
        Minecraft.getInstance().gui.handleChat(ChatType.SYSTEM, new TextComponent(text), Util.NIL_UUID);
    }

    public void systemMessage(String color, String text) {
        Integer colorInt = parseColor(color);
        MutableComponent component = new TextComponent(text);
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        Minecraft.getInstance().gui.handleChat(ChatType.SYSTEM, component, Util.NIL_UUID);
    }

    public void systemMessage(String color1, String text1, String color2, String text2) {
        Integer color1Int = parseColor(color1);
        Integer color2Int = parseColor(color2);
        MutableComponent component1 = new TextComponent(text1);
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = new TextComponent(text2);
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        Minecraft.getInstance().gui.handleChat(ChatType.SYSTEM, component1.append(" ").append(component2), Util.NIL_UUID);
    }

    public void openTradingWithClosestVillager() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            Villager villager = StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false)
                    .filter(e -> Villager.class.isInstance(e))
                    .map(e -> (Villager) e)
                    .min(Comparator.comparingDouble(v -> mc.player.distanceToSqr(v)))
                    .orElse(null);
            if (villager == null) {
                return;
            }
            interactWithEntity(mc, villager);
        }
    }

    private void interactWithEntity(Minecraft mc, Entity entity) {
        mc.gameMode.interactAt(mc.player, entity, new EntityHitResult(entity), InteractionHand.MAIN_HAND);
        mc.gameMode.interact(mc.player, entity, InteractionHand.MAIN_HAND);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    private static Integer parseColor(String str) {
        if (str == null) {
            return null;
        }
        str = str.toLowerCase(Locale.ROOT);
        if (str.length() == 7) {
            if (str.charAt(0) != '#') {
                return null;
            }
            for (int i = 1; i < 7; i++) {
                char ch = str.charAt(i);
                if ('0' <= ch && ch <= '9') {
                    continue;
                }
                if ('a' <= ch && ch <= 'f') {
                    continue;
                }
                return null;
            }
            return Integer.parseInt(str.substring(1, 3), 16) << 16 | Integer.parseInt(str.substring(3, 5), 16) << 8 | Integer.parseInt(str.substring(5, 7), 16);
        }
        return null;
    }
}