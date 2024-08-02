package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;

import static com.zergatul.cheatutils.utils.ComponentUtils.constructMessage;

public class UIApi {

    private final Minecraft mc = Minecraft.getInstance();

    public boolean isDebugScreenEnabled() {
        return mc.gui.getDebugOverlay().showDebugScreen();
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
        showMessage(constructMessage(color1, text1, color2, text2), false);
    }

    @ApiVisibility({ ApiType.ACTION, ApiType.LOGGING })
    public void overlayMessage(String color1, String text1, String color2, String text2) {
        showMessage(constructMessage(color1, text1, color2, text2), true);
    }

    private void showMessage(MutableComponent message, boolean overlay) {
        mc.getChatListener().handleSystemMessage(message, overlay);
    }
}