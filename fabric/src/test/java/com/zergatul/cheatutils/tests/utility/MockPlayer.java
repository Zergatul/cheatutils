package com.zergatul.cheatutils.tests.utility;

import com.mojang.authlib.GameProfile;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public class MockPlayer extends Player {

    public MockPlayer(MockLevel level, String name) {
        super(level, new GameProfile(UUID.randomUUID(), name));
    }

    @Override
    public @Nullable GameType gameMode() {
        return GameType.SURVIVAL;
    }
}