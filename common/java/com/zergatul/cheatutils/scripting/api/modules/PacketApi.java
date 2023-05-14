package com.zergatul.cheatutils.scripting.api.modules;

import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.exploits.SignExploit;
import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.ApiVisibility;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;

public class PacketApi {

    @ApiVisibility(ApiType.ACTION)
    public void sendSignUpdatePacket(int x, int y, int z, String line1, String line2, String line3, String line4) {
        ServerboundSignUpdatePacket packet = new ServerboundSignUpdatePacket(
                new BlockPos(x, y, z),
                line1, line2, line3, line4);

        SignExploit.instance.skipPackets = false;
        NetworkPacketsController.instance.sendPacket(packet);
    }
}