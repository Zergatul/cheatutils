package com.zergatul.cheatutils.wrappers;

import com.zergatul.cheatutils.common.WrappedRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;

import java.util.Collection;

public class RegistriesWrapper {

    public static WrappedRegistry<Block> getBlocks() {
        return new ForgeWrappedRegistry<>(ForgeRegistries.BLOCKS);
    }

    public static WrappedRegistry<Item> getItems() {
        return new ForgeWrappedRegistry<>(ForgeRegistries.ITEMS);
    }

    public static WrappedRegistry<EntityType<?>> getEntityTypes() {
        return new ForgeWrappedRegistry<>(ForgeRegistries.ENTITY_TYPES);
    }

    public static WrappedRegistry<Enchantment> getEnchantments() {
        return new ForgeWrappedRegistry<>(ForgeRegistries.ENCHANTMENTS);
    }

    public static WrappedRegistry<MobEffect> getMobEffects() {
        return new ForgeWrappedRegistry<>(ForgeRegistries.MOB_EFFECTS);
    }

    private record ForgeWrappedRegistry<T>(IForgeRegistry<T> registry) implements WrappedRegistry<T> {

        @Override
        public ResourceLocation getKey(T value) {
            return registry.getKey(value);
        }

        @Override
        public T getValue(ResourceLocation id) {
            return registry.getValue(id);
        }

        @Override
        public Collection<T> getValues() {
            return registry.getValues();
        }
    }
}