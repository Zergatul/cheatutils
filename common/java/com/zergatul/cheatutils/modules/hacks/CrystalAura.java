package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.blocks.BlockPlacePlan;
import com.zergatul.cheatutils.blocks.BlockPlacer;
import com.zergatul.cheatutils.blocks.BlockPlacingMethod;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import com.zergatul.cheatutils.configs.InteractionConfig;
import com.zergatul.cheatutils.controllers.FakeRotation;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.DamageUtils;
import com.zergatul.cheatutils.utils.EndCrystalCalculationCache;
import com.zergatul.cheatutils.utils.HotbarUtils;
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
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class CrystalAura implements Module {

    public static final CrystalAura instance = new CrystalAura();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<LivingEntity> targets = new ArrayList<>();
    private final List<EndCrystal> spawnedCrystals = new ArrayList<>();
    private final Int2IntMap recentlyAttackedCrystals = new Int2IntArrayMap();
    private final EndCrystalCalculationCache cache = new EndCrystalCalculationCache();
    private int placeCountdown;
    private int breakCountdown;
    private Runnable afterPositionSentAction;
    private CompletableFuture<Void> supportPlacingProcess;

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
        supportPlacingProcess = null;
    }

    private void onBeforeProcessQueuedPackets() {
        if (mc.level == null || mc.player == null) {
            return;
        }

        spawnedCrystals.clear();
        cache.begin(mc.level, getLevelSliceRadius(), mc.player.getEyePosition());
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

        if (supportPlacingProcess != null && !supportPlacingProcess.isDone()) {
            return;
        }

        spawnedCrystals.add(crystal);
    }

    private void onAfterProcessQueuedPackets() {
        if (mc.level == null || mc.player == null) {
            return;
        }

        if (spawnedCrystals.isEmpty()) {
            cache.end();
            return;
        }

        assert mc.gameMode != null;

        CrystalAuraConfig config = ConfigStore.instance.getConfig().crystalAuraConfig;

        findTargets();
        if (targets.isEmpty()) {
            cache.end();
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

            float damage = calculatePossibleDamage(mc.player, crystal.position(), config);
            if (damage == 0) {
                continue;
            }

            mc.gameMode.attack(mc.player, crystal);
            recentlyAttackedCrystals.put(crystal.getId(), 0);
            break;
        }

        targets.clear();
        cache.end();
    }

    private void onTickStart() {
        assert mc.level != null;
        assert mc.player != null;

        cache.begin(mc.level, getLevelSliceRadius(), mc.player.getEyePosition());
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

        tickRecentlyAttackedCrystals();

        if (supportPlacingProcess != null && supportPlacingProcess.isDone()) {
            supportPlacingProcess = null;
        }
        if (supportPlacingProcess != null) {
            return;
        }

        findTargets();

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
        cache.end();
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

        if (!HotbarUtils.hasItem(player, Items.END_CRYSTAL)) {
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
                if (inBreakRange(bb, eyePos, config) && calculatePossibleDamage(player, crystal.position(), config) > 0) {
                    return false;
                }
            }
        }

        BlockPos crystalPos;
        PotentialPlacement placement = calculatePotentialPlacement(level, player, eyePos, config);
        if (placement == null) {
            if (!config.autoPlaceSupport) {
                return false;
            }

            PotentialSupportPlacement psp = calculatePotentialSupportPlacement(level, player, eyePos, config);
            if (psp == null) {
                return false;
            }

            Optional<InteractionHand> hand = HotbarUtils.selectItem(player, Items.OBSIDIAN);
            if (hand.isEmpty()) {
                return false;
            }

            // theoretically selected item may get changed by the time of sending UseItemOn packet
            // but this is too exotic case for now
            // also for simplicity we forget about potential crystal placement, and hope next tryPlaceCrystal call
            // picks up this spot, or chooses better one
            supportPlacingProcess = psp.placePlan.apply(hand.get());
            placeCountdown = config.placeSupportDelay;
            return false;
        } else {
            crystalPos = placement.crystalPos;
        }

        if (config.autoRotate) {
            // look at support center for now
            FakeRotation.instance.setServerRotation(Vec3.atCenterOf(crystalPos.below()));
        }

        // TODO: proper HitResult calculation
        BlockHitResult hit = new BlockHitResult(
                Vec3.atBottomCenterOf(crystalPos),
                Direction.UP,
                crystalPos.below(),
                false);

        afterPositionSentAction = () -> {
            Optional<InteractionHand> hand = HotbarUtils.selectItem(player, Items.END_CRYSTAL);
            if (hand.isEmpty()) {
                // rollback countdown: crystals disappeared from hotbar
                placeCountdown = 0;
                return;
            }

            InteractionResult result = mc.gameMode.useItemOn(player, hand.get(), hit);
            if (!result.consumesAction()) {
                // rollback countdown: action failed on client
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

            float damage = calculatePossibleDamage(player, crystal.position(), config);
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

    private @Nullable PotentialPlacement calculatePotentialPlacement(ClientLevel level, LocalPlayer player, Vec3 eyePos, CrystalAuraConfig config) {
        int radius = Mth.ceil(config.placeRange);
        double placeRangeSqr = config.placeRange * config.placeRange;
        BlockGetter levelSlice = cache.getBlockGetter();

        float bestDamage = 0;
        BlockPos bestCrystalBlockPos = null;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            pos.setX(player.blockPosition().getX() + dx);
            for (int dy = -radius; dy <= radius; dy++) {
                pos.setY(player.blockPosition().getY() + dy);
                for (int dz = -radius; dz <= radius; dz++) {
                    pos.setZ(player.blockPosition().getZ() + dz);
                    BlockState state = levelSlice.getBlockState(pos);
                    if (!state.is(Blocks.OBSIDIAN) && !state.is(Blocks.BEDROCK)) {
                        continue;
                    }

                    BlockPos crystalBlockPos = pos.above();
                    if (!levelSlice.getBlockState(crystalBlockPos).isAir()) {
                        continue;
                    }

                    if (new AABB(pos).distanceToSqr(eyePos) > placeRangeSqr) {
                        continue;
                    }

                    Vec3 crystalEntityPosition = Vec3.atBottomCenterOf(crystalBlockPos);

                    float damage = calculatePossibleDamage(player, crystalEntityPosition, config);
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

        if (bestCrystalBlockPos != null) {
            return new PotentialPlacement(bestCrystalBlockPos);
        } else {
            return null;
        }
    }

    private @Nullable PotentialSupportPlacement calculatePotentialSupportPlacement(ClientLevel level, LocalPlayer player, Vec3 eyePos, CrystalAuraConfig config) {
        if (!HotbarUtils.hasItem(player, Items.OBSIDIAN)) {
            return null;
        }

        int radius = Mth.ceil(config.placeRange);
        double placeRangeSqr = config.placeRange * config.placeRange;

        float bestDamage = 0;
        BlockPos bestSupportPos = null;
        BlockPlacePlan bestSupportPlacePlan = null;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            pos.setX(player.blockPosition().getX() + dx);
            for (int dy = -radius; dy <= radius; dy++) {
                pos.setY(player.blockPosition().getY() + dy);
                for (int dz = -radius; dz <= radius; dz++) {
                    pos.setZ(player.blockPosition().getZ() + dz);
                    BlockState state = level.getBlockState(pos);
                    if (!state.canBeReplaced()) {
                        continue;
                    }

                    BlockPos crystalBlockPos = pos.above();
                    if (!level.isEmptyBlock(crystalBlockPos)) {
                        continue;
                    }

                    if (new AABB(pos).distanceToSqr(eyePos) > placeRangeSqr) {
                        continue;
                    }

                    // bounding box for support block + crystal placement check
                    AABB bb = new AABB(
                            pos.getX(), pos.getY(), pos.getZ(),
                            pos.getX() + 1, pos.getY() + 3, pos.getZ() + 1);

                    // TODO: possible optimization - return first entity
                    List<Entity> collisions = level.getEntities(null, bb);
                    if (!collisions.isEmpty()) {
                        continue;
                    }

                    BlockPlacePlan plan = BlockPlacer.createPlan(
                            Blocks.OBSIDIAN.defaultBlockState(),
                            pos.immutable(),
                            BlockPlacingMethod.ANY,
                            new SupportPlacementInteractionConfig(config));
                    if (plan == null) {
                        continue;
                    }

                    Vec3 crystalEntityPosition = Vec3.atBottomCenterOf(crystalBlockPos);

                    float damage = calculatePossibleDamage(level, player, crystalEntityPosition, pos, config);
                    if (damage <= bestDamage) {
                        continue;
                    }

                    bestSupportPos = pos.immutable();
                    bestDamage = damage;
                    bestSupportPlacePlan = plan;
                }
            }
        }

        if (bestSupportPos == null) {
            return null;
        }

        return new PotentialSupportPlacement(bestSupportPlacePlan, bestSupportPos);
    }

    private boolean inBreakRange(AABB crystalBB, Vec3 eyePos, CrystalAuraConfig config) {
        return crystalBB.distanceToSqr(eyePos) <= config.breakRange * config.breakRange;
    }

    // returns 0 if this EndCrystal is not good for blowing up
    private float calculatePossibleDamage(LocalPlayer player, Vec3 crystalPos, CrystalAuraConfig config) {
        // TODO: think if sum is better than max
        float targetDamage = 0;
        for (LivingEntity target : targets) {
            targetDamage += DamageUtils.calculateEndCrystalDamage(cache, crystalPos, target);
        }

        if (targetDamage < config.minTargetDamage) {
            return 0;
        }

        float selfDamage = DamageUtils.calculateEndCrystalDamage(cache, crystalPos, player);
        if (selfDamage > config.maxSelfDamage) {
            return 0;
        }

        return targetDamage;
    }

    // returns 0 if this EndCrystal is not good for blowing up
    private float calculatePossibleDamage(
            ClientLevel level,
            LocalPlayer player,
            Vec3 crystalPos,
            BlockPos obsidianPos,
            CrystalAuraConfig config
    ) {
        // TODO: think if sum is better than max
        cache.pushBlockStateOverride(obsidianPos, Blocks.OBSIDIAN.defaultBlockState());
        try {
            float targetDamage = 0;
            for (LivingEntity target : targets) {
                targetDamage += DamageUtils.calculateEndCrystalDamage(cache, crystalPos, target);

            }

            if (targetDamage < config.minTargetDamage) {
                return 0;
            }

            float selfDamage = DamageUtils.calculateEndCrystalDamage(cache, crystalPos, player);
            if (selfDamage > config.maxSelfDamage) {
                return 0;
            }

            return targetDamage;
        } finally {
            cache.popBlockStateOverride();
        }
    }

    private int getLevelSliceRadius() {
        return getLevelSliceRadius(ConfigStore.instance.getConfig().crystalAuraConfig);
    }

    private int getLevelSliceRadius(CrystalAuraConfig config) {
        return Mth.ceil(Math.max(config.placeRange, config.breakRange) + DamageUtils.END_CRYSTAL_EXPLOSION_RADIUS) + 1;
    }

    private record PotentialPlacement(BlockPos crystalPos) {}

    private record PotentialSupportPlacement(BlockPlacePlan placePlan, BlockPos supportPos) {}

    private static class SupportPlacementInteractionConfig implements InteractionConfig {

        private final CrystalAuraConfig config;

        public SupportPlacementInteractionConfig(CrystalAuraConfig config) {
            this.config = config;
        }

        @Override
        public double getMaxRange() {
            return config.placeRange;
        }

        @Override
        public boolean shouldAutoRotate() {
            return config.autoRotate;
        }
    }
}