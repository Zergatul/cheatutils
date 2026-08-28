package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.world.item.ItemStack;

@SuppressWarnings("unused")
@CustomType(name = "ItemStack")
public class ItemStackWrapper {

    private final ItemStack inner;

    public ItemStackWrapper(ItemStack inner) {
        this.inner = inner;
    }

    @Getter(name = "item")
    public ItemWrapper getItem() {
        return new ItemWrapper(inner.getItem());
    }

    @Getter(name = "count")
    public int getCount() {
        return inner.getCount();
    }

    @Getter(name = "isDamageable")
    public boolean getIsDamageable() {
        return inner.isDamageableItem();
    }

    @Getter(name = "durability")
    public int getDurability() {
        return inner.getMaxDamage() - inner.getDamageValue();
    }

    @Getter(name = "maxDurability")
    public int getMaxDurability() {
        return inner.getMaxDamage();
    }

    @Getter(name = "isEmpty")
    public boolean getIsEmpty() {
        return inner.isEmpty();
    }

    @Getter(name = "stackSize")
    public int getStackSize() {
        return inner.getMaxStackSize();
    }
}