package com.zergatul.cheatutils.mixins.fabric.compatibility.sodium;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import com.zergatul.cheatutils.utils.UnsafeUtil;
import com.zergatul.cheatutils.webui.BlockModelApi;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import sun.misc.Unsafe;

@Mixin(value = BlockModelApi.MemoryVertexConsumer.class, remap = false)
public abstract class MixinBlockModelApiMemoryVertexConsumer implements VertexBufferWriter {

    @Unique
    private static final Unsafe UNSAFE = UnsafeUtil.get();

    @Shadow(remap = false)
    protected abstract VertexConsumer vertex(double x, double y, double z);

    @Shadow(remap = false)
    protected abstract VertexConsumer color(int r, int g, int b, int a);

    @Shadow(remap = false)
    protected abstract VertexConsumer uv(float u, float v);

    @Shadow(remap = false)
    protected abstract void endVertex();

    public void push(MemoryStack stack, long ptr, int count, VertexFormat format) {
        int positionOffset = format.getOffset(VertexFormatElement.POSITION);
        int colorOffset = format.getOffset(VertexFormatElement.COLOR);
        int uvOffset = format.getOffset(VertexFormatElement.UV0);
        int stride = format.getVertexSize();
        for (int i = 0; i < count; i++) {
            long offset = ptr + i * stride;

            float x = UNSAFE.getFloat(offset + positionOffset);
            float y = UNSAFE.getFloat(offset + positionOffset + 4);
            float z = UNSAFE.getFloat(offset + positionOffset + 8);
            this.vertex(x, y, z);

            int color = UNSAFE.getInt(offset + colorOffset);
            int r = (color >>> 16) & 0xFF;
            int g = (color >>> 8) & 0xFF;
            int b = color & 0xFF;
            int a = (color >>> 24) & 0xFF;
            this.color(r, g, b, a);

            float u = UNSAFE.getFloat(offset + uvOffset);
            float v = UNSAFE.getFloat(offset + uvOffset + 4);
            this.uv(u, v);

            this.endVertex();
        }
    }
}