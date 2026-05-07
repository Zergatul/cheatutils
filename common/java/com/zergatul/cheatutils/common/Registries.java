package com.zergatul.cheatutils.common;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class Registries {
    public static final WrappedRegistry<Block> BLOCKS = ModLoaderBridgeInstance.get().getBlockRegistry();
    public static final WrappedRegistry<Item> ITEMS = ModLoaderBridgeInstance.get().getItemRegistry();
    public static final WrappedRegistry<EntityType<?>> ENTITY_TYPES = ModLoaderBridgeInstance.get().getEntityTypeRegistry();
    public static final WrappedRegistry<MobEffect> MOB_EFFECTS = ModLoaderBridgeInstance.get().getMobEffectRegistry();
}