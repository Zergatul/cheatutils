package com.zergatul.cheatutils.scripting;

import java.lang.reflect.Method;
import java.util.Arrays;

public class VisibilityCheck {

    public static boolean isOk(Method method, ApiType[] types) {
        ApiVisibility visibility = method.getAnnotation(ApiVisibility.class);
        if (visibility == null) {
            return true;
        }

        return Arrays.stream(visibility.value()).anyMatch(t1 -> Arrays.stream(types).anyMatch(t2 -> t1 == t2));
    }
}