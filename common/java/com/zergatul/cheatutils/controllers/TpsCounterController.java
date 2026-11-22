package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetTimePacket;

public class TpsCounterController {

    public static final TpsCounterController INSTANCE = new TpsCounterController();

    private static final int SIZE = 20;

    private final long[] localTime = new long[SIZE];
    private final long[] gameTime = new long[SIZE];
    private int counter;

    private TpsCounterController() {
        Events.ClientPlayerLoggingIn.add(this::onLoggingIn);
        // we want to run handler even before FakeLag, to display actual TPS
        Events.BeforeHandlePacketClientThread.add(this::onServerPacket, -2000);
        Events.DimensionChange.add(this::onDimensionChange);
        Events.ClientPlayerLoggingOut.add(this::onLoggingOut);
    }

    public double getTps() {
        if (counter < 2) {
            return 0;
        }

        int from = counter < 20 ? 0 : counter % 20;
        int to = counter < 20 ? counter - 1 : (counter - 1) % 20;
        long ns = localTime[to] - localTime[from];
        long ticks = gameTime[to] - gameTime[from];
        return ticks / (ns / 1e9);
    }

    private void onLoggingIn(Connection connection) {
        counter = 0;
    }

    private void onLoggingOut() {
        counter = 0;
    }

    private void onDimensionChange() {
        counter = 0;
    }

    private boolean onServerPacket(Packet<?> packet) {
        if (packet instanceof ClientboundSetTimePacket setTimePacket) {
            localTime[counter % SIZE] = System.nanoTime();
            gameTime[counter % SIZE] = setTimePacket.gameTime();
            counter++;
        }
        return true;
    }
}