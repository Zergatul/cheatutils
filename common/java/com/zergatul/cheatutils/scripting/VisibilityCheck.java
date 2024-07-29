package com.zergatul.cheatutils.scripting;

import java.lang.reflect.Method;
import java.util.Arrays;

public class VisibilityCheck {

    public static ApiType[] getTypes(String scriptType) {
        return switch (scriptType) {
            case "overlay" -> new ApiType[] { ApiType.OVERLAY };
            case "keybindings" -> new ApiType[] { ApiType.ACTION, ApiType.UPDATE };
            case "block-automation" -> new ApiType[] { ApiType.CURRENT_BLOCK, ApiType.BLOCK_AUTOMATION};
            case "villager-roller" -> new ApiType[] { ApiType.VILLAGER_ROLLER, ApiType.LOGGING };
            case "events" -> new ApiType[] { ApiType.ACTION, ApiType.UPDATE, ApiType.EVENTS };
            case "entity-esp" -> new ApiType[] { ApiType.CURRENT_ENTITY_ESP };
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