package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.configs.AutoWaterBucketConfig;
import com.zergatul.cheatutils.configs.ConfigStore;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundContainerClickPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

public class AutoWaterBucketController {

    public static final AutoWaterBucketController instance = new AutoWaterBucketController();

    private final Minecraft mc = Minecraft.getInstance();
    private boolean wasFalling = false;
    private long startedFalling = 0;
    private long minFalling = 500 * 1000000L;
    private List<BlockPos> fallBlocks = new ArrayList<>();
    private boolean lockHorizontalMovement = false;

    public AutoWaterBucketController() {

    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            AutoWaterBucketConfig config = ConfigStore.instance.getConfig().autoWaterBucketConfig;
            if (config.enabled && mc.player != null && mc.level != null) {
                if (!mc.player.isFallFlying() && !mc.player.isOnGround()) {
                    if (wasFalling) {
                        if (System.nanoTime() - startedFalling > minFalling && !lockHorizontalMovement) {
                            if (!selectWaterBucket(mc.player, config)) {
                                return;
                            }

                            float radius = mc.player.getBbWidth() / 2;
                            double xp = mc.player.getX();
                            double yp = mc.player.getY();
                            double zp = mc.player.getZ();
                            int x = Mth.floor(xp);
                            int y = Mth.floor(yp);
                            int z = Mth.floor(zp);

                            fallBlocks.clear();
                            for (int dx = -1; dx <= 1; dx++) {
                                for (int dz = -1; dz <= 1; dz++) {
                                    if (intersects(xp, zp, radius, x + dx + 0.5, z + dz + 0.5)) {
                                        for (int yb = y; yb >= -64; yb--) {
                                            BlockPos pos = new BlockPos(x + dx, yb, z + dz);
                                            BlockState state = mc.level.getBlockState(pos);
                                            if (state.isAir()) {
                                                continue;
                                            }
                                            if (state.getBlock() == Blocks.WATER || state.getBlock() == Blocks.LAVA) {
                                                break;
                                            }
                                            fallBlocks.add(pos);
                                            break;
                                        }
                                    }
                                }
                            }

                            fallBlocks.sort((p1, p2) -> -Integer.compare(p1.getY(), p2.getY()));

                            if (fallBlocks.size() > 0) {
                                BlockPos pos = fallBlocks.get(0);
                                double dx = xp - (pos.getX() + 0.5);
                                double dy = yp - (pos.getY() + 1);
                                double dz = zp - (pos.getZ() + 0.5);
                                double distanceSqr = dx * dx + dy * dy + dz * dz;
                                if (distanceSqr < 4) {
                                    useWaterBucketOn(mc.player, pos);
                                    lockHorizontalMovement = true;
                                }
                            }
                        }
                    } else {
                        wasFalling = true;
                        startedFalling = System.nanoTime();
                    }
                } else {
                    wasFalling = false;
                    lockHorizontalMovement = false;
                }
            }
        }
    }

    private boolean selectWaterBucket(LocalPlayer player, AutoWaterBucketConfig config) {
        Inventory inventory = player.getInventory();

        // check hotbar
        int waterBucketSlot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() == Items.WATER_BUCKET) {
                inventory.selected = i;
                return true;
            }
        }

        // check inventory
        for (int i = 9; i < 36; i++) {
            ItemStack itemStack = inventory.getItem(i);
            if (itemStack.getItem() == Items.WATER_BUCKET) {
                waterBucketSlot = i;
                break;
            }
        }
        if (waterBucketSlot > 0) {
            ItemStack air = new ItemStack(Items.AIR, 1);
            ItemStack waterBucket = inventory.getItem(waterBucketSlot);
            Int2ObjectMap<ItemStack> int2objectmap = new Int2ObjectOpenHashMap<>();
            int2objectmap.put(waterBucketSlot, air);
            NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                    0, // containerId
                    player.inventoryMenu.getStateId(),
                    waterBucketSlot,
                    0, // buttonNum
                    ClickType.PICKUP,
                    waterBucket,
                    int2objectmap
            ));
            inventory.setItem(waterBucketSlot, air);

            int hotbarSlot = config.slot - 1;
            ItemStack old = inventory.getItem(hotbarSlot);
            if (old.isEmpty()) {
                int2objectmap = new Int2ObjectOpenHashMap<>();
                int2objectmap.put(hotbarSlot + 36, waterBucket);
                NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                        0, // containerId
                        player.inventoryMenu.getStateId(),
                        hotbarSlot + 36,
                        0, // buttonNum
                        ClickType.PICKUP,
                        air,
                        int2objectmap
                ));
                inventory.setItem(hotbarSlot, waterBucket);

                inventory.selected = hotbarSlot;
                return true;
            } else {
                int2objectmap = new Int2ObjectOpenHashMap<>();
                int2objectmap.put(hotbarSlot + 36, waterBucket);
                NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                        0, // containerId
                        player.inventoryMenu.getStateId(),
                        hotbarSlot + 36,
                        0, // buttonNum
                        ClickType.PICKUP,
                        old,
                        int2objectmap
                ));
                inventory.setItem(hotbarSlot, waterBucket);

                int2objectmap = new Int2ObjectOpenHashMap<>();
                int2objectmap.put(waterBucketSlot, old);
                NetworkPacketsController.instance.sendPacket(new ServerboundContainerClickPacket(
                        0, // containerId
                        player.inventoryMenu.getStateId(),
                        waterBucketSlot,
                        0, // buttonNum
                        ClickType.PICKUP,
                        air,
                        int2objectmap
                ));
                inventory.setItem(waterBucketSlot, old);

                inventory.selected = hotbarSlot;
                return true;
            }
        }

        return false;
    }

    private boolean intersects(double xp, double zp, double r, double xb, double zb) {
        double dx = Math.abs(xp - xb);
        double dz = Math.abs(zp - zb);

        if (dx > (0.5 + r)) { return false; }
        if (dz > (0.5 + r)) { return false; }

        if (dx <= 0.5) { return true; }
        if (dz <= 0.5) { return true; }

        double d2 = (dx - 0.5) * (dx - 0.5) + (dz - 0.5) * (dz - 0.5);
        return d2 <= r * r;
    }

    private void useWaterBucketOn(LocalPlayer player, BlockPos pos) {
        if (mc.gameMode == null || mc.level == null) {
            return;
        }
        Vec3 vec3 = player.getEyePosition();
        double dx = pos.getX() + 0.5 - player.getX();
        double dz = pos.getZ() + 0.5 - player.getZ();
        double xRot = Math.atan2(dz, dx) /  Math.PI * 180;
        double horizontal = Math.sqrt(dx * dx + dz * dz);
        double yRot = -Math.atan2(pos.getY() + 1 - player.getY(), horizontal) / Math.PI * 180;
        if (yRot > 90) {
            yRot -= 180;
        }
        if (yRot < -90) {
            yRot += 180;
        }
        player.setXRot((float)xRot);
        player.setYRot((float)yRot);
        Vec3 vec31 = player.getViewVector(1);
        double pickRange = mc.gameMode.getPickRange();
        Vec3 vec32 = vec3.add(vec31.x * pickRange, vec31.y * pickRange, vec31.z * pickRange);
        HitResult hitResult = mc.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, null));
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            //BlockHitResult blockHitResult = (hit)
        }
        //BlockHitResult
        //NetworkPacketsController.instance.sendPacket(new ServerboundUseItemOnPacket(InteractionHand.MAIN_HAND));
    }
}
