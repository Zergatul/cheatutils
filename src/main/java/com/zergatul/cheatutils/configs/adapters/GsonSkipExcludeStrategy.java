package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public class GsonSkipExcludeStrategy implements ExclusionStrategy {

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return f.getAnnotation(GsonSkip.class) != null;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return false;
    }
}