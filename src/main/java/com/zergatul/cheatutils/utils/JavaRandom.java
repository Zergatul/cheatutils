package com.zergatul.cheatutils.utils;

import net.minecraft.util.RandomSource;
import net.minecraft.world.level.levelgen.BitRandomSource;
import net.minecraft.world.level.levelgen.MarsagliaPolarGaussian;
import net.minecraft.world.level.levelgen.PositionalRandomFactory;

public class JavaRandom implements BitRandomSource {

    private static final long multiplier = 0x5DEECE66DL;
    private static final long addend = 0xBL;
    private static final long mask = (1L << 48) - 1;

    private long seed;
    private final MarsagliaPolarGaussian gaussianSource = new MarsagliaPolarGaussian(this);

    public JavaRandom(long seed) {
        setSeed(seed);
    }

    @Override
    public int next(int bits) {
        seed = (seed * multiplier + addend) & mask;
        return (int)(seed >>> (48 - bits));
    }

    @Override
    public RandomSource fork() {
        return null;
    }

    @Override
    public PositionalRandomFactory forkPositional() {
        return null;
    }

    @Override
    public void setSeed(long seed) {
        this.seed = seed & mask;
    }

    @Override
    public double nextGaussian() {
        return gaussianSource.nextGaussian();
    }
}
