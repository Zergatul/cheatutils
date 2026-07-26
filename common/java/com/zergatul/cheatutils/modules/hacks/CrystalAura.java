package com.zergatul.cheatutils.modules.hacks;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import com.zergatul.cheatutils.controllers.FakeRotation;
import com.zergatul.cheatutils.mixins.common.accessors.MultiPlayerGameModeAccessor;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.utils.*;
import it.unimi.dsi.fastutil.ints.Int2IntArrayMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

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
    private final CrystalAuraDamageCalculator calculator = new FastCrystalAuraDamageCalculator();
    private CrystalAuraConfig config; // we snapshot config, so it doesn't get update mid-process
    private int placeCountdown;
    private int breakCountdown;
    private Runnable afterPositionSentAction;
    private CompletableFuture<Boolean> supportPlacingProcess;

    private CrystalAura() {
        Events.BeforeProcessQueuedPackets.add(this::onBeforeProcessQueuedPackets);
        Events.EntityAdded.add(this::onEntityAdded);
        Events.AfterProcessQueuedPackets.add(this::onAfterProcessQueuedPackets);
        Events.InGameTickStart.add(this::onTickStart);
        Events.AfterPlayerAiStep.add(this::onAfterPlayerAiStep);
        Events.AfterSendPlayerPos.add(this::onAfterSendPlayerPos);
        Events.InGameTickEnd.add(this::onTickEnd);
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
        config = ConfigStore.instance.getConfig().crystalAuraConfig;

        if (mc.level == null || mc.player == null) {
            return;
        }

        spawnedCrystals.clear();
        if (isFastBreakEnabled()) {
            calculator.begin(mc.level, config, mc.player.getEyePosition());
        }
    }

    private void onEntityAdded(Entity entity) {
        if (!isFastBreakEnabled()) {
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

        if (!isFastBreakEnabled()) {
            return;
        }

        if (spawnedCrystals.isEmpty()) {
            calculator.end();
            return;
        }

        assert mc.gameMode != null;

        findTargets();
        if (targets.isEmpty()) {
            calculator.end();
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

            float damage = calculator.calculatePossibleDamage(mc.player, targets, crystal.position());
            if (damage == 0) {
                continue;
            }

            mc.gameMode.attack(mc.player, crystal);
            recentlyAttackedCrystals.put(crystal.getId(), 0);
            break;
        }

        targets.clear();
        calculator.end();
    }

    private void onTickStart() {
        config = ConfigStore.instance.getConfig().crystalAuraConfig;
        if (!config.enabled) {
            return;
        }

        assert mc.level != null;
        assert mc.player != null;

        calculator.begin(mc.level, config, mc.player.getEyePosition());
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

        if (tryBreakCrystal()) {
            breakCountdown = config.breakDelay;
        } else if (tryPlaceCrystal()) {
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
        calculator.end();
    }

    private void findTargets() {
        assert mc.level != null;
        assert mc.player != null;
        assert mc.gameMode != null;

        targets.clear();
        if (isPaused(mc.player, mc.gameMode)) {
            return;
        }

        double maxRange = config.breakRange + DamageUtils.END_CRYSTAL_EXPLOSION_RADIUS;
        double maxRangeSqr = maxRange * maxRange;
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

            if (mc.player.getEyePosition().distanceToSqr(entity.position()) > maxRangeSqr) {
                continue;
            }

            for (EntityType<?> type : config.targets) {
                if (entity.getType() == type) {
                    targets.add(living);
                    break;
                }
            }
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

    private boolean tryPlaceCrystal() {
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
                if (inBreakRange(bb, eyePos, config) && calculator.calculatePossibleDamage(player, targets, crystal.position()) > 0) {
                    return false;
                }
            }
        }

        BlockPos crystalPos = CrystalAuraPositionCalculator.calculatePotentialPlacement(
                level,
                player,
                eyePos,
                targets,
                calculator,
                config);
        if (crystalPos == null) {
            if (!config.autoPlaceSupport) {
                return false;
            }

            Optional<HotbarSlot> slot = HotbarUtils.findItem(player, Items.OBSIDIAN);
            if (slot.isEmpty()) {
                return false;
            }

            CrystalAuraPositionCalculator.SupportPlacement sp = CrystalAuraPositionCalculator.calculatePotentialSupportPlacement(
                    level,
                    player,
                    eyePos,
                    targets,
                    calculator,
                    config);
            if (sp == null) {
                return false;
            }

            // theoretically selected item may get changed by the time of sending UseItemOn packet
            // but this is too exotic case for now
            // also for simplicity we forget about potential crystal placement, and hope next tryPlaceCrystal call
            // picks up this spot, or chooses better one
            supportPlacingProcess = sp.placePlan().apply(slot.get(), isHotbarSilentSwitch());
            placeCountdown = config.placeSupportDelay;
            return false;
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
            Optional<HotbarSlot> slot = HotbarUtils.findItem(player, Items.END_CRYSTAL);
            if (slot.isEmpty()) {
                // rollback countdown: crystals disappeared from hotbar
                placeCountdown = 0;
                return;
            }

            int uiSlot = -1;
            if (slot.get().getHand() == InteractionHand.MAIN_HAND) {
                if (isHotbarSilentSwitch()) {
                    if (slot.get().getSlot() != player.getInventory().getSelectedSlot()) {
                        uiSlot = player.getInventory().getSelectedSlot();
                    }
                }
                player.getInventory().setSelectedSlot(slot.get().getSlot());
            }

            InteractionResult result = mc.gameMode.useItemOn(player, slot.get().getHand(), hit);

            if (uiSlot >= 0) {
                player.getInventory().setSelectedSlot(uiSlot);
            }

            if (!result.consumesAction()) {
                // rollback countdown: action failed on client
                placeCountdown = 0;
            }
        };

        return true;
    }

    private boolean tryBreakCrystal() {
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

            float damage = calculator.calculatePossibleDamage(player, targets, crystal.position());
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

    private boolean isPaused(LocalPlayer player, MultiPlayerGameMode gameMode) {
        if (config.pauseOnItemUse && player.isUsingItem()) {
            return true;
        }

        if (config.pauseOnMining) {
            MultiPlayerGameModeAccessor mode = (MultiPlayerGameModeAccessor) gameMode;
            if (mode.getIsDestroying_CU()) {
                return true;
            }
        }

        return false;
    }

    private boolean inBreakRange(AABB crystalBB, Vec3 eyePos, CrystalAuraConfig config) {
        return crystalBB.distanceToSqr(eyePos) <= config.breakRange * config.breakRange;
    }

    private boolean isFastBreakEnabled() {
        // TODO: for now Fast-Break is disabled when Auto-Rotate is ON
        return config.enabled && config.autoBreak && config.fastBreak && !config.autoRotate;
    }

    private boolean isHotbarSilentSwitch() {
        return CrystalAuraConfig.HOTBAR_SWITCH_SILENT.equals(config.hotbarSwitchMode);
    }
}