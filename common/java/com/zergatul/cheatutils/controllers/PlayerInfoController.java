package com.zergatul.cheatutils.controllers;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PlayerInfoUpdateEvent;
import com.zergatul.cheatutils.common.events.PlayerInfoUpdateType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

public class PlayerInfoController {

    public static final PlayerInfoController instance = new PlayerInfoController();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Entry> entries = new ArrayList<>();

    private PlayerInfoController() {}

    public void onBeforeUpdate(Stream<UUID> stream) {
        if (!mc.isSameThread()) {
            return;
        }
        ClientPacketListener listener = mc.getConnection();
        if (listener == null) {
            return;
        }

        entries.clear();
        stream.forEach(uuid -> entries.add(new Entry(uuid, listener.getPlayerInfo(uuid))));
    }

    public void onAfterUpdate() {
        if (!mc.isSameThread()) {
            return;
        }
        ClientPacketListener listener = mc.getConnection();
        if (listener == null) {
            return;
        }

        for (Entry entry : entries) {
            PlayerInfo oldInfo = entry.info;
            PlayerInfo newInfo = listener.getPlayerInfo(entry.uuid);
            if (oldInfo == null && newInfo != null) {
                Events.PlayerInfoUpdated.trigger(new PlayerInfoUpdateEvent(newInfo, PlayerInfoUpdateType.ADD));
            }
            if (oldInfo != null && newInfo == null) {
                Events.PlayerInfoUpdated.trigger(new PlayerInfoUpdateEvent(oldInfo, PlayerInfoUpdateType.REMOVE));
            }
            if (oldInfo != null && newInfo != null) {
                Events.PlayerInfoUpdated.trigger(new PlayerInfoUpdateEvent(newInfo, PlayerInfoUpdateType.UPDATE));
            }
        }
    }

    private record Entry(UUID uuid, PlayerInfo info) {}
}