package com.zergatul.cheatutils.controllers;

import com.mojang.datafixers.util.Pair;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.scripting.Root;
import com.zergatul.cheatutils.utils.IntList;
import com.zergatul.cheatutils.utils.JavaRandom;
import net.minecraft.client.Minecraft;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class EnchantmentScreenController {

    public static final EnchantmentScreenController instance = new EnchantmentScreenController();

    private static final int SCAN_CHUNK_SIZE = 100000;

    private final List<EnchantingState> enchantingStates = new ArrayList<>();
    private final List<EnchantingState> scanningEnchantingStates = new ArrayList<>();
    private Thread[] threads;
    private volatile AtomicLong progress = new AtomicLong();
    private IntList seeds;
    private Integer lastEnchSeed, currEnchSeed;
    private Long playerSeed;
    private volatile boolean cancel;
    private volatile long scanFrom, scanTo;
    private IntList[] threadSeeds;
    private int lastUsedThreads = 2;

    private EnchantmentScreenController() {

    }

    public void addState(ItemStack itemStack, int enchantPowerBonus, int[] costs, int[] enchantments, int[] levels) {
        EnchantingState state = new EnchantingState();
        state.itemStack = itemStack.copy();
        state.enchantPowerBonus = enchantPowerBonus;
        state.costs = costs.clone();
        state.enchantments = enchantments.clone();
        state.levels = levels.clone();
        enchantingStates.add(state);
    }

    public void reset() {
        if (threads != null) {
            cancel = true;
            for (Thread thread: threads) {
                try {
                    thread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            threads = null;
            threadSeeds = null;
            cancel = false;
        }
        enchantingStates.clear();
        seeds = null;
        lastEnchSeed = null;
        currEnchSeed = null;
        playerSeed = null;
    }

    public int getDataCount() {
        return enchantingStates.size();
    }

    public long getSeedsCount() {
        if (seeds == null) {
            return 0x100000000L;
        } else {
            return seeds.count();
        }
    }

    public boolean isFilterInProgress() {
        return threads != null;
    }

    public double getProgress() {
        return 1d * progress.get() / getSeedsCount();
    }

    public long getNewSeedsCount() {
        if (threadSeeds == null) {
            return 0;
        }
        IntList[] copy = threadSeeds;
        long count = 0;
        for (int i = 0; i < copy.length; i++) {
            if (copy[i] != null) {
                count += copy[i].count();
            }
        }
        return count;
    }

    public void scan(int threads) {
        lastUsedThreads = threads;

        if (this.threads != null) {
            return;
        }

        scanningEnchantingStates.clear();
        scanningEnchantingStates.addAll(enchantingStates);
        enchantingStates.clear();

        if (seeds == null) {
            scanFrom = 0;
            scanTo = 0x100000000L;
        } else {
            scanFrom = 0;
            scanTo = seeds.count();
        }

        if ((scanTo + SCAN_CHUNK_SIZE - 1) / SCAN_CHUNK_SIZE < threads) {
            threads = (int) ((scanTo + SCAN_CHUNK_SIZE - 1) / SCAN_CHUNK_SIZE);
            if (threads == 0) {
                threads = 1;
            }
        }

        this.threads = new Thread[threads];
        this.threadSeeds = new IntList[threads];
        this.progress.set(0);
        for (int i = 0; i < threads; i++) {
            int index = i;
            this.threads[i] = new Thread(() -> scanEnchantingSeeds(index));
            this.threads[i].start();
        }
    }

    public String getSingleSeed() {
        if (seeds != null && seeds.count() == 1) {
            return Integer.toHexString(seeds.get(0));
        } else {
            return null;
        }
    }

    public void setLastEnchSeed() {
        if (seeds != null && seeds.count() == 1) {
            lastEnchSeed = seeds.get(0);
            seeds = null;
            enchantingStates.clear();
        }
    }

    public void setCurrEnchSeed() {
        if (seeds != null && seeds.count() == 1) {
            currEnchSeed = seeds.get(0);
            seeds = null;
            enchantingStates.clear();
        }
    }

    public Integer getLastEnchSeed() {
        return lastEnchSeed;
    }

    public Integer getCurrEnchSeed() {
        return currEnchSeed;
    }

    public void crackPlayerSeed() {
        if (lastEnchSeed == null || currEnchSeed == null) {
            return;
        }

        playerSeed = null;

        long seed1High = ((long) lastEnchSeed << 16) & 0x0000_ffff_ffff_0000L;
        long seed2High = ((long) currEnchSeed << 16) & 0x0000_ffff_ffff_0000L;
        for (int seed1Low = 0; seed1Low < 65536; seed1Low++) {
            if ((((seed1High | seed1Low) * 0x5deece66dL + 0xb) & 0x0000_ffff_ffff_0000L) == seed2High) {
                playerSeed = ((seed1High | seed1Low) * 0x5deece66dL + 0xb) & 0x0000_ffff_ffff_ffffL;
                break;
            }
        }
    }

    public Long getPlayerSeed() {
        return playerSeed;
    }

    public int getLastUsedThreads() {
        return lastUsedThreads;
    }

    public void findDrops() {
        if (playerSeed == null) {
            return;
        }

        JavaRandom random = new JavaRandom(playerSeed);

        for (int drops = 0; drops < 1; drops++) {
            random.next(32);
            random.next(32);
            random.next(32);
            random.next(32);
        }
        int seed = random.next(32);
        isSeedGood(seed);

        /*int[] counts = new int[10];
        for (int i = 0; i < 1000000; i++) {
            counts[isSeedGood(i)]++;
        }
        isSeedGood(12345678);*/
    }

    private int isSeedGood(int seed) {
        RandomSource mcRandom = RandomSource.create();
        mcRandom.setSeed(seed);

        ItemStack itemStack = new ItemStack(Items.DIAMOND_SWORD, 1);
        int enchantPowerBonus = 15;
        {
            int cost0 = EnchantmentHelper.getEnchantmentCost(mcRandom, 0, enchantPowerBonus, itemStack);
            if (cost0 < 1) {
                cost0 = 0;
            }

            int cost1 = EnchantmentHelper.getEnchantmentCost(mcRandom, 1, enchantPowerBonus, itemStack);
            if (cost1 < 2) {
                cost1 = 0;
            }

            int cost2 = EnchantmentHelper.getEnchantmentCost(mcRandom, 2, enchantPowerBonus, itemStack);
            if (cost2 < 3) {
                cost2 = 0;
            }

            if (cost0 > 0) {
                List<EnchantmentInstance> list = getEnchantmentList(mcRandom, seed, itemStack, 0, cost0);
            }

            if (cost1 > 0) {
                List<EnchantmentInstance> list = getEnchantmentList(mcRandom, seed, itemStack, 1, cost1);
            }

            if (cost2 > 0) {
                List<EnchantmentInstance> list = getEnchantmentList(mcRandom, seed, itemStack, 2, cost2);
                for (var ei: list) {
                    var id = Registries.ENCHANTMENTS.getKey(ei.enchantment);
                    Root.main.systemMessage(id.toString() + " - " + ei.level);
                }
                return list.size();
            }
        }

        return 0;
    }

    private synchronized Pair<Long, Long> getNextScanChunk() {
        if (scanFrom < scanTo) {
            long from = scanFrom;
            long to = from + SCAN_CHUNK_SIZE;
            if (to > scanTo) {
                to = scanTo;
            }
            scanFrom = to;
            return new Pair<>(from, to);
        } else {
            return null;
        }
    }

    private void scanEnchantingSeeds(int index) {
        IntList newSeeds = new IntList(256);
        threadSeeds[index] = newSeeds;
        RandomSource random = RandomSource.create();
        while (true) {
            var chunk = getNextScanChunk();
            if (chunk == null) {
                break;
            }
            long from = chunk.getFirst();
            long to = chunk.getSecond();
            long lastProgress = from;
            if (seeds == null) {
                for (long i = from; i < to; i++) {
                    if (i % 1000 == 0) {
                        progress.addAndGet(i - lastProgress);
                        lastProgress = i;
                        if (cancel) {
                            return;
                        }
                    }

                    int seed = (int) (Integer.MIN_VALUE + i);
                    if (checkSeed(random, seed)) {
                        newSeeds.add(seed);
                    }
                }
            } else {
                for (long i = from; i < to; i++) {
                    if (i % 1000 == 0) {
                        progress.addAndGet(i - lastProgress);
                        lastProgress = i;
                        if (cancel) {
                            return;
                        }
                    }

                    int seed = seeds.get((int)i);
                    if (checkSeed(random, seed)) {
                        newSeeds.add(seed);
                    }
                }
            }
        }

        threads[index] = null;

        mergeThreads();
    }

    private synchronized void mergeThreads() {
        for (Thread thread : threads) {
            if (thread != null) {
                return;
            }
        }

        seeds = new IntList(256);
        for (IntList list : threadSeeds) {
            for (int i = 0; i < list.count(); i++) {
                seeds.add(list.get(i));
            }
        }

        threads = null;
        threadSeeds = null;
    }

    private boolean checkSeed(RandomSource random, int seed) {
        /*for (int s = 0; s < scanningEnchantingStates.size(); s++) {
            EnchantingState state = scanningEnchantingStates.get(s);

            random.setSeed(seed);

            int cost0 = EnchantmentHelper.getEnchantmentCost(random, 0, state.enchantPowerBonus, state.itemStack);
            int enchant0 = -1;
            int level0 = -1;
            if (cost0 < 1) {
                cost0 = 0;
            }
            if (cost0 != state.costs[0]) {
                return false;
            }

            int cost1 = EnchantmentHelper.getEnchantmentCost(random, 1, state.enchantPowerBonus, state.itemStack);
            int enchant1 = -1;
            int level1 = -1;
            if (cost1 < 2) {
                cost1 = 0;
            }
            if (cost1 != state.costs[1]) {
                return false;
            }

            int cost2 = EnchantmentHelper.getEnchantmentCost(random, 2, state.enchantPowerBonus, state.itemStack);
            int enchant2 = -1;
            int level2 = -1;
            if (cost2 < 3) {
                cost2 = 0;
            }
            if (cost2 != state.costs[2]) {
                return false;
            }

            if (cost0 > 0) {
                List<EnchantmentInstance> list = getEnchantmentList(random, seed, state.itemStack, 0, cost0);
                if (!list.isEmpty()) {
                    EnchantmentInstance enchantmentinstance = list.get(random.nextInt(list.size()));
                    enchant0 = Registries.ENCHANTMENT.(enchantmentinstance.enchantment);
                    level0 = enchantmentinstance.level;
                }
            }

            if (enchant0 != state.enchantments[0] || level0 != state.levels[0]) {
                return false;
            }

            if (cost1 > 0) {
                List<EnchantmentInstance> list = getEnchantmentList(random, seed, state.itemStack, 1, cost1);
                if (!list.isEmpty()) {
                    EnchantmentInstance enchantmentinstance = list.get(random.nextInt(list.size()));
                    enchant1 = Registry.ENCHANTMENT.getId(enchantmentinstance.enchantment);
                    level1 = enchantmentinstance.level;
                }
            }

            if (enchant1 != state.enchantments[1] || level1 != state.levels[1]) {
                return false;
            }

            if (cost2 > 0) {
                List<EnchantmentInstance> list = getEnchantmentList(random, seed, state.itemStack, 2, cost2);
                if (!list.isEmpty()) {
                    EnchantmentInstance enchantmentinstance = list.get(random.nextInt(list.size()));
                    enchant2 = Registry.ENCHANTMENT.getId(enchantmentinstance.enchantment);
                    level2 = enchantmentinstance.level;
                }
            }

            if (enchant2 != state.enchantments[2] || level2 != state.levels[2]) {
                return false;
            }
        }*/

        return true;
    }

    private List<EnchantmentInstance> getEnchantmentList(RandomSource random, int seed, ItemStack itemStack, int index, int cost) {
        random.setSeed(seed + index);
        List<EnchantmentInstance> list = EnchantmentHelper.selectEnchantment(Minecraft.getInstance().level.enabledFeatures(), random, itemStack, cost, false);
        if (itemStack.is(Items.BOOK) && list.size() > 1) {
            list.remove(random.nextInt(list.size()));
        }
        return list;
    }

    private static class EnchantingState {
        public ItemStack itemStack;
        public int enchantPowerBonus;
        public int[] costs;
        public int[] enchantments;
        public int[] levels;
    }
}