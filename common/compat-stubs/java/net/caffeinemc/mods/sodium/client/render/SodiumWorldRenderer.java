package net.caffeinemc.mods.sodium.client.render;

import net.caffeinemc.mods.sodium.client.util.FogParameters;
import net.minecraft.client.Camera;
import org.joml.Matrix4f;

public class SodiumWorldRenderer {
    public void setupTerrain(
            Camera camera,
            Object viewport,
            FogParameters fogParameters,
            boolean useOcclusionCulling,
            boolean updateChunksImmediately,
            Matrix4f cullMatrix
    ) {}
}