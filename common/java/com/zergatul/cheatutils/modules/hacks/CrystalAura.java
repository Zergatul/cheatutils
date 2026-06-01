package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import com.zergatul.cheatutils.controllers.FakeRotation;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.DamageUtils;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
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
    private final List<EndCrystal> spawnedCrystals = new ArrayList<>();
    private final Int2IntMap recentlyAttackedCrystals = new Int2IntArrayMap();
    private int placeCountdown;
    private int breakCountdown;
    private Runnable afterPositionSentAction;

    private CrystalAura() {
        Events.BeforeProcessQueuedPackets.add(this::onBeforeProcessQueuedPackets);
        Events.EntityAdded.add(this::onEntityAdded);
        Events.AfterProcessQueuedPackets.add(this::onAfterProcessQueuedPackets);
        Events.ClientTickStart.add(this::onTickStart);
        Events.AfterPlayerAiStep.add(this::onAfterPlayerAiStep);
        Events.AfterSendPlayerPos.add(this::onAfterSendPlayerPos);
        Events.ClientTickEnd.add(this::onTickEnd);
    }

    public void onEnableStateChanged() {
        targets.clear();
        spawnedCrystals.clear();
        recentlyAttackedCrystals.clear();
        placeCountdown = 0;
        breakCountdown = 0;
        afterPositionSentAction = null;
    }

    private void onBeforeProcessQueuedPackets() {
        spawnedCrystals.clear();
    }

    private void onEntityAdded(Entity entity) {
        CrystalAuraConfig config = ConfigStore.instance.getConfig().crystalAuraConfig;
        if (!config.enabled) {
            return;
        }

        // TODO: for now Fast-Break is disabled when Auto-Rotate is ON
        if (!config.autoBreak || !config.fastBreak || config.autoRotate) {
            return;
        }

        if (!(entity instanceof EndCrystal crystal)) {
            return;
        }

        spawnedCrystals.add(crystal);
    }

    private void onAfterProcessQueuedPackets() {
        if (spawnedCrystals.isEmpty()) {
            return;
        }

        assert mc.level != null;
        assert mc.player != null;
        assert mc.gameMode != null;

        CrystalAuraConfig config = ConfigStore.instance.getConfig().crystalAuraConfig;

        findTargets();
        if (targets.isEmpty()) {
            return;
        }

        for (EndCrystal crystal : spawnedCrystals) {
            if (recentlyAttackedCrystals.containsKey(crystal.getId())) {
                // should not happen
                continue;
            }

            if (!inBreakRange(crystal.getBoundingBox(), mc.player.getEyePosition(), config)) {
                continue;
            }

            float damage = calculatePossibleDamage(mc.level, mc.player, crystal.position(), config);
            if (damage == 0) {
                continue;
            }

            mc.gameMode.attack(mc.player, crystal);
            recentlyAttackedCrystals.put(crystal.getId(), 0);
            break;
        }

        targets.clear();
    }

    private void onTickStart() {
        targets.clear();
        afterPositionSentAction = null;

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

        findTargets();
        tickRecentlyAttackedCrystals();

        if (tryBreakCrystal(config)) {
            breakCountdown = config.breakDelay;
        } else if (tryPlaceCrystal(config)) {
            placeCountdown = config.placeDelay;
        }
    }

    private void onAfterSendPlayerPos() {
        if (afterPositionSentAction != null) {
            afterPositionSentAction.run();
            afterPositionSentAction = null;
        }
    }

    private void onTickEnd() {

    }

    private void findTargets() {
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
    }

    private void tickRecentlyAttackedCrystals() {
        ObjectIterator<Int2IntMap.Entry> iterator = recentlyAttackedCrystals.int2IntEntrySet().iterator();
        while (iterator.hasNext()) {
            Int2IntMap.Entry entry = iterator.next();
            int ticks = entry.getIntValue();
            if (ticks > 3) {
                iterator.remove();
            } else {
                entry.setValue(ticks + 1);
            }
        }
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

        if (!hasCrystalOnHotbar(player)) {
            return false;
        }

        Vec3 eyePos = player.getEyePosition();

        // check if there is at least one crystal that's good for blowing up
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof EndCrystal crystal) {
                if (recentlyAttackedCrystals.containsKey(crystal.getId())) {
                    continue;
                }
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

                    if (new AABB(pos).distanceToSqr(eyePos) > placeRangeSqr) {
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

        if (config.autoRotate) {
            // look at support center for now
            FakeRotation.instance.setServerRotation(Vec3.atCenterOf(bestCrystalBlockPos.below()));
        }

        // TODO: proper HitResult calculation
        BlockHitResult hit = new BlockHitResult(
                Vec3.atBottomCenterOf(bestCrystalBlockPos),
                Direction.UP,
                bestCrystalBlockPos.below(),
                false);

        afterPositionSentAction = () -> {
            InteractionResult result;
            if (player.getMainHandItem().is(Items.END_CRYSTAL)) {
                result = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
            } else if (player.getOffhandItem().is(Items.END_CRYSTAL)) {
                result = mc.gameMode.useItemOn(player, InteractionHand.OFF_HAND, hit);
            } else {
                Inventory inventory = player.getInventory();
                for (int i = 0; i < 9; i++) {
                    if (inventory.getItem(i).is(Items.END_CRYSTAL)) {
                        inventory.setSelectedSlot(i);
                        break;
                    }
                }
                if (!player.getMainHandItem().is(Items.END_CRYSTAL)) {
                    // rollback countdown, crystals disappeared from hotbar
                    placeCountdown = 0;
                    return;
                }

                result = mc.gameMode.useItemOn(player, InteractionHand.MAIN_HAND, hit);
            }
            if (!result.consumesAction()) {
                // rollback countdown, since action failed on client
                placeCountdown = 0;
            }
        };

        return true;
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
            if (recentlyAttackedCrystals.containsKey(crystal.getId())) {
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

        if (config.autoRotate) {
            // look at bottom-center position for now
            FakeRotation.instance.setServerRotation(bestCrystal.position());
        }

        EndCrystal crystal = bestCrystal;
        afterPositionSentAction = () -> {
            mc.gameMode.attack(player, crystal);
            recentlyAttackedCrystals.put(crystal.getId(), 0);
        };

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

    private boolean hasCrystalOnHotbar(LocalPlayer player) {
        if (player.getMainHandItem().is(Items.END_CRYSTAL)) {
            return true;
        }
        if (player.getOffhandItem().is(Items.END_CRYSTAL)) {
            return true;
        }

        Inventory inventory = player.getInventory();
        for (int i = 0; i < 9; i++) {
            if (inventory.getItem(i).is(Items.END_CRYSTAL)) {
                return true;
            }
        }

        return false;
    }
}