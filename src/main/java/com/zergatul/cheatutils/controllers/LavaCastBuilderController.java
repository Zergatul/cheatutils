package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

public class LavaCastBuilderController {

    public static final LavaCastBuilderController instance = new LavaCastBuilderController();

    private final Minecraft mc = Minecraft.getInstance();
    private final Logger logger = LogManager.getLogger(LavaCastBuilderController.class);
    //private final Thread thread;
    private final Object loopWaitEvent = new Object();
    private final Object clientTickEvent = new Object();
    private volatile boolean active;
    private State state = State.INVALID;
    private int x, y, z;
    private volatile boolean waitingForBlockBreak;
    private final Object blockBreakEvent = new Object();
    private final Random random = new Random();

    private LavaCastBuilderController() {
        //thread = new Thread(this::buildingLoop);
        //thread.start();
    }

    /*public boolean isActive() {
        return active;
    }

    public boolean activate() {
        if (active) {
            return true;
        }

        // tool - can be anything
        // mc.player.inventory.items.get(0)

        // lava bucket
        if (mc.player.inventory.items.get(1).getItem() != Items.LAVA_BUCKET) {
            return false;
        }

        // water bucket
        if (mc.player.inventory.items.get(2).getItem() != Items.WATER_BUCKET) {
            return false;
        }

        // building blocks
        if (mc.player.inventory.items.get(3).getItem() == Items.AIR) {
            return false;
        }

        active = true;
        state = State.BEGIN;
        x = MathHelper.floor(mc.player.getX());
        y = MathHelper.floor(mc.player.getY());
        z = MathHelper.floor(mc.player.getZ());
        synchronized (loopWaitEvent) {
            loopWaitEvent.notify();
        }

        return true;
    }

    @SubscribeEvent
    public void onChatEvent(ClientChatEvent event) {
        if (event.getMessage().contains("lavacast")) {
            activate();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            synchronized (clientTickEvent) {
                clientTickEvent.notify();
            }
        }
    }

    private void buildingLoop() {
        try {
            while (true) {

                synchronized (loopWaitEvent) {
                    loopWaitEvent.wait();
                }

                try {
                    logger.debug("Starting building loop");

                    while (active) {
                        switch (state) {
                            case BEGIN:
                                logger.debug("Checking surrounding blocks");

                                boolean ok = true;
                                for (int dx = -1; dx <= 1; dx++) {
                                    for (int dz = -1; dz <= 1; dz++) {
                                        if (dx != 0 && dz != 0) {
                                            if (!mc.level.isEmptyBlock(new BlockPos(x + dx, y - 1, z + dz))) {
                                                ok = false;
                                            }
                                        }
                                    }
                                }

                                if (ok) {
                                    state = State.PREPARE_NEXT_LEVEL;
                                } else {
                                    state = State.PILLAR_UP;
                                }

                                break;

                            case PILLAR_UP:
                                jumpAndPlaceBlock();
                                state = State.BEGIN;
                                Thread.sleep(random.nextInt(500));
                                break;

                            case PREPARE_NEXT_LEVEL:
                                jumpAndPlaceBlock();
                                jumpAndPlaceBlock();
                                state = State.PLACE_LAVA;
                                Thread.sleep(random.nextInt(500));
                                break;

                            case PLACE_LAVA:
                                breakBlockUnder();
                                placeLava();
                                waitForLavaFlow();
                                state = State.PLACE_WATER;
                                break;

                            case PLACE_WATER:
                                placeWater();
                                breakStandingBlock();
                                removeLava();
                                placeBlock();
                                jumpAndPlaceBlock();
                                state = State.BEGIN;
                                Thread.sleep(random.nextInt(500));
                                break;

                        }
                    }
                }
                catch (CannotProceedFurtherException e) {
                    logger.debug("Cannot proceed further");
                    active = false;
                }
            }
        }
        catch (InterruptedException e) {
            // do nothing
        }
    }

    private void pickTool() throws InterruptedException {
        logger.debug("Picking tool");
        mc.player.inventory.selected = 0;
    }

    private void pickLava() throws InterruptedException {
        logger.debug("Picking lava");
        mc.player.inventory.selected = 1;
        Thread.sleep(500);
    }

    private void pickWater() throws InterruptedException {
        logger.debug("Picking water");
        mc.player.inventory.selected = 2;
        Thread.sleep(500);
    }

    private void pickBuildingBlock() throws CannotProceedFurtherException, InterruptedException {
        logger.debug("Picking building block");

        for (int i = 3; i < PlayerInventory.getSelectionSize(); i++) {
            if (mc.player.inventory.items.get(3).getItem() != Items.AIR) {
                mc.player.inventory.selected = i;
                Thread.sleep(500);
                return;
            }
        }

        // no items left
        throw new CannotProceedFurtherException();
    }

    private void placeBlock() throws InterruptedException, CannotProceedFurtherException {
        pickBuildingBlock();

        LookAtCommand.instance.invoke(x + 0.4 + random.nextDouble() * 0.2, y - 1, z + 0.4 + random.nextDouble() * 0.2);

        logger.debug("Placing block");
        KeyBinding keyUse = mc.options.keyUse.getKeyBinding();
        KeyBinding.set(keyUse.getKey(), true);

        ItemStack itemStack = mc.player.getItemInHand(Hand.MAIN_HAND);
        Block block = ((BlockItem)itemStack.getItem()).getBlock();
        WaitForBlockState.instance.invoke(x, y - 1, z, block);
    }

    private void jumpAndPlaceBlock() throws InterruptedException, CannotProceedFurtherException {
        pickBuildingBlock();

        LookAtCommand.instance.invoke(x + 0.4 + random.nextDouble() * 0.2, y, z + 0.4 + random.nextDouble() * 0.2);

        logger.debug("Jump");
        KeyBinding keyJump = mc.options.keyJump.getKeyBinding();
        KeyBinding keyUse = mc.options.keyUse.getKeyBinding();
        KeyBinding.set(keyJump.getKey(), true);
        KeyBinding.set(keyUse.getKey(), true);

        ItemStack itemStack = mc.player.getItemInHand(Hand.MAIN_HAND);
        Block block = ((BlockItem)itemStack.getItem()).getBlock();
        WaitForBlockState.instance.invoke(x, y, z, block);

        KeyBinding.set(keyJump.getKey(), false);
        KeyBinding.set(keyUse.getKey(), false);

        waitNoYChange();

        if (mc.player.getY() == y + 1) {
            logger.debug("Successfully placed block");
            y++;
        } else {
            logger.debug("Block wasn't placed");
            throw new CannotProceedFurtherException();
        }
    }

    private void breakStandingBlock() throws InterruptedException {
        pickTool();

        LookAtCommand.instance.invoke(x + 0.4 + random.nextDouble() * 0.2, y, z + 0.4 + random.nextDouble() * 0.2);
        Thread.sleep(500);

        logger.debug("Breaking block");

        KeyBinding keyAttack = mc.options.keyAttack.getKeyBinding();
        keyAttack.setDown(true);
        WaitForBlockState.instance.invoke(x, y - 1, z, Blocks.AIR);
        keyAttack.setDown(false);

        logger.debug("Block destroyed");

        y--;
    }

    private void breakBlockUnder() throws InterruptedException, CannotProceedFurtherException {
        pickTool();

        MoveToCommand.instance.invoke(x + 0.1 + random.nextDouble() * 0.8, y, z + 1.25, true);
        Thread.sleep(300 + random.nextInt(100));
        LookAtCommand.instance.invoke(x + 0.1 + random.nextDouble() * 0.8, y - 1.5, z + 1);
        Thread.sleep(300 + random.nextInt(100));

        logger.debug("Breaking block");

        KeyBinding keyAttack = mc.options.keyAttack.getKeyBinding();
        keyAttack.setDown(true);
        WaitForBlockState.instance.invoke(x, y - 2, z, Blocks.AIR);
        keyAttack.setDown(false);

        logger.debug("Block destroyed");
    }

    private void placeLava() throws InterruptedException, CannotProceedFurtherException {
        pickLava();

        double dx = 0;
        double dz = 0;
        if (random.nextBoolean()) {
            if (random.nextBoolean()) {
                dx = -1;
            } else {
                dx = 1;
            }
        } else {
            if (random.nextBoolean()) {
                dz = -1;
            } else {
                dz = 1;
            }
        }

        if (dx == 0) {
            MoveToCommand.instance.invoke(x + 0.1 + random.nextDouble() * 0.8, y, z + 0.5 + dz * 0.75, true);
            Thread.sleep(300 + random.nextInt(100));

            LookAtCommand.instance.invoke(x + 0.1 + random.nextDouble() * 0.8, y - 2, z + 0.5 + dz * 0.45);
            Thread.sleep(300 + random.nextInt(100));
        } else {
            MoveToCommand.instance.invoke(x + 0.5 + dx * 0.75, y, z + 0.1 + random.nextDouble() * 0.8, true);
            Thread.sleep(300 + random.nextInt(100));

            LookAtCommand.instance.invoke(x + 0.5 + dx * 0.45, y - 2, z + 0.1 + random.nextDouble() * 0.8);
            Thread.sleep(300 + random.nextInt(100));
        }

        if (!EnsureLookingAtBlockCommand.instance.invoke(x, y - 3, z)) {
            logger.debug("Look at validation failed");
            throw new CannotProceedFurtherException();
        }

        logger.debug("Placing lava");
        KeyBinding keyUse = mc.options.keyUse.getKeyBinding();
        keyUse.setDown(true);
        Thread.sleep(50 + random.nextInt(100));
        keyUse.setDown(false);

        WaitForBlockState.instance.invoke(x, y - 2, z, Blocks.LAVA);

        logger.debug("Lava placed");
    }

    private void waitForLavaFlow() throws InterruptedException, CannotProceedFurtherException {
        MoveToCommand.instance.invoke(x + 0.5, y, z + 0.5, true);
        logger.debug("Waiting for no block updates");
        WaitForNoBlockUpdates.instance.invoke(5000, Blocks.LAVA);
        logger.debug("No new lava flows");
    }

    private void placeWater() throws InterruptedException {
        pickWater();

        LookAtCommand.instance.invoke(x + 0.1 + random.nextDouble() * 0.8, y, z + 0.1 + random.nextDouble() * 0.8);
        Thread.sleep(300 + random.nextInt(100));

        logger.debug("Placing water");
        KeyBinding keyUse = mc.options.keyUse.getKeyBinding();
        keyUse.setDown(true);
        Thread.sleep(50 + random.nextInt(100));
        keyUse.setDown(false);

        WaitForBlockState.instance.invoke(x, y, z, Blocks.WATER);

        Thread.sleep(3000);

        logger.debug("Removing water");
        keyUse = mc.options.keyUse.getKeyBinding();
        keyUse.setDown(true);
        Thread.sleep(100);
        keyUse.setDown(false);

        WaitForBlockState.instance.invoke(x, y, z, Blocks.AIR);

        MoveToCommand.instance.invoke(x + 0.8 + random.nextDouble() * 0.2, y, z + 0.8 + random.nextDouble() * 0.2, true);
    }

    private void removeLava() throws InterruptedException {
        pickLava();

        LookAtCommand.instance.invoke(x + 0.4 + random.nextDouble() * 0.2, y - 1, z + 0.4 + random.nextDouble() * 0.2);

        KeyBinding keyUse = mc.options.keyUse.getKeyBinding();
        keyUse.setDown(true);
        Thread.sleep(50 + random.nextInt(100));
        keyUse.setDown(false);

        WaitForBlockState.instance.invoke(x, y - 1, z, Blocks.AIR);
    }

    private void waitNoYChange() throws InterruptedException {
        synchronized (clientTickEvent) {
            clientTickEvent.wait();
        }

        double y = mc.player.getY();

        while (true) {
            synchronized (clientTickEvent) {
                clientTickEvent.wait();
            }
            double newY = mc.player.getY();
            if (newY == y) {
                break;
            }
            y = newY;
        }
    }

    private class CannotProceedFurtherException extends Exception {

    }*/

    private enum State {
        INVALID,
        BEGIN,
        PREPARE_NEXT_LEVEL,
        PILLAR_UP,
        PLACE_LAVA,
        PLACE_WATER,
    }
}