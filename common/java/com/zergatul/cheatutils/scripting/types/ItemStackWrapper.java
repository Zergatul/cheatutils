package com.zergatul.cheatutils.scripting.types;

import com.zergatul.cheatutils.scripting.types.nbt.CompoundTagWrapper;
import com.zergatul.scripting.Getter;
import com.zergatul.scripting.Lazy;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.storage.TagValueOutput;

import java.util.List;

@SuppressWarnings("unused")
@CustomType(name = "ItemStack")
public class ItemStackWrapper {

    private final ItemStack inner;
    private final Lazy<EnchantmentWrapper[]> enchantments;
    private final Lazy<String[]> tooltip;

    public ItemStackWrapper(ItemStack inner) {
        this.inner = inner;
        this.enchantments = new Lazy<>(this::getEnchantmentsInternal);
        this.tooltip = new Lazy<>(this::getTooltipInternal);
    }

    @Getter(name = "item")
    public ItemWrapper getItem() {
        return new ItemWrapper(inner.getItem());
    }

    @Getter(name = "count")
    public int getCount() {
        return inner.getCount();
    }

    @Getter(name = "enchantments")
    public EnchantmentWrapper[] getEnchantments() {
        return enchantments.value();
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

    @Getter(name = "tooltip")
    public String[] getTooltip() {
        return tooltip.value();
    }

    @Getter(name = "attributeModifiers")
    public AttributeModifier[] getAttributeModifiers() {
        ItemAttributeModifiers modifiers = inner.getComponents().get(DataComponents.ATTRIBUTE_MODIFIERS);
        if (modifiers == null) {
            return new AttributeModifier[0];
        }

        AttributeModifier[] result = new AttributeModifier[modifiers.modifiers().size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = new AttributeModifier(modifiers.modifiers().get(i));
        }
        return result;
    }

    @Getter(name = "nbt")
    public CompoundTagWrapper getNbt() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return new CompoundTagWrapper(new CompoundTag());
        }

        TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, mc.level.registryAccess());
        output.store(ItemStack.MAP_CODEC, inner);
        return new CompoundTagWrapper(output.buildResult());
    }

    public boolean hasEnchantment(String id) {
        Identifier location = Identifier.tryParse(id);
        if (location == null) {
            return false;
        }
        return getItemEnchantments().keySet().stream().anyMatch(holder -> holder.unwrapKey().get().identifier().equals(location));
    }

    private EnchantmentWrapper[] getEnchantmentsInternal() {
        return EnchantmentWrapper.of(getItemEnchantments());
    }

    private ItemEnchantments getItemEnchantments() {
        if (inner.is(Items.ENCHANTED_BOOK)) {
            return inner.getOrDefault(DataComponents.STORED_ENCHANTMENTS, ItemEnchantments.EMPTY);
        } else {
            return inner.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        }
    }

    private String[] getTooltipInternal() {
        List<Component> components = Screen.getTooltipFromItem(Minecraft.getInstance(), inner);
        String[] result = new String[components.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = components.get(i).getString();
        }
        return result;
    }
}