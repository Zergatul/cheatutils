package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MainApi {

    public void toggleEsp() {
        ConfigStore.instance.getConfig().esp = !ConfigStore.instance.getConfig().esp;
        ConfigStore.instance.requestWrite();
    }

    public void chat(String text) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player != null) {
            Minecraft.getInstance().player.chatSigned(text, null);
        }
    }

    public void systemMessage(String text) {
        Minecraft.getInstance().getChatListener().handleSystemMessage(MutableComponent.create(new LiteralContents(text)), false);
    }

    public void systemMessage(String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        MutableComponent component = MutableComponent.create(new LiteralContents(text));
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        Minecraft.getInstance().getChatListener().handleSystemMessage(component, false);
    }

    public void systemMessage(String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        MutableComponent component1 = MutableComponent.create(new LiteralContents(text1));
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = MutableComponent.create(new LiteralContents(text2));
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        Minecraft.getInstance().getChatListener().handleSystemMessage(component1.append(" ").append(component2), false);
    }

    public void openClosestChestBoat() {
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
    }
}