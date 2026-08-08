package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.scripting.MethodDescription;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.LiteralContents;

@SuppressWarnings("unused")
public class UIApi {

    private final Minecraft mc = Minecraft.getInstance();

    public boolean isDebugScreenEnabled() {
        return mc.options.renderDebug;
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void systemMessage(String text) {
        showMessage(constructMessage(text), false);
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void overlayMessage(String text) {
        showMessage(constructMessage(text), true);
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void systemMessage(String color, String text) {
        showMessage(constructMessage(color, text), false);
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void overlayMessage(String color, String text) {
        showMessage(constructMessage(color, text), true);
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void systemMessage(String color1, String text1, String color2, String text2) {
        showMessage(constructMessage(new String[] { color1, text1, "#FFFFFF", " ", color2, text2 }), false);
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void overlayMessage(String color1, String text1, String color2, String text2) {
        showMessage(constructMessage(new String[] { color1, text1, "#FFFFFF", " ", color2, text2 }), true);
    }

    @MethodDescription("Array length must be divisible by 2. Example: [color1, text1, color2, text2]")
    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void systemMessage(String[] parameters) {
        showMessage(constructMessage(parameters), false);
    }

    @MethodDescription("Array length must be divisible by 2. Example: [color1, text1, color2, text2]")
    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void overlayMessage(String[] parameters) {
        showMessage(constructMessage(parameters), true);
    }

    private MutableComponent constructMessage(String text) {
        return MutableComponent.create(new LiteralContents(text));
    }

    private MutableComponent constructMessage(String color, String text) {
        MutableComponent component = constructMessage(text);
        Integer value = ColorUtils.parseColor(color);
        return value == null ? component : component.withStyle(Style.EMPTY.withColor(value));
    }

    private MutableComponent constructMessage(String[] parameters) {
        MutableComponent component = constructMessage("");
        if (parameters.length == 0 || parameters.length % 2 != 0) {
            return component;
        }
        for (int i = 0; i < parameters.length; i += 2) {
            component.append(constructMessage(parameters[i], parameters[i + 1]));
        }
        return component;
    }

    private void showMessage(MutableComponent message, boolean overlay) {
        mc.getChatListener().handleSystemMessage(message, overlay);
    }
}