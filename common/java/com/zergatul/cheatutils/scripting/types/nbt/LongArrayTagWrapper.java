package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.IndexGetter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.nbt.LongArrayTag;

@CustomType(name = "LongArrayTag")
public class LongArrayTagWrapper extends TagWrapper {

    private final LongArrayTag inner;

    LongArrayTagWrapper(LongArrayTag tag) {
        this.inner = tag;
    }

    @Getter(name = "size")
    public int getSize() {
        return inner.size();
    }

    @IndexGetter
    public long indexer(int index) {
        return inner.getAsLongArray()[index];
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}