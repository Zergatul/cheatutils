package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.utils.ColorUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.util.Util;
import net.minecraft.util.text.ChatType;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;

public class MainApi {

    public void toggleEsp() {
        ConfigStore.instance.getConfig().esp = !ConfigStore.instance.getConfig().esp;
        ConfigStore.instance.requestWrite();
    }

    public void chat(String text) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (player != null) {
            Minecraft.getInstance().player.chat(text);
        }
    }

    public void systemMessage(String text) {
        Minecraft.getInstance().gui.handleChat(ChatType.SYSTEM, new StringTextComponent(text), Util.NIL_UUID);
    }

    public void systemMessage(String color, String text) {
        Integer colorInt = ColorUtils.parseColor(color);
        IFormattableTextComponent component = new StringTextComponent(text);
        if (colorInt != null) {
            component = component.withStyle(Style.EMPTY.withColor(net.minecraft.util.text.Color.fromRgb(colorInt)));
        }
        Minecraft.getInstance().gui.handleChat(ChatType.SYSTEM, component, Util.NIL_UUID);
    }

    public void systemMessage(String color1, String text1, String color2, String text2) {
        Integer color1Int = ColorUtils.parseColor(color1);
        Integer color2Int = ColorUtils.parseColor(color2);
        IFormattableTextComponent component1 = new StringTextComponent(text1);
        if (color1Int != null) {
            component1 = component1.withStyle(Style.EMPTY.withColor(net.minecraft.util.text.Color.fromRgb(color1Int)));
        }
        IFormattableTextComponent component2 = new StringTextComponent(text2);
        if (color2Int != null) {
            component2 = component2.withStyle(Style.EMPTY.withColor(net.minecraft.util.text.Color.fromRgb(color2Int)));
        }
        Minecraft.getInstance().gui.handleChat(ChatType.SYSTEM, component1.append(" ").append(component2), Util.NIL_UUID);
    }
}