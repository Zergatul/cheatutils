package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.nbt.FloatTag;

@CustomType(name = "FloatTag")
public class FloatTagWrapper extends TagWrapper {

    private final FloatTag inner;

    FloatTagWrapper(FloatTag tag) {
        this.inner = tag;
    }

    @Getter(name = "value")
    public double getValue() {
        return inner.value();
    }

    @Override
    public int getIntOr(int defaultValue) {
        return inner.intValue();
    }

    @Override
    public long getLongOr(long defaultValue) {
        return inner.longValue();
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