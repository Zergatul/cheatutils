package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.cheatutils.scripting.types.UUIDWrapper;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.IndexGetter;
import com.zergatul.scripting.MethodDescription;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.IntArrayTag;

@CustomType(name = "IntArrayTag")
public class IntArrayTagWrapper extends TagWrapper {

    private final IntArrayTag inner;

    IntArrayTagWrapper(IntArrayTag tag) {
        this.inner = tag;
    }

    @Getter(name = "size")
    public int getSize() {
        return inner.size();
    }

    @IndexGetter
    public int indexer(int index) {
        return inner.getAsIntArray()[index];
    }

    @MethodDescription("Requires an int array of exactly 4 elements.")
    public UUIDWrapper asUUID() {
        if (inner.size() != 4) {
            throw new IllegalArgumentException("Expected int-array of length 4, got " + inner.getAsIntArray().length + ".");
        }
        return new UUIDWrapper(UUIDUtil.uuidFromIntArray(inner.getAsIntArray()));
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}