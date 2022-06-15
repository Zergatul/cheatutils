package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LightLevelConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLevelLastEvent;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

public class LightLevelController {

    public static final LightLevelController instance = new LightLevelController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Object loopWaitEvent = new Object();
    private final Thread eventLoop;
    private final Queue<Runnable> queue = new ConcurrentLinkedQueue<>();
    private final HashMap<ChunkPos, HashSet<BlockPos>> chunks = new HashMap<>();
    private final List<BlockPos> listForRendering = new ArrayList<>();
    private boolean active = false;
    private VertexBuffer vertexBuffer;

    private LightLevelController() {

        RenderSystem.recordRenderCall(() -> vertexBuffer = new VertexBuffer());

        ChunkController.instance.addOnChunkLoadedHandler(this::onChunkLoaded);
        ChunkController.instance.addOnChunkUnLoadedHandler(this::onChunkUnLoaded);
        ChunkController.instance.addOnBlockChangedHandler(this::onBlockChanged);

        eventLoop = new Thread(() -> {
            try {
                while (true) {
                    synchronized (loopWaitEvent) {
                        loopWaitEvent.wait();
                    }
                    while (queue.size() > 0) {
                        Runnable process = queue.remove();
                        process.run();
                        Thread.yield();
                    }
                }
            }
            catch (InterruptedException e) {
                // do nothing
            }
        });

        eventLoop.start();
    }

    public void onChanged() {
        boolean value = ConfigStore.instance.getConfig().lightLevelConfig.enabled;
        if (active != value) {
            active = value;
            if (active) {
                for (ChunkAccess chunk : ChunkController.instance.getLoadedChunks()) {
                    onChunkLoaded(chunk);
                }
            } else {
                queue.clear();
            }
        }
    }

    public void render(RenderLevelLastEvent event) {
        LightLevelConfig config = ConfigStore.instance.getConfig().lightLevelConfig;
        if (!config.enabled || !config.display) {
            return;
        }

        Vec3 view = mc.gameRenderer.getMainCamera().getPosition();

        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder bufferBuilder = tesselator.getBuilder();

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(true);
        RenderSystem.enableBlend();

        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

        double maxDistance2 = config.maxDistance * config.maxDistance;

        for (BlockPos pos: getBlockForRendering()) {
            double dx = mc.player.getX() - pos.getX();
            double dz = mc.player.getZ() - pos.getZ();
            if (dx * dx + dz * dz > maxDistance2) {
                continue;
            }
            int blockLight = mc.level.getBrightness(LightLayer.BLOCK, pos);
            int skyLight = mc.level.getBrightness(LightLayer.SKY, pos);
            if (blockLight == 0) {
                float r, g, b;
                if (skyLight == 0) {
                    // can spawn any time
                    r = 1f;
                    g = 0f;
                    b = 0f;
                } else {
                    // can spawn at night
                    r = 0f;
                    g = 0f;
                    b = 1f;
                }

                double x1 = pos.getX();
                double x2 = x1 + 1;
                double z1 = pos.getZ();
                double z2 = z1 + 1;
                double y = pos.getY() + 0.1d;
                bufferBuilder.vertex(x1, y, z1).color(r, g, b, 0.2f).endVertex();
                bufferBuilder.vertex(x1, y, z2).color(r, g, b, 0.2f).endVertex();
                bufferBuilder.vertex(x2, y, z2).color(r, g, b, 0.2f).endVertex();
                bufferBuilder.vertex(x2, y, z1).color(r, g, b, 0.2f).endVertex();
            }
        }

        vertexBuffer.bind();
        vertexBuffer.upload(bufferBuilder.end());

        PoseStack matrix = event.getPoseStack();
        matrix.pushPose();
        matrix.translate(-view.x, -view.y, -view.z);
        var shader = GameRenderer.getPositionColorShader();
        vertexBuffer.drawWithShader(matrix.last().pose(), event.getProjectionMatrix().copy(), shader);
        matrix.popPose();

        VertexBuffer.unbind();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        RenderSystem.disableBlend();
    }

    public List<BlockPos> getBlockForRendering() {
        listForRendering.clear();
        synchronized (chunks) {
            for (HashSet<BlockPos> set : chunks.values()) {
                synchronized (set) {
                    listForRendering.addAll(set);
                }
            }
        }
        return listForRendering;
    }

    private void onChunkLoaded(ChunkAccess chunk) {
        if (!active) {
            return;
        }
        queue.add(() -> {
            ChunkPos chunkPos = chunk.getPos();
            HashSet<BlockPos> set;
            synchronized (chunks) {
                set = chunks.get(chunkPos);
                if (set == null) {
                    set = new HashSet<>();
                    chunks.put(chunkPos, set);
                }
            }
            int xc = chunk.getPos().x << 4;
            int zc = chunk.getPos().z << 4;
            synchronized (set) {
                set.clear();
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                        for (int y = 0; y <= height; y++) {
                            int xb = xc | x;
                            int zb = zc | z;
                            BlockPos pos = new BlockPos(xb, y, zb);
                            checkBlock(chunk, pos, set);
                        }
                    }
                }
            }
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void onChunkUnLoaded(ChunkAccess chunk) {
        if (!active) {
            return;
        }
        queue.add(() -> {
            synchronized (chunks) {
                chunks.remove(chunk.getPos());
            }
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void onBlockChanged(BlockPos pos, BlockState state) {
        if (!active) {
            return;
        }
        queue.add(() -> {
            ChunkPos chunkPos = new ChunkPos(pos);
            HashSet<BlockPos> set;
            synchronized (chunks) {
                set = chunks.get(chunkPos);
            }
            if (set == null) {
                return;
            }
            ChunkAccess chunk = mc.level.getChunkSource().getChunk(chunkPos.x, chunkPos.z, false);
            if (chunk == null) {
                return;
            }
            synchronized (set) {
                BlockPos above = pos.above();
                BlockPos below = pos.below();
                BlockPos below2 = below.below();
                set.remove(pos);
                set.remove(above);
                set.remove(below);
                set.remove(below2);
                checkBlock(chunk, pos, set);
                checkBlock(chunk, above, set);
                checkBlock(chunk, below, set);
                checkBlock(chunk, below2, set);
            }
        });
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }
    }

    private void checkBlock(ChunkAccess chunk, BlockPos pos, HashSet<BlockPos> set) {
        BlockState state = chunk.getBlockState(pos);
        if (state.getMaterial().isSolid() && state.isCollisionShapeFullBlock(mc.level, pos)) {
            BlockPos posAbove = pos.above();
            BlockState stateAbove = chunk.getBlockState(posAbove);
            if (stateAbove.getMaterial().isSolid()) {
                return;
            }
            if (!stateAbove.getFluidState().isEmpty()) {
                return;
            }
            if (stateAbove.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
                return;
            }

            set.add(posAbove);
        }
    }

}
