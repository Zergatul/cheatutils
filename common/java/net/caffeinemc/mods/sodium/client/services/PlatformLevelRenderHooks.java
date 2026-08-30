package net.caffeinemc.mods.sodium.client.services;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.client.world.LevelSlice;
import net.minecraft.client.renderer.RenderType;

import java.util.List;
import java.util.function.Function;

public interface PlatformLevelRenderHooks {
    PlatformLevelRenderHooks INSTANCE = null;

    void runChunkMeshAppenders(List<?> renderers, Function<RenderType, VertexConsumer> typeToConsumer, LevelSlice slice);
}