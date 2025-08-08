package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.blocks.BlockBreakPlan;
import com.zergatul.cheatutils.blocks.BlockBreaker;
import com.zergatul.cheatutils.blocks.BlockPlacePlan;
import com.zergatul.cheatutils.blocks.BlockPlacer;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.BlockAutomationConfig;
import com.zergatul.cheatutils.modules.automation.VillagerRoller;
import com.zergatul.cheatutils.scripting.events.BlockPosConsumer;
import com.zergatul.cheatutils.blocks.BlockPlacingMethod;
import com.zergatul.cheatutils.utils.NearbyBlockEnumerator;
import com.zergatul.cheatutils.utils.SlotSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

public class BlockAutomation {

    public static final BlockAutomation instance = new BlockAutomation();

    private final Minecraft mc = Minecraft.getInstance();
    private final SlotSelector slotSelector = new SlotSelector();
    private BlockPosConsumer script;
    private Predicate<ItemStack> useItemPredicate;
    private InteractionHand hand;
    private BlockPlacingMethod method;
    private boolean breakCurrentBlock;
    private Predicate<ItemStack> breakItemPredicate;
    private BlockBreakPlan breakPlan;
    private BlockPlacePlan placePlan;
    private volatile boolean debugStep;
    private double actionTickCounter;
    private CompletableFuture<Void> applyFuture;

    private BlockAutomation() {
        Events.AfterPlayerAiStep.add(this::onAfterPlayerAiStep);
        Events.AfterRenderWorld.add(this::onRenderWorldLast);
    }

    public void setScript(BlockPosConsumer script) {
        this.script = script;
    }

    public void useItem(Predicate<ItemStack> predicate, BlockPlacingMethod method) {
        this.hand = null;
        this.useItemPredicate = predicate;
        this.method = method;
    }

    public void useItem(InteractionHand hand, BlockPlacingMethod method) {
        this.useItemPredicate = null;
        this.hand = hand;
        this.method = method;
    }

    public void breakBlock(Predicate<ItemStack> predicate) {
        this.breakCurrentBlock = true;
        this.breakItemPredicate = predicate;
    }

    public void placeOne() {
        debugStep = true;
    }

    public boolean isBreakingBlock() {
        BlockAutomationConfig config = ConfigStore.instance.getConfig().blockAutomationConfig;
        if (!config.enabled) {
            return false;
        }
        if (script == null) {
            return false;
        }

        return breakPlan != null;
    }

    private void onAfterPlayerAiStep() {
        BlockAutomationConfig config = ConfigStore.instance.getConfig().blockAutomationConfig;
        // Block Automation code stops breaking lectern after first tick for Villager Roller
        if (!config.enabled || VillagerRoller.instance.isActive()) {
            resetState();
            return;
        }

        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            resetState();
            return;
        }

        if (script == null) {
            resetState();
            return;
        }

        Vec3 eyePos = mc.player.getEyePosition(1);
        actionTickCounter += 1 / config.placementRate;
        if (applyFuture != null) {
            if (applyFuture.isDone()) {
                breakPlan = null;
                placePlan = null;
                applyFuture = null;
            } else {
                // block action is in progress
                if (actionTickCounter > 1) {
                    // don't accumulate too much while action is in progress
                    actionTickCounter = 1;
                }
                return;
            }
        }

        if (actionTickCounter >= 1) {
            actionTickCounter -= 1;

            boolean actionPerformed = false;
            for (BlockPos pos : NearbyBlockEnumerator.getPositions(eyePos, config.maxRange)) {
                useItemPredicate = null;
                hand = null;
                breakCurrentBlock = false;
                breakItemPredicate = null;
                script.accept(pos.getX(), pos.getY(), pos.getZ());

                if (breakCurrentBlock && !mc.level.isEmptyBlock(pos) && selectItemForBlockBreak(config)) {
                    breakPlan = BlockBreaker.createPlan(pos, config);
                    applyFuture = breakPlan.apply();
                    actionPerformed = true;
                    break;
                } else if (hand != null) {
                    placePlan = BlockPlacer.createPlan(Blocks.STONE.defaultBlockState(), pos, method, config);
                    if (placePlan != null) {
                        if (config.debugMode && !debugStep) {
                            //debugPlan = plan; // TODO: test after actions per tick change?
                        } else {
                            debugStep = false;
                            applyFuture = placePlan.apply(hand);
                            actionPerformed = true;
                        }
                        break;
                    }
                } else if (useItemPredicate != null) {
                    int slot = slotSelector.selectItem(config, useItemPredicate);
                    if (slot < 0) {
                        continue;
                    }

                    placePlan = BlockPlacer.createPlan(Blocks.STONE.defaultBlockState(), pos, method, config);
                    if (placePlan != null) {
                        if (config.debugMode && !debugStep) {
                            //debugPlan = plan; // TODO: test after actions per tick change?
                            break;
                        } else {
                            debugStep = false;
                            mc.player.getInventory().setSelectedSlot(slot);
                            applyFuture = placePlan.apply();
                            actionPerformed = true;
                            break;
                        }
                    }
                }
            }

            if (!actionPerformed) {
                actionTickCounter = 1 - 1 / config.placementRate;
            }
        }
    }

    private void onRenderWorldLast(RenderWorldLastEvent event) {
        BlockAutomationConfig config = ConfigStore.instance.getConfig().blockAutomationConfig;
//        if (config.enabled && config.debugMode && debugPlan != null) {
//            // draw neighbour block
//            LineRenderer renderer = RenderUtilities.instance.getLineRenderer();
//
//            renderer.begin(event, false);
//            double x1 = debugPlan.neighbour().getX();
//            double y1 = debugPlan.neighbour().getY();
//            double z1 = debugPlan.neighbour().getZ();
//            double x2 = x1 + 1;
//            double y2 = y1 + 1;
//            double z2 = z1 + 1;
//            renderer.cuboid(x1, y1, z1, x2, y2, z2, 1f, 1f, 1f, 1f);
//            renderer.end();
//
//            // draw target block
//            renderer.begin(event, false);
//            x1 = debugPlan.destination().getX() + 0.05;
//            y1 = debugPlan.destination().getY() + 0.05;
//            z1 = debugPlan.destination().getZ() + 0.05;
//            x2 = x1 + 0.9;
//            y2 = y1 + 0.9;
//            z2 = z1 + 0.9;
//            renderer.cuboid(x1, y1, z1, x2, y2, z2, 0.7f, 1f, 0.7f, 1f);
//            renderer.end();
//
//            // draw target point
//            renderer.begin(event, false);
//            for (Direction direction : Direction.values()) {
//                Vec3 p1 = debugPlan.target().relative(direction, 0.1);
//                Vec3 p2 = debugPlan.target().relative(direction, -0.1);
//                renderer.line(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, 1f, 1f, 0.7f, 1f);
//            }
//            renderer.end();
//        }
    }

    private boolean selectItemForBlockBreak(BlockAutomationConfig config) {
        assert mc.player != null;

        int slot = slotSelector.selectItem(config, breakItemPredicate);
        if (slot >= 0) {
            mc.player.getInventory().setSelectedSlot(slot);
            return true;
        } else {
            return false;
        }
    }

    private void resetState() {
        actionTickCounter = 0;
        breakPlan = null;
        placePlan = null;
        applyFuture = null;
    }
}