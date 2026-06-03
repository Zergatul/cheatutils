package com.zergatul.cheatutils.scripting;

import com.zergatul.scripting.compiler.MethodUsagePolicy;

import java.lang.reflect.Method;
import java.util.Optional;

public class CurseForgeMethodUsagePolicy implements MethodUsagePolicy {

    @Override
    public Optional<String> validate(Method method) {
        if (method.isAnnotationPresent(CurseForgeRestricted.class)) {
            return Optional.of("This method is disabled in CurseForge builds of the mod. Get full version from the GitHub.");
        } else {
            return Optional.empty();
        }
    }
}