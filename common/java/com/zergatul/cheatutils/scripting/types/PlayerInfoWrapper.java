package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.Getter;
import com.zergatul.scripting.type.CustomType;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;

@SuppressWarnings("unused")
@CustomType(name = "PlayerInfo")
public class PlayerInfoWrapper {

    private final PlayerInfo info;

    public PlayerInfoWrapper(PlayerInfo info) {
        this.info = info;
    }

    @Getter(name = "profileId")
    public String getProfileId() {
        return info.getProfile().getId().toString();
    }

    @Getter(name = "profileName")
    public String getProfileName() {
        return info.getProfile().getName();
    }

    @Getter(name = "displayName")
    public String getDisplayName() {
        Component name = info.getTabListDisplayName();
        if (name == null) {
            return "";
        }
        return name.getString();
    }

    @Getter(name = "gameMode")
    public String getGameMode() {
        return info.getGameMode().toString();
    }

    @Getter(name = "latency")
    public int getLatency() {
        return info.getLatency();
    }
}