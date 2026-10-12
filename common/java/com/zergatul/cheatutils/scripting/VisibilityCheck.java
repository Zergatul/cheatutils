package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.utilities.Core;

import java.lang.reflect.Method;
import java.util.Arrays;

public class VisibilityCheck {

    public static boolean isOk(Method method, ApiType[] types) {
        if (method.isAnnotationPresent(HiddenMethod.class)) {
            return false;
        }

        boolean isAdvancedApi =
                method.getDeclaringClass().isAnnotationPresent(AdvancedApi.class) ||
                method.isAnnotationPresent(AdvancedApi.class);
        if (isAdvancedApi && !Core.INSTANCE.getSettings().get().advancedScripting) {
            return false;
        }

        ApiVisibility visibility = method.getAnnotation(ApiVisibility.class);
        if (visibility == null) {
            return true;
        }

        return Arrays.stream(visibility.value()).anyMatch(t1 -> Arrays.stream(types).anyMatch(t2 -> t1 == t2));
    }
}