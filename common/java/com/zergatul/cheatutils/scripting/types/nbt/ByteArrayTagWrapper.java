package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.IndexGetter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.nbt.ByteArrayTag;

@CustomType(name = "ByteArrayTag")
public class ByteArrayTagWrapper extends TagWrapper {

    private final ByteArrayTag inner;

    public ByteArrayTagWrapper(ByteArrayTag tag) {
        this.inner = tag;
    }

    @Getter(name = "size")
    public int getSize() {
        return inner.size();
    }

    @IndexGetter
    public int indexer(int index) {
        return inner.getAsByteArray()[index];
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}