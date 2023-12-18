package net.caffeinemc.mods.sodium.api.vertex.buffer;

import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import org.lwjgl.system.MemoryStack;

public interface VertexBufferWriter {
    void push(MemoryStack stack, long ptr, int count, VertexFormatDescription format);
}
