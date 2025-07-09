package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.nbt.StringTag;

@CustomType(name = "StringTag")
public class StringTagWrapper extends TagWrapper {

    private final StringTag inner;

    public StringTagWrapper(StringTag tag) {
        this.inner = tag;
    }

    @Getter(name = "value")
    public String getValue() {
        return inner.value();
    }

    @Override
    public String getStringOr(String defaultValue) {
        return inner.value();
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}