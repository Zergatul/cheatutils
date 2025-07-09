package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.IndexGetter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.nbt.CompoundTag;

@CustomType(name = "CompoundTag")
public class CompoundTagWrapper extends TagWrapper {

    private final CompoundTag inner;

    public CompoundTagWrapper(CompoundTag tag) {
        this.inner = tag;
    }

    @Getter(name = "size")
    public int getSize() {
        return inner.size();
    }

    @IndexGetter
    public TagWrapper indexer(String key) {
        return get(key);
    }

    public boolean contains(String key) {
        return inner.contains(key);
    }

    public TagWrapper get(String key) {
        return TagWrapper.from(inner.get(key));
    }

    public String toString() {
        return inner.toString();
    }
}