package com.zergatul.cheatutils.modules.esp;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.TickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LightLevelConfig;
import com.zergatul.cheatutils.controllers.BlockEventsProcessor;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.render.*;
import com.zergatul.cheatutils.utils.Dimension;
import com.zergatul.cheatutils.common.events.BlockUpdateEvent;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class LightLevel implements Module {

    public static final LightLevel instance = new LightLevel();

    private final Minecraft mc = Minecraft.getInstance();
    private final Identifier texture = Identifier.fromNamespaceAndPath(ModMain.MODID, "textures/light-level.png");
    private final TextureLocation[] numbers = new TextureLocation[16];
    private final HashMap<ChunkPos, HashSet<BlockPos>> chunks = new HashMap<>();
    private final List<BlockPos> listForRendering = new ArrayList<>();
    private final Map<Direction, RotationIndexes> rotations = Map.ofEntries(
            Map.entry(Direction.NORTH, new RotationIndexes(
                    new int[] { 0, 0, 1, 1 },
                    new int[] { 0, 1, 1, 0 })),
            Map.entry(Direction.SOUTH, new RotationIndexes(
                    new int[] { 1, 1, 0, 0 },
                    new int[] { 1, 0, 0, 1 })),
            Map.entry(Direction.EAST, new RotationIndexes(
                    new int[] { 0, 1, 1, 0 },
                    new int[] { 1, 1, 0, 0 })),
            Map.entry(Direction.WEST, new RotationIndexes(
                    new int[] { 1, 0, 0, 1 },
                    new int[] { 0, 0, 1, 1 })));

    private GpuTextureView textureView;
    private boolean active = false;

    private LightLevel() {
        Events.AfterRenderWorld.add(this::render);
        Events.RawChunkLoaded.add(this::onChunkLoaded);
        Events.RawChunkUnloaded.add(this::onChunkUnLoaded);
        Events.RawBlockUpdated.add(this::onBlockChanged);

        for (int i = 0; i < 16; i++) {
            float x1 = 0.25f * (i % 4);
            float y1 = 0.25f * (i >> 2);
            float x2 = x1 + 0.25f;
            float y2 = y1 + 0.25f;
            numbers[i] = new TextureLocation(new float[] { x1, x2 }, new float[] { y1, y2 });
        }
    }

    public void onChanged() {
        TickEndExecutor.instance.execute(() -> {
            boolean value = ConfigStore.instance.getConfig().lightLevelConfig.enabled;
            if (active != value) {
                active = value;
                if (active) {
                    AtomicReferenceArray<LevelChunk> chunks = BlockEventsProcessor.instance.getRawChunks();
                    for (int i = 0; i < chunks.length(); i++) {
                        LevelChunk chunk = chunks.get(i);
                        if (chunk != null) {
                            onChunkLoaded(chunk);
                        }
                    }
                }
            }
        });
    }

    private void render(RenderWorldLastEvent event) {
        assert mc.level != null;
        assert mc.player != null;

        LightLevelConfig config = ConfigStore.instance.getConfig().lightLevelConfig;
        if (!config.enabled) {
            return;
        }

        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 view = camera.position();

        Direction direction = Direction.fromYRot(camera.yRot());
        RotationIndexes rot = rotations.get(direction);

        double maxDistance2 = config.maxDistance * config.maxDistance;
        double xc = config.useFreeCamPosition ? view.x : mc.player.getX();
        double yc = config.useFreeCamPosition ? view.y : mc.player.getY();
        double zc = config.useFreeCamPosition ? view.z : mc.player.getZ();

        Texture3dRenderer textureRenderer = Texture3dRenderer.getInstance();
        textureRenderer.begin();

        List<BlockPos> listTracers = new ArrayList<>();
        for (BlockPos pos : getBlockForRendering()) {
            double dx = xc - pos.getX();
            double dy = yc - pos.getY();
            double dz = zc - pos.getZ();
            if (dx * dx + dy * dy + dz * dz > maxDistance2) {
                continue;
            }
            int blockLight = mc.level.getBrightness(LightLayer.BLOCK, pos);
            if (blockLight == 0) {
                listTracers.add(pos);
            }
            if (config.showLightLevelValue) {
                float y = (float)(pos.getY() + 0.05 - view.y);
                float x1 = (float)(pos.getX() + 0.05 - view.x);
                float z1 = (float)(pos.getZ() + 0.05 - view.z);
                float x2 = x1 + 0.9f;
                float z2 = z1 + 0.9f;

                TextureLocation location = numbers[blockLight];
                textureRenderer.quad(
                        x1, y, z1, location.x[rot.u[0]], location.y[rot.v[0]],
                        x1, y, z2, location.x[rot.u[1]], location.y[rot.v[1]],
                        x2, y, z2, location.x[rot.u[2]], location.y[rot.v[2]],
                        x2, y, z1, location.x[rot.u[3]], location.y[rot.v[3]]);
            }
        }

        if (textureView == null) {
            textureView = RenderSystem.getDevice().createTextureView(mc.getTextureManager().getTexture(texture).getTexture());
        }

        textureRenderer.end(event.getMvp(), textureView);

        Vec3 tracerCenter = event.getTracerCenter();
        double tracerX = tracerCenter.x;
        double tracerY = tracerCenter.y;
        double tracerZ = tracerCenter.z;

        EspLineRenderer lineRenderer = EspLineRenderer.getInstance();
        lineRenderer.begin();

        for (BlockPos pos: listTracers) {
            double y = pos.getY() + 0.05;
            if (config.showTracers) {
                lineRenderer.line(
                        event.getCameraPos(),
                        tracerX, tracerY, tracerZ,
                        pos.getX() + 0.5, y, pos.getZ() + 0.5,
                        0x7FFFFFFF, 1f);
            }

            if (config.showLocations) {
                double x1 = pos.getX() + 0.05;
                double z1 = pos.getZ() + 0.05;
                double x2 = x1 + 0.9;
                double z2 = z1 + 0.9;
                lineRenderer.line(event.getCameraPos(), x1, y, z1, x1, y, z2, 0x7FFFFFFF, 1f);
                lineRenderer.line(event.getCameraPos(), x1, y, z2, x2, y, z2, 0x7FFFFFFF, 1f);
                lineRenderer.line(event.getCameraPos(), x2, y, z2, x2, y, z1, 0x7FFFFFFF, 1f);
                lineRenderer.line(event.getCameraPos(), x2, y, z1, x1, y, z1, 0x7FFFFFFF, 1f);
            }
        }

        lineRenderer.end(event.getMvp());
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

    private void onChunkLoaded(LevelChunk chunk) {
        if (!active) {
            return;
        }

        Dimension dimension = Dimension.get((ClientLevel) chunk.getLevel());
        ChunkPos chunkPos = chunk.getPos();
        HashSet<BlockPos> set;
        synchronized (chunks) {
            set = chunks.get(chunkPos);
            if (set == null) {
                set = new HashSet<>();
                chunks.put(chunkPos, set);
            }
        }
        int xc = chunk.getPos().x() << 4;
        int zc = chunk.getPos().z() << 4;
        synchronized (set) {
            set.clear();
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    int height = chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                    for (int y = dimension.getMinY(); y <= height; y++) {
                        int xb = xc | x;
                        int zb = zc | z;
                        BlockPos pos = new BlockPos(xb, y, zb);
                        checkBlock(chunk, pos, set);
                    }
                }
            }
        }
    }

    private void onChunkUnLoaded(ChunkAccess chunk) {
        if (!active) {
            return;
        }

        synchronized (chunks) {
            chunks.remove(chunk.getPos());
        }
    }

    private void onBlockChanged(BlockUpdateEvent event) {
        if (!active) {
            return;
        }

        LevelChunk chunk = event.chunk();
        HashSet<BlockPos> set;
        synchronized (chunks) {
            set = chunks.get(chunk.getPos());
        }
        if (set == null) {
            return;
        }
        synchronized (set) {
            BlockPos pos = event.pos();
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
    }

    private void checkBlock(ChunkAccess chunk, BlockPos pos, HashSet<BlockPos> set) {
        if (canSpawnOn(chunk.getBlockState(pos), pos)) {
            BlockPos posAbove = pos.above();
            BlockState stateAbove = chunk.getBlockState(posAbove);
            if (stateAbove.isSolid()) {
                return;
            }
            if (!stateAbove.getFluidState().isEmpty()) {
                return;
            }
            if (stateAbove.is(BlockTags.PREVENT_MOB_SPAWNING_INSIDE)) {
                return;
            }

            BlockState stateAbove2 = chunk.getBlockState(posAbove.above());
            if (stateAbove2.isSolid()) {
                return;
            }

            set.add(posAbove);
        }
    }

    private boolean canSpawnOn(BlockState state, BlockPos pos) {
        assert mc.level != null;

        if (state.getBlock() instanceof SlabBlock) {
            return state.getValue(SlabBlock.TYPE) != SlabType.BOTTOM;
        }

        if (state.getBlock() instanceof StairBlock) {
            return state.getValue(StairBlock.HALF) == Half.TOP;
        }

        if (!state.canOcclude()) {
            return false;
        }

        if (state.getBlock() == Blocks.BEDROCK) {
            return false;
        }

        return state.isSolid() && state.isCollisionShapeFullBlock(mc.level, pos);
    }

    private record TextureLocation(float[] x, float[] y) {}

    private record RotationIndexes(int[] u, int[] v) {}
}