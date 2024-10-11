package com.zergatul.cheatutils.scripting.types;

import com.zergatul.cheatutils.common.Registries;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.world.item.Item;

@CustomType(name = "Item")
public class ItemWrapper {

    private final Item inner;

    public ItemWrapper(Item item) {
        this.inner = item;
    }

    @Getter(name = "id")
    public String getId() {
        return Registries.ITEMS.getKey(inner).toString();
    }

    @Getter(name = "name")
    public String getName() {
        return inner.getName(inner.getDefaultInstance()).getString();
    }
}