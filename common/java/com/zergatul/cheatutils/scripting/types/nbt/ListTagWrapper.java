package com.zergatul.cheatutils.scripting.types.nbt;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.IndexGetter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.nbt.ListTag;

@CustomType(name = "ListTag")
public class ListTagWrapper extends TagWrapper {

    private final ListTag inner;

    ListTagWrapper(ListTag tag) {
        this.inner = tag;
    }

    @Getter(name = "size")
    public int getSize() {
        return inner.size();
    }

    @IndexGetter
    public TagWrapper indexer(int index) {
        return TagWrapper.from(inner.get(index));
    }

    @Override
    public String toString() {
        return inner.toString();
    }
}
