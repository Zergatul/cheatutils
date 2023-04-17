package com.zergatul.cheatutils.scripting.api.modules;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.mixins.common.accessors.InputConstantsKeyAccessor;
import com.zergatul.cheatutils.scripting.api.HelpText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InputApi {

    private static final Minecraft mc = Minecraft.getInstance();

    private final Map<String, InputConstants.Key> keyMap = new HashMap<>();

    public InputApi() {
        for (InputConstants.Key key: InputConstantsKeyAccessor.getNameMap().values()) {
            StringBuilder sb = new StringBuilder();
            key.getDisplayName().visit(cc -> {
                sb.append(cc);
                return Optional.empty();
            });
            keyMap.put(sb.toString(), key);
        }
    }

    public boolean isShiftDown() {
        return Screen.hasShiftDown();
    }

    public boolean isControlDown() {
        return Screen.hasControlDown();
    }

    public boolean isAltDown() {
        return Screen.hasAltDown();
    }

    @HelpText("Use exactly the same key names you see in Key Binds screen.")
    public boolean isKeyDown(String key) {
        if (!mc.isWindowActive()) {
            return false;
        }

        InputConstants.Key inputKey = keyMap.get(key);
        if (inputKey == null) {
            return false;
        }
        if (inputKey.getType() == InputConstants.Type.KEYSYM) {
            return InputConstants.isKeyDown(mc.getWindow().getWindow(), inputKey.getValue());
        }

        return false;
    }
}