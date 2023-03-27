package com.zergatul.cheatutils.scripting.api;

import java.lang.reflect.Method;
import java.util.Arrays;

public class VisibilityCheck {

    public static ApiType[] getTypes(String scriptType) {
        return switch (scriptType) {
            case "overlay" -> new ApiType[] { ApiType.OVERLAY };
            case "handle-keybindings" -> new ApiType[] { ApiType.ACTION, ApiType.UPDATE };
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