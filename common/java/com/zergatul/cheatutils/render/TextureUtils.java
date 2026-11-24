package com.zergatul.cheatutils.render;

import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.renderpearl.api.GpuFormat;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.textures.FilterMode;
import com.mojang.renderpearl.api.textures.GpuTexture;
import com.mojang.renderpearl.api.textures.GpuTextureView;
import net.minecraft.client.renderer.RenderPassLayouts;
import net.minecraft.client.renderer.pipeline.RenderPipelines;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class TextureUtils {

    public static void saveAsPng(int id, String path) throws IOException {
        File file = new File(path);
        FileOutputStream stream = new FileOutputStream(file);
        stream.write(glTextureToPng(id));
        stream.close();
    }

    // logic copied from Screenshot.takeScreenshot
    public static CompletableFuture<byte[]> toPng(GpuTexture texture) {
        if ((texture.usage() & GpuTexture.USAGE_COPY_SRC) == 0) {
            TextureTarget target = new TextureTarget("Texture dump target", texture.getWidth(0), texture.getHeight(0), GpuFormat.RGBA8_UNORM, null);
            try {
                try (GpuTextureView sourceView = RenderSystem.getDevice().createTextureView(texture);
                     RenderPass renderPass = RenderSystem.getDevice().createCommandEncoder().createRenderPass(
                             () -> "Copy texture for readback",
                             RenderPassLayouts.COLOR_ONLY,
                             Objects.requireNonNull(target.getColorTextureView()))) {
                    renderPass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.TRACY_BLIT, renderPass.activeSubpass()));
                    RenderSystem.bindDefaultUniforms(renderPass);
                    renderPass.setUniform("InSampler", sourceView, RenderSystem.getSamplerCache().getClampToEdge(FilterMode.NEAREST));
                    renderPass.draw(3, 1, 0, 0);
                }
                return toPng(Objects.requireNonNull(target.getColorTexture())).whenComplete((png, error) -> target.destroyBuffers());
            } catch (RuntimeException | Error e) {
                target.destroyBuffers();
                throw e;
            }
        }

        CompletableFuture<byte[]> future = new CompletableFuture<>();

        int width = texture.getWidth(0);
        int height = texture.getHeight(0);
        int size = width * height * texture.getFormat().blockSize();
        GpuBuffer buffer = RenderSystem.getDevice().createBuffer(() -> "Texture dump buffer", GpuBuffer.USAGE_MAP_READ | GpuBuffer.USAGE_COPY_DST, size);
        RenderSystem.getDevice().createCommandEncoder().copyTextureToBuffer(texture, buffer, 0L, 0);
        RenderSystem.queueFencedTask(() -> {
            try (buffer; GpuBufferSlice.MappedView read = buffer.map(true, false)) {
                try (NativeImage image = new NativeImage(width, height, true)) {
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            image.setPixelABGR(x, y, read.data().getInt((x + y * width) * texture.getFormat().blockSize()));
                        }
                    }

                    future.complete(ImageUtils.toPng(image));
                }
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    public static byte[] glTextureToPng(int id) throws IOException {
        int prevTexture = GL30.glGetInteger(GL30.GL_TEXTURE_BINDING_2D);
        GL30.glBindTexture(GL30.GL_TEXTURE_2D, id);

        pushState();
        try {
            int width = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_WIDTH);
            int height = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_HEIGHT);
            int format = GL30.glGetTexLevelParameteri(GL30.GL_TEXTURE_2D, 0, GL30.GL_TEXTURE_INTERNAL_FORMAT);

            byte[] result;
            if (format == GL30.GL_RGBA || format == GL30.GL_RGBA8) {
                ByteBuffer buffer = MemoryUtil.memAlloc(width * height * 4);
                try {
                    GL30.glGetTexImage(GL30.GL_TEXTURE_2D, 0, GL30.GL_BGRA, GL30.GL_UNSIGNED_BYTE, buffer);
                    IntBuffer intBuf = buffer.asIntBuffer();
                    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            image.setRGB(x, y, intBuf.get(y * width + x));
                        }
                    }

                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    ImageIO.write(image, "png", stream);
                    result = stream.toByteArray();
                } finally {
                    MemoryUtil.memFree(buffer);
                }
            } else {
                throw new IOException("Pixel format is not supported.");
            }

            return result;
        } finally {
            popState();
            GL30.glBindTexture(GL30.GL_TEXTURE_2D, prevTexture);
        }
    }

    private static int prevPackBuffer;
    private static int prevPackAlignment;

    private static void pushState() {
        prevPackBuffer = GL30.glGetInteger(GL30.GL_PIXEL_PACK_BUFFER_BINDING);
        prevPackAlignment = GL30.glGetInteger(GL30.GL_PACK_ALIGNMENT);

        GL30.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, 0);
        GL30.glPixelStorei(GL30.GL_PACK_ALIGNMENT, 1);
    }

    private static void popState() {
        GL30.glPixelStorei(GL30.GL_PACK_ALIGNMENT, prevPackAlignment);
        GL30.glBindBuffer(GL30.GL_PIXEL_PACK_BUFFER, prevPackBuffer);
    }
}