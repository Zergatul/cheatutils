package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.nbt.ByteTag;

@CustomType(name = "ByteTag")
public class ByteTagWrapper extends TagWrapper {

    private final ByteTag inner;

    public ByteTagWrapper(ByteTag tag) {
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