package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;

import java.util.Locale;

public class MainApi {

    public void toggleEsp() {
        ConfigStore.instance.getConfig().esp = !ConfigStore.instance.getConfig().esp;
        ConfigStore.instance.requestWrite();
    }

    public void chat(String text) {
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player != null) {
            MinecraftClient.getInstance().player.sendChatMessage(text, null);
        }
    }

    public void systemMessage(String text) {
        MinecraftClient.getInstance().getMessageHandler().onGameMessage(MutableText.of(new LiteralTextContent(text)), false);
    }

    public void systemMessage(String color, String text) {
        Integer colorInt = parseColor(color);
        MutableText component = MutableText.of(new LiteralTextContent(text));
        if (colorInt != null) {
            component = component.setStyle(Style.EMPTY.withColor(colorInt));
        }
        MinecraftClient.getInstance().getMessageHandler().onGameMessage(component, false);
    }

    public void systemMessage(String color1, String text1, String color2, String text2) {
        Integer color1Int = parseColor(color1);
        Integer color2Int = parseColor(color2);
        MutableText component1 = MutableText.of(new LiteralTextContent(text1));
        if (color1Int != null) {
            component1 = component1.setStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableText component2 = MutableText.of(new LiteralTextContent(text2));
        if (color2Int != null) {
            component2 = component2.setStyle(Style.EMPTY.withColor(color2Int));
        }
        MinecraftClient.getInstance().getMessageHandler().onGameMessage(component1.append(" ").append(component2), false);
    }

    /*public void openClosestChestBoat() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null && mc.player != null) {
            Stream<ChestBoat> boats = StreamSupport
                    .stream(mc.level.entitiesForRendering().spliterator(), false)
                    .filter(e -> e instanceof ChestBoat)
                    .map(e -> (ChestBoat) e);
            double minDistance = Double.MAX_VALUE;
            ChestBoat target = null;
            for (ChestBoat boat: boats.toList()) {
                double d2 = mc.player.distanceToSqr(boat);
                if (d2 < minDistance) {
                    minDistance = d2;
                    target = boat;
                }
            }

            if (target == null) {
                return;
            }

            boolean oldShiftKeyDown = mc.player.input.shiftKeyDown;
            mc.player.input.shiftKeyDown = true;
            mc.gameMode.interactAt(mc.player, target, new EntityHitResult(target), InteractionHand.MAIN_HAND);
            mc.gameMode.interact(mc.player, target, InteractionHand.MAIN_HAND);
            mc.player.swing(InteractionHand.MAIN_HAND);
            mc.player.input.shiftKeyDown = oldShiftKeyDown;
        }
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
    }*/

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