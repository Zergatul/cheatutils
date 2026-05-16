package com.zergatul.cheatutils.common;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public interface ModLoaderBridge {
    WrappedRegistry<Block> getBlockRegistry();
    WrappedRegistry<Item> getItemRegistry();
    WrappedRegistry<EntityType<?>> getEntityTypeRegistry();
    WrappedRegistry<MobEffect> getMobEffectRegistry();

    boolean isProduction();

    String getModLoaderName();
    String getModLoaderVersion();
    String getModVersion();
    int getModCount();
    boolean hasMod(String modId);
}