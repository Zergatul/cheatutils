package com.zergatul.cheatutils.tests.utility;

import net.minecraft.world.Difficulty;
import net.minecraft.world.level.storage.WritableLevelData;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class MockWritableLevelData implements WritableLevelData {

    @Override
    public void setSpawn(RespawnData respawnData) {
        throw new UnsupportedOperationException();
    }

    @Override
    public RespawnData getRespawnData() {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getGameTime() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isHardcore() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Difficulty getDifficulty() {
        return Difficulty.HARD;
    }

    @Override
    public boolean isDifficultyLocked() {
        throw new UnsupportedOperationException();
    }
}