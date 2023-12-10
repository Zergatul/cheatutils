package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;

import java.lang.reflect.Method;
import java.util.Arrays;

public class VisibilityCheck {

    public static ApiType[] getTypes(String scriptType) {
        return switch (scriptType) {
            case "overlay" -> new ApiType[] { ApiType.OVERLAY };
            case "handle-keybindings" -> new ApiType[] { ApiType.ACTION, ApiType.UPDATE };
            case "block-placer" -> new ApiType[] { ApiType.CURRENT_BLOCK, ApiType.BLOCK_PLACER };
            case "auto-disconnect" -> new ApiType[] { ApiType.ACTION, ApiType.DISCONNECT };
            case "villager-roller" -> new ApiType[] { ApiType.VILLAGER_ROLLER, ApiType.LOGGING };
            case "events" -> new ApiType[] { ApiType.ACTION, ApiType.UPDATE, ApiType.EVENTS, ApiType.DISCONNECT };
            default -> null;
        };
    }

    public static boolean isOk(Method method, ApiType[] types) {
        ApiVisibility visibility = method.getAnnotation(ApiVisibility.class);
        if (visibility == null) {
            return true;
        }

        return Arrays.stream(visibility.value()).anyMatch(t1 -> Arrays.stream(types).anyMatch(t2 -> t1 == t2));
    }
}