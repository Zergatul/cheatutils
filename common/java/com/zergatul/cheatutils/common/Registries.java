package com.zergatul.cheatutils.common;

import com.zergatul.cheatutils.wrappers.RegistriesWrapper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;

public class Registries {
    public static final WrappedRegistry<Block> BLOCKS = RegistriesWrapper.getBlocks();
    public static final WrappedRegistry<Item> ITEMS = RegistriesWrapper.getItems();
    public static final WrappedRegistry<EntityType<?>> ENTITY_TYPES = RegistriesWrapper.getEntityTypes();
    public static final WrappedRegistry<Enchantment> ENCHANTMENTS = RegistriesWrapper.getEnchantments();
}