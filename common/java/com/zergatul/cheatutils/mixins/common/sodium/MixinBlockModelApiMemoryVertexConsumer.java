package com.zergatul.cheatutils.mixins.common.sodium;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.VertexFormatDescription;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(targets = "com.zergatul.cheatutils.webui.BlockModelApi$MemoryVertexConsumer", remap = false)
public abstract class MixinBlockModelApiMemoryVertexConsumer implements VertexBufferWriter {

    @Shadow(remap = true)
    public abstract VertexConsumer vertex(double x, double y, double z);

    @Shadow(remap = true)
    public abstract VertexConsumer color(int r, int g, int b, int a);

    @Shadow(remap = true)
    public abstract VertexConsumer uv(float u, float v);

    @Shadow(remap = true)
    public abstract void endVertex();

    @Override
    public void push(MemoryStack stack, long ptr, int count, VertexFormatDescription format) {
        int positionOffset = format.getElementOffset(CommonVertexAttribute.POSITION);
        int colorOffset = format.getElementOffset(CommonVertexAttribute.COLOR);
        int uvOffset = format.getElementOffset(CommonVertexAttribute.TEXTURE);
        int stride = format.stride();
        for (int i = 0; i < count; i++) {
            long offset = ptr + (long) i * stride;

            this.vertex(
                    MemoryUtil.memGetFloat(offset + positionOffset),
                    MemoryUtil.memGetFloat(offset + positionOffset + 4),
                    MemoryUtil.memGetFloat(offset + positionOffset + 8));

            int color = MemoryUtil.memGetInt(offset + colorOffset);
            this.color(
                    color & 0xFF,
                    color >>> 8 & 0xFF,
                    color >>> 16 & 0xFF,
                    color >>> 24 & 0xFF);

            this.uv(
                    MemoryUtil.memGetFloat(offset + uvOffset),
                    MemoryUtil.memGetFloat(offset + uvOffset + 4));
            this.endVertex();
        }
    }
}
