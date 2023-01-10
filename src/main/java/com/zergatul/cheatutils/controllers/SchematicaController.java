package com.zergatul.cheatutils.controllers;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SchematicaConfig;
import com.zergatul.cheatutils.schematics.PlacingConverter;
import com.zergatul.cheatutils.schematics.PlacingSettings;
import com.zergatul.cheatutils.schematics.SchematicFile;
import com.zergatul.cheatutils.utils.*;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FaceInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.model.data.ModelData;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class SchematicaController {

    public static final SchematicaController instance = new SchematicaController();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Entry> entries = new ArrayList<>();
    private final RandomSource random = new JavaRandom(0);
    private final long[] lastSlotUsage = new long[9];

    private SchematicaController() {
        ModApiWrapper.ClientTickEnd.add(this::onClientTickEnd);
        ModApiWrapper.RenderWorldLast.add(this::onRender);

        Arrays.fill(lastSlotUsage, Long.MIN_VALUE);
    }

    public synchronized void clear() {
        entries.clear();
    }

    public synchronized void place(SchematicFile file, PlacingSettings placing) {
        entries.add(new Entry(file, placing));
    }

    private synchronized void onClientTickEnd() {
        SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
        if (!config.enabled || !config.autoBuild) {
            return;
        }

        if (mc.level == null || mc.player == null) {
            return;
        }

        Vec3 pos = mc.player.getEyePosition();
        int xp = (int)Math.round(pos.x);
        int yp = (int)Math.round(pos.y);
        int zp = (int)Math.round(pos.z);
        int distance = (int)Math.round(config.autoBuildDistance) + 1;
        double maxDistanceSqr = config.autoBuildDistance * config.autoBuildDistance;
        ItemStack itemInHand = mc.player.getMainHandItem();

        Block blockInHand;
        if (itemInHand.getItem() instanceof BlockItem blockItem) {
            blockInHand = blockItem.getBlock();
        } else {
            if (config.autoSelectItems) {
                blockInHand = null;
            } else {
                return;
            }
        }

        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int dx = -distance; dx <= distance; dx++) {
            for (int dy = -distance; dy <= distance; dy++) {
                for (int dz = -distance; dz <= distance; dz++) {
                    mpos.setX(xp + dx);
                    mpos.setY(yp + dy);
                    mpos.setZ(zp + dz);

                    double blockDx = mpos.getX() + 0.5 - xp;
                    double blockDy = mpos.getY() + 0.5 - yp;
                    double blockDz = mpos.getZ() + 0.5 - zp;
                    if (blockDx * blockDx + blockDy * blockDy + blockDz * blockDz > maxDistanceSqr) {
                        continue;
                    }

                    for (Entry entry : entries) {
                        Block block = entry.blocks.get(mpos);
                        if (block == null) {
                            continue;
                        }

                        BlockUtils.PlaceBlockPlan plan = BlockUtils.getPlacingPlan(mpos);
                        if (plan == null) {
                            continue;
                        }

                        if (config.autoSelectItems) {
                            if (selectItem(config, block))  {
                                blockInHand = block;
                            }
                        }
                        if (blockInHand == block && mc.level.getBlockState(mpos).isAir()) {
                            BlockUtils.applyPlacingPlan(plan);
                            return;
                        }
                    }
                }
            }
        }
    }

    private synchronized void onRender(RenderWorldLastEvent event) {
        SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
        if (!config.enabled) {
            return;
        }

        if (mc.level == null) {
            return;
        }

        Vec3 view = event.getCamera().getPosition();

        if (config.showMissingBlockGhosts || config.showMissingBlockTracers || config.showMissingBlockCubes) {
            double missingBlockGhostsMaxDistanceSqr =
                    config.missingBlockGhostsMaxDistance * config.missingBlockGhostsMaxDistance;
            double missingBlockTracersMaxDistanceSqr =
                    config.missingBlockTracersMaxDistance * config.missingBlockTracersMaxDistance;
            double missingBlockCubesMaxDistanceSqr =
                    config.missingBlockCubesMaxDistance * config.missingBlockCubesMaxDistance;

            List<BlockPos> missingBlockTracers = new ArrayList<>();
            List<BlockPos> missingBlockCubes = new ArrayList<>();

            RenderSystem.enableDepthTest();
            RenderSystem.depthMask(true);
            RenderSystem.enableCull();
            RenderSystem.enableBlend();
            RenderSystem.enableTexture();
            RenderSystem.setShaderColor(1.0f, 0.5f, 0.5f, 0.6f);

            BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
            for (Entry entry: entries) {
                for (var mapEntry : entry.blocks.entrySet()) {
                    BlockPos pos = mapEntry.getKey();
                    Block block = mapEntry.getValue();
                    BlockState state = mc.level.getBlockState(pos);

                    double dx = pos.getX() - view.x;
                    double dy = pos.getY() - view.y;
                    double dz = pos.getZ() - view.z;
                    double distanceSqr = dx * dx + dy * dy + dz * dz;

                    if (state.isAir()) {
                        if (config.showMissingBlockGhosts && distanceSqr < missingBlockGhostsMaxDistanceSqr) {
                            BakedModel model = mc.getBlockRenderer().getBlockModel(block.defaultBlockState());
                            for (var direction : Direction.values()) {
                                mpos.setX(pos.getX() + direction.getStepX());
                                mpos.setY(pos.getY() + direction.getStepY());
                                mpos.setZ(pos.getZ() + direction.getStepZ());
                                if (!entry.blocks.containsKey(mpos)) {
                                    List<BakedQuad> quads = model.getQuads(null, direction, random, ModelData.EMPTY, null);
                                    if (quads.size() > 0) {
                                        BakedQuad quad = quads.get(0);
                                        TextureAtlasSprite sprite = quad.getSprite();
                                        BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                                        bufferBuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                                        RenderSystem.setShaderTexture(0, sprite.atlasLocation());

                                        FaceInfo face = FaceInfo.fromFacing(direction);
                                        FaceInfo.VertexInfo info;

                                        info = face.getVertexInfo(0);
                                        bufferBuilder.vertex(
                                                        (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                                        (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                                        (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                                .uv(sprite.getU0(), sprite.getV0()).endVertex();

                                        info = face.getVertexInfo(1);
                                        bufferBuilder.vertex(
                                                        (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                                        (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                                        (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                                .uv(sprite.getU0(), sprite.getV1()).endVertex();

                                        info = face.getVertexInfo(2);
                                        bufferBuilder.vertex(
                                                        (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                                        (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                                        (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                                .uv(sprite.getU1(), sprite.getV1()).endVertex();

                                        info = face.getVertexInfo(3);
                                        bufferBuilder.vertex(
                                                        (info.xFace == FaceInfo.Constants.MIN_X ? pos.getX() : pos.getX() + 1) - view.x,
                                                        (info.yFace == FaceInfo.Constants.MIN_Y ? pos.getY() : pos.getY() + 1) - view.y,
                                                        (info.zFace == FaceInfo.Constants.MIN_Z ? pos.getZ() : pos.getZ() + 1) - view.z)
                                                .uv(sprite.getU1(), sprite.getV0()).endVertex();

                                        SharedVertexBuffer.instance.bind();
                                        SharedVertexBuffer.instance.upload(bufferBuilder.end());
                                        SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionTexShader());
                                        VertexBuffer.unbind();
                                    }
                                }
                            }
                        }

                        if (config.showMissingBlockTracers && distanceSqr < missingBlockTracersMaxDistanceSqr) {
                            missingBlockTracers.add(pos);
                        }

                        if (config.showMissingBlockCubes && distanceSqr < missingBlockCubesMaxDistanceSqr) {
                            missingBlockCubes.add(pos);
                        }
                    }
                }
            }

            if (missingBlockTracers.size() > 0) {
                Vec3 tracerCenter = event.getTracerCenter();
                double tracerX = tracerCenter.x;
                double tracerY = tracerCenter.y;
                double tracerZ = tracerCenter.z;

                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                RenderSystem.setShaderColor(0.2f, 1.0f, 0.2f, 0.8f);

                for (BlockPos pos : missingBlockTracers) {
                    bufferBuilder.vertex(tracerX - view.x, tracerY - view.y, tracerZ - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(pos.getX() + 0.5 - view.x, pos.getY() + 0.5 - view.y, pos.getZ() + 0.5 - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                }

                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);

                SharedVertexBuffer.instance.bind();
                SharedVertexBuffer.instance.upload(bufferBuilder.end());
                SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();

                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            if (missingBlockCubes.size() > 0) {
                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                RenderSystem.setShaderColor(0.2f, 1.0f, 0.2f, 0.8f);

                for (BlockPos pos : missingBlockCubes) {
                    double x1 = pos.getX() + 0.25 - view.x;
                    double y1 = pos.getY() + 0.25 - view.y;
                    double z1 = pos.getZ() + 0.25 - view.z;
                    double x2 = x1 + 0.5;
                    double y2 = y1 + 0.5;
                    double z2 = z1 + 0.5;

                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();

                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();

                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                }

                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);

                SharedVertexBuffer.instance.bind();
                SharedVertexBuffer.instance.upload(bufferBuilder.end());
                SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();

                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }

        if (config.showWrongBlockTracers || config.showWrongBlockCubes) {
            double wrongBlockTracersMaxDistanceSqr =
                    config.wrongBlockTracersMaxDistance * config.wrongBlockTracersMaxDistance;
            double wrongBlockCubesMaxDistanceSqr =
                    config.wrongBlockCubesMaxDistance * config.wrongBlockCubesMaxDistance;
            double maxCheck = Math.max(wrongBlockTracersMaxDistanceSqr, wrongBlockCubesMaxDistanceSqr);

            List<BlockPos> wrongBlockTracers = new ArrayList<>();
            List<BlockPos> wrongBlockCubes = new ArrayList<>();

            BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
            for (Entry entry: entries) {
                int x1 = entry.x1;
                int x2 = entry.x2;
                int y1 = entry.y1;
                int y2 = entry.y2;
                int z1 = entry.z1;
                int z2 = entry.z2;
                for (int x = x1; x < x2; x++) {
                    mpos.setX(x);
                    double dx = x - view.x;
                    double dxSqr = dx * dx;
                    for (int y = y1; y < y2; y++) {
                        mpos.setY(y);
                        double dy = y - view.y;
                        double dySqr = dy * dy;
                        for (int z = z1; z < z2; z++) {
                            mpos.setZ(z);
                            double dz = z - view.z;
                            double dzSqr = dz * dz;
                            double distanceSqr = dxSqr + dySqr + dzSqr;
                            if (distanceSqr > maxCheck) {
                                continue;
                            }

                            BlockState state = mc.level.getBlockState(mpos);
                            if (state.isAir()) {
                                continue;
                            }
                            Block block = entry.blocks.getOrDefault(mpos, Blocks.AIR);
                            if (state.getBlock() != block) {
                                if (distanceSqr < wrongBlockTracersMaxDistanceSqr) {
                                    wrongBlockTracers.add(mpos.immutable());
                                }
                                if (distanceSqr < wrongBlockCubesMaxDistanceSqr) {
                                    wrongBlockCubes.add(mpos.immutable());
                                }
                            }
                        }
                    }
                }
            }

            if (wrongBlockTracers.size() > 0) {
                Vec3 tracerCenter = event.getTracerCenter();
                double tracerX = tracerCenter.x;
                double tracerY = tracerCenter.y;
                double tracerZ = tracerCenter.z;

                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                RenderSystem.setShaderColor(1.0f, 0.2f, 0.2f, 0.8f);

                for (BlockPos pos : wrongBlockTracers) {
                    bufferBuilder.vertex(tracerX - view.x, tracerY - view.y, tracerZ - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(pos.getX() + 0.5 - view.x, pos.getY() + 0.5 - view.y, pos.getZ() + 0.5 - view.z)
                            .color(1f, 1f, 1f, 1f).endVertex();
                }

                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);

                SharedVertexBuffer.instance.bind();
                SharedVertexBuffer.instance.upload(bufferBuilder.end());
                SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();

                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }

            if (wrongBlockCubes.size() > 0) {
                BufferBuilder bufferBuilder = Tesselator.getInstance().getBuilder();
                bufferBuilder.begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
                RenderSystem.setShaderColor(1.0f, 0.2f, 0.2f, 0.8f);

                for (BlockPos pos : wrongBlockCubes) {
                    double x1 = pos.getX() + 0.25 - view.x;
                    double y1 = pos.getY() + 0.25 - view.y;
                    double z1 = pos.getZ() + 0.25 - view.z;
                    double x2 = x1 + 0.5;
                    double y2 = y1 + 0.5;
                    double z2 = z1 + 0.5;

                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();

                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();

                    bufferBuilder.vertex(x1, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x1, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z2)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y1, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                    bufferBuilder.vertex(x2, y2, z1)
                            .color(1f, 1f, 1f, 1f).endVertex();
                }

                RenderSystem.disableCull();
                RenderSystem.enableBlend();
                RenderSystem.defaultBlendFunc();
                RenderSystem.disableTexture();
                RenderSystem.disableDepthTest();
                GL11.glEnable(GL11.GL_LINE_SMOOTH);

                SharedVertexBuffer.instance.bind();
                SharedVertexBuffer.instance.upload(bufferBuilder.end());
                SharedVertexBuffer.instance.drawWithShader(event.getMatrixStack().last().pose(), event.getProjectionMatrix(), GameRenderer.getPositionColorShader());
                VertexBuffer.unbind();

                RenderSystem.disableBlend();
                RenderSystem.enableCull();
                RenderSystem.enableTexture();
                RenderSystem.enableDepthTest();
            }
        }
    }

    private boolean selectItem(SchematicaConfig config, Block block) {
        Inventory inventory = mc.player.getInventory();

        // search on hotbar
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() == block) {
                    lastSlotUsage[i] = System.nanoTime();
                    inventory.selected = i;
                    return true;
                }
            }
        }

        if (config.autoSelectSlots.length == 0) {
            return false;
        }

        // search in inventory
        for (int i = 9; i < 36; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() instanceof BlockItem blockItem) {
                if (blockItem.getBlock() == block) {
                    long minTime = Long.MAX_VALUE;
                    int minSlot = config.autoSelectSlots[0];
                    for (int slot : config.autoSelectSlots) {
                        if (lastSlotUsage[slot - 1] < minTime) {
                            minTime = lastSlotUsage[slot - 1];
                            minSlot = slot - 1;
                        }
                    }

                    InventoryUtils.moveItemStack(new InventorySlot(i), new InventorySlot(minSlot));
                    lastSlotUsage[minSlot] = System.nanoTime();
                    inventory.selected = minSlot;
                    return true;
                }
            }
        }

        return false;
    }

    private static class Entry {

        public final int x1, x2, y1, y2, z1, z2;
        public final Map<BlockPos, Block> blocks;

        public Entry(SchematicFile file, PlacingSettings placing) {
            PlacingConverter converter = new PlacingConverter(placing, file.getWidth(), file.getHeight(), file.getLength());

            x1 = placing.x;
            x2 = x1 + converter.getWidth();
            y1 = placing.y;
            y2 = y1 + converter.getHeight();
            z1 = placing.z;
            z2 = z1 + converter.getLength();

            blocks = new HashMap<>();
            for (int x = 0; x < file.getWidth(); x++) {
                for (int y = 0; y < file.getHeight(); y++) {
                    for (int z = 0; z < file.getLength(); z++) {
                        Block block = file.getBlock(x, y, z);
                        if (block != Blocks.AIR) {
                            PlacingConverter.Vec3iMutable vec = converter.convert(x, y, z);
                            blocks.put(new BlockPos(x1 + vec.x, y1 + vec.y, z1 + vec.z), block);
                        }
                    }
                }
            }
        }
    }
}