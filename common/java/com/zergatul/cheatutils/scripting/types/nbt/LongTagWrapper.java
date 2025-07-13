package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.nbt.LongTag;

@CustomType(name = "LongTag")
public class LongTagWrapper extends TagWrapper {

    private final LongTag inner;

    LongTagWrapper(LongTag tag) {
        this.inner = tag;
    }

    @Getter(name = "value")
    public long getValue() {
        return inner.value();
    }

    @Override
    public int getIntOr(int defaultValue) {
        return inner.intValue();
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