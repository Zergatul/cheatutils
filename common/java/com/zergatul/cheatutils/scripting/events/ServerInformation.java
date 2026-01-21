package com.zergatul.cheatutils.scripting.events;

import com.zergatul.cheatutils.mixins.common.accessors.MinecraftServerAccessor;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.world.level.storage.LevelStorageSource;

@CustomType(name = "ServerInformation")
public class ServerInformation {

    public final String address;
    public final boolean isSinglePlayer;
    public final String singlePlayerWorldName;
    public final String singlePlayerWorldPath;

    public ServerInformation(String address, IntegratedServer server) {
        this.address = address;
        if (server != null) {
            this.isSinglePlayer = true;
            LevelStorageSource.LevelStorageAccess storageAccess = ((MinecraftServerAccessor) server).getStorageSource_CU();
            this.singlePlayerWorldName = storageAccess.getLevelId();
            this.singlePlayerWorldPath = storageAccess.getLevelDirectory().path().toString();
        } else {
            this.isSinglePlayer = false;
            this.singlePlayerWorldName = "";
            this.singlePlayerWorldPath = "";
        }
    }
}