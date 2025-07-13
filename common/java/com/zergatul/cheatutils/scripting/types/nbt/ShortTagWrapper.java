package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.nbt.ShortTag;

@CustomType(name = "ShortTag")
public class ShortTagWrapper extends TagWrapper {

    private final ShortTag inner;

    ShortTagWrapper(ShortTag tag) {
        this.inner = tag;
    }

    @Getter(name = "value")
    public int getValue() {
        return inner.value();
    }

    @Override
    public int getIntOr(int defaultValue) {
        return inner.value();
    }

    @Override
    public long getLongOr(long defaultValue) {
        return inner.value();
    }

    @Override
    public double getFloatOr(double defaultValue) {
        return inner.value();
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}