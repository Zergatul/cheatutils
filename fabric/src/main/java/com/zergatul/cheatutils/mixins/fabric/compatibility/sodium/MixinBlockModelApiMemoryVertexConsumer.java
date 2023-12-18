package com.zergatul.cheatutils.mixins.fabric.compatibility.sodium;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zergatul.cheatutils.utils.UnsafeUtil;
import com.zergatul.cheatutils.webui.BlockModelApi;
import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import sun.misc.Unsafe;

@Mixin(BlockModelApi.MemoryVertexConsumer.class)
public abstract class MixinBlockModelApiMemoryVertexConsumer implements VertexBufferWriter {

    @Unique
    private static final Unsafe UNSAFE = UnsafeUtil.get();

    @Shadow
    protected abstract VertexConsumer vertex(double x, double y, double z);

    @Shadow
    protected abstract VertexConsumer color(int r, int g, int b, int a);

    @Shadow
    protected abstract VertexConsumer uv(float u, float v);

    @Shadow
    protected abstract void endVertex();

    public void push(MemoryStack stack, long ptr, int count, VertexFormatDescription format) {
        int positionOffset = format.getElementOffset(CommonVertexAttribute.POSITION);
        int colorOffset = format.getElementOffset(CommonVertexAttribute.COLOR);
        int uvOffset = format.getElementOffset(CommonVertexAttribute.TEXTURE);
        int stride = format.stride();
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