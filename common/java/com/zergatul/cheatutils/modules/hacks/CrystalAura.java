package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.DamageUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class CrystalAura implements Module {

    public static final CrystalAura instance = new CrystalAura();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<LivingEntity> targets = new ArrayList<>();
    private int placeCountdown;
    private int breakCountdown;

    private CrystalAura() {
        Events.ClientTickStart.add(this::onTickStart);
        Events.AfterPlayerAiStep.add(this::onAfterPlayerAiStep);
        Events.ClientTickEnd.add(this::onTickEnd);
    }

    private void onTickStart() {
        targets.clear();

        if (placeCountdown > 0) {
            placeCountdown--;
        }
        if (breakCountdown > 0) {
            breakCountdown--;
        }
    }

    private void onAfterPlayerAiStep() {
        CrystalAuraConfig config = ConfigStore.instance.getConfig().crystalAuraConfig;
        if (!config.enabled) {
            return;
        }

        assert mc.level != null;
        assert mc.player != null;

        targets.clear();
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity == mc.player) {
                continue;
            }

            if (!(entity instanceof LivingEntity living)) {
                continue;
            }

            if (entity instanceof Player player && player.isCreative()) {
                continue;
            }

            if (!living.isAlive()) {
                continue;
            }

            if (mc.player.distanceToSqr(entity) > 100) {
                continue;
            }

            targets.add(living);
        }

        if (tryBreakCrystal(config)) {
            breakCountdown = config.breakDelay;
        } else if (tryPlaceCrystal(config)) {
            placeCountdown = config.placeDelay;
        }
    }

    private void onTickEnd() {

    }

    private boolean tryPlaceCrystal(CrystalAuraConfig config) {
        if (!config.autoPlace || targets.isEmpty() || placeCountdown > 0) {
            return false;
        }

        assert mc.level != null;
        assert mc.player != null;
        assert mc.gameMode != null;

        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;

        boolean hasCrystalInMainHand = player.getMainHandItem().is(Items.END_CRYSTAL);
        boolean hasCrystalInOffHand = player.getOffhandItem().is(Items.END_CRYSTAL);
        int crystalSlot = -1;
        if (!hasCrystalInMainHand && !hasCrystalInOffHand) {
            Inventory inventory = player.getInventory();
            for (int i = 0; i < 9; i++) {
                if (inventory.getItem(i).is(Items.END_CRYSTAL)) {
                    crystalSlot = i;
                    break;
                }
            }

            if (crystalSlot == -1) {
                return false;
            }
        }

        Vec3 eyePos = player.getEyePosition();

        // check if there is at least one crystal that's good for blowing up
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof EndCrystal crystal) {
                AABB bb = crystal.getBoundingBox();
                if (inBreakRange(bb, eyePos, config) && calculatePossibleDamage(level, player, crystal.position(), config) > 0) {
                    return false;
                }
            }
        }

        int radius = Mth.ceil(config.placeRange);
        double placeRangeSqr = config.placeRange * config.placeRange;

        float bestDamage = 0;
        BlockPos bestCrystalBlockPos = null;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            pos.setX(player.blockPosition().getX() + dx);
            for (int dy = -radius; dy <= radius; dy++) {
                pos.setY(player.blockPosition().getY() + dy);
                for (int dz = -radius; dz <= radius; dz++) {
                    pos.setZ(player.blockPosition().getZ() + dz);
                    BlockState state = level.getBlockState(pos);
                    if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) {
                        continue;
                    }

                    BlockPos crystalBlockPos = pos.above();
                    if (!level.isEmptyBlock(crystalBlockPos)) {
                        continue;
                    }

                    if (new AABB(crystalBlockPos).distanceToSqr(eyePos) > placeRangeSqr) {
                        continue;
                    }

                    Vec3 crystalEntityPosition = Vec3.atBottomCenterOf(crystalBlockPos);

                    float damage = calculatePossibleDamage(level, player, crystalEntityPosition, config);
                    if (damage <= bestDamage) {
                        continue;
                    }

                    AABB crystalBB = new AABB(
                            crystalBlockPos.getX(), crystalBlockPos.getY(), crystalBlockPos.getZ(),
                            crystalBlockPos.getX() + 1, crystalBlockPos.getY() + 2, crystalBlockPos.getZ() + 1);

                    // TODO: possible optimization - return first entity
                    List<Entity> collisions = level.getEntities(null, crystalBB);
                    if (!collisions.isEmpty()) {
                        continue;
                    }

                    bestCrystalBlockPos = crystalBlockPos;
                    bestDamage = damage;
                }
            }
        }

        if (bestCrystalBlockPos == null) {
            return false;
        }

        // TODO: auto-rotation

        // TODO: proper HitResult calculation
        BlockHitResult hit = new BlockHitResult(
                Vec3.atBottomCenterOf(bestCrystalBlockPos),
                Direction.UP,
                bestCrystalBlockPos.below(),
                false);

        InteractionResult result;
        if (hasCrystalInMainHand) {
            result = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        } else if (hasCrystalInOffHand) {
            result = mc.gameMode.useItemOn(player, InteractionHand.OFF_HAND, hit);
        } else {
            player.getInventory().setSelectedSlot(crystalSlot);
            result = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
        }

        return result.consumesAction();
    }

    private boolean tryBreakCrystal(CrystalAuraConfig config) {
        if (!config.autoBreak || targets.isEmpty() || breakCountdown > 0) {
            return false;
        }

        assert mc.level != null;
        assert mc.player != null;
        assert mc.gameMode != null;

        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        Vec3 eyePos = player.getEyePosition();

        EndCrystal bestCrystal = null;
        float bestDamage = 0;
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal crystal)) {
                continue;
            }
            if (crystal.tickCount < config.crystalAge) {
                continue;
            }

            if (!inBreakRange(crystal.getBoundingBox(), eyePos, config)) {
                continue;
            }

            float damage = calculatePossibleDamage(level, player, crystal.position(), config);
            if (damage > bestDamage) {
                bestCrystal = crystal;
                bestDamage = damage;
            }
        }

        if (bestCrystal == null) {
            return false;
        }

        // TODO: auto rotate here, and trigger attack from another event, once client sends position/rotation to the server
        mc.gameMode.attack(player, bestCrystal);
        return true;
    }

    private boolean inBreakRange(AABB crystalBB, Vec3 eyePos, CrystalAuraConfig config) {
        return crystalBB.distanceToSqr(eyePos) <= config.breakRange * config.breakRange;
    }

    // returns 0 if this EndCrystal is not good for blowing up
    private float calculatePossibleDamage(ClientLevel level, LocalPlayer player, Vec3 crystalPos, CrystalAuraConfig config) {
        // TODO: think if sum is better than max
        float targetDamage = 0;
        for (LivingEntity target : targets) {
            targetDamage += DamageUtils.calculateEndCrystalDamage(level, crystalPos, target);
        }

        if (targetDamage < config.minTargetDamage) {
            return 0;
        }

        float selfDamage = DamageUtils.calculateEndCrystalDamage(level, crystalPos, player);
        if (selfDamage > config.maxSelfDamage) {
            return 0;
        }

        return targetDamage;
    }
}