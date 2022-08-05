package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.configs.ConfigStore;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ChestBoat;
import net.minecraft.world.phys.EntityHitResult;

import java.util.Locale;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class MainApi {

    private final ChatType chatType;

    public MainApi() {
        Registry<ChatType> registry = RegistryAccess.BUILTIN.get().registryOrThrow(Registry.CHAT_TYPE_REGISTRY);
        chatType = registry.get(ChatType.SYSTEM);
    }

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
        Minecraft.getInstance().gui.handleSystemChat(chatType, MutableComponent.create(new LiteralContents(text)));
    }

    public void systemMessage(String color, String text) {
        Integer colorInt = parseColor(color);
        MutableComponent component = MutableComponent.create(new LiteralContents(text));
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(colorInt));
        }
        Minecraft.getInstance().gui.handleSystemChat(chatType, component);
    }

    public void systemMessage(String color1, String text1, String color2, String text2) {
        Integer color1Int = parseColor(color1);
        Integer color2Int = parseColor(color2);
        MutableComponent component1 = MutableComponent.create(new LiteralContents(text1));
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(color1Int));
        }
        MutableComponent component2 = MutableComponent.create(new LiteralContents(text2));
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(color2Int));
        }
        Minecraft.getInstance().gui.handleSystemChat(chatType, component1.append(" ").append(component2));
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