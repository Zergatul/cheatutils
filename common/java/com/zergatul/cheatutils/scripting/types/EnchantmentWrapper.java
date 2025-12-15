package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

@CustomType(name = "Enchantment")
public class EnchantmentWrapper {

    private final Identifier location;
    private final Enchantment enchantment;
    private final int level;

    public EnchantmentWrapper(Identifier location, Enchantment enchantment, int level) {
        this.location = location;
        this.enchantment = enchantment;
        this.level = level;
    }

    @Getter(name = "id")
    public String getId() {
        return location.toString();
    }

    @Getter(name = "level")
    public int getLevel() {
        return level;
    }

    @Getter(name = "name")
    public String getName() {
        return enchantment.description().getString();
    }

    public static EnchantmentWrapper[] of(ItemEnchantments enchantments) {
        EnchantmentWrapper[] result = new EnchantmentWrapper[enchantments.size()];
        int index = 0;
        for (Holder<Enchantment> holder : enchantments.keySet()) {
            int level = enchantments.getLevel(holder);
            result[index++] = new EnchantmentWrapper(holder.unwrapKey().get().identifier(), holder.value(), level);
        }
        return result;
    }
}