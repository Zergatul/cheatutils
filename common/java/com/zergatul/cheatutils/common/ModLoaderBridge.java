package com.zergatul.cheatutils.common;

import com.zergatul.cheatutils.webui.BlockModelApi;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.List;

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
    List<String> getModsJars();

    default void extractQuads(SubmitNode submission, List<BlockModelApi.Quad> output) {}
}