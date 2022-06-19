package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.SynchedEntityData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestController {

    public static final TestController instance = new TestController();

    private final Logger logger = LogManager.getLogger(TestController.class);

    private TestController() {
        //NetworkPacketsController.instance.addServerPacketHandler(this::onServerPacket);
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        if (args.packet instanceof ClientboundSetEntityDataPacket packet) {
            if (packet.getId() == Minecraft.getInstance().player.getId()) {
                var data = packet.getUnpackedData();
                data.forEach(item -> {
                    if (item.getAccessor().getId() == 0) {
                        logger.info(item.getValue());
                    }
                });
            }
        }
    }
}