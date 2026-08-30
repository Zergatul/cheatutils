package net.caffeinemc.mods.sodium.api.vertex.buffer;

import com.mojang.blaze3d.vertex.VertexFormat;
import org.lwjgl.system.MemoryStack;

/**
 * Compile-time stub for optional Sodium integration. This interface is excluded from produced jars.
 */
public interface VertexBufferWriter {
    void push(MemoryStack stack, long ptr, int count, VertexFormat format);
}