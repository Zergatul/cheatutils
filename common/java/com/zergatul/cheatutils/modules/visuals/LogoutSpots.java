package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.PlayerInfoUpdateEvent;
import com.zergatul.cheatutils.common.events.PlayerInfoUpdateType;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.LogoutSpotsConfig;
import com.zergatul.cheatutils.entities.EntityLike;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.utilities.RenderUtilities;
import com.zergatul.cheatutils.render.LineRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.Connection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.*;

public class LogoutSpots implements Module {

    public static final LogoutSpots instance = new LogoutSpots();

    private static final int TICKS_WINDOW = 5;

    private final Minecraft mc = Minecraft.getInstance();
    private final Map<UUID, CachedPlayerEntry> cachedPlayers = new HashMap<>();
    private final List<LogoutSpotEntry> entries = new ArrayList<>();
    private final List<EntityLike> players = new ArrayList<>();

    private LogoutSpots() {
        Events.ClientPlayerLoggingIn.add(this::onPlayerLoggingIn);
        Events.DimensionChange.add(this::onDimensionChanged);
        Events.PlayerInfoUpdated.add(this::onPlayerInfoUpdated);
        Events.ClientTickEnd.add(this::onClientTickEnd);
        Events.AfterRenderWorld.add(this::onAfterRenderWorld); // move before other ESPs?
    }

    public List<EntityLike> getDisconnectedPlayers() {
        assert mc.level != null;

        if (!getConfig().enabled) {
            return List.of();
        }
        if (entries.isEmpty()) {
            return List.of();
        }

        players.clear();
        for (LogoutSpotEntry entry : entries) {
            if (entry.dimension.equals(mc.level.dimension().location())) {
                players.add(entry.entity);
            }
        }
        return players;
    }

    private void onPlayerLoggingIn(Connection connection) {
        cachedPlayers.clear();
        entries.clear();
    }

    private void onDimensionChanged() {
        cachedPlayers.clear();
    }

    private void onPlayerInfoUpdated(PlayerInfoUpdateEvent event) {
        assert mc.level != null;

        UUID uuid = event.info().getProfile().id();
        if (event.type() == PlayerInfoUpdateType.REMOVE) {
            CachedPlayerEntry entry = cachedPlayers.get(uuid);
            if (entry != null) {
                entries.removeIf(e -> e.profileId.equals(uuid));
                entries.add(new LogoutSpotEntry(entry.player));
            }
        } else {
            entries.removeIf(e -> e.profileId.equals(uuid));
        }
    }

    private void onClientTickEnd() {
        if (mc.level == null) {
            return;
        }

        Iterator<CachedPlayerEntry> iterator = cachedPlayers.values().iterator();
        while (iterator.hasNext()) {
            CachedPlayerEntry entry = iterator.next();
            entry.ticks--;
            if (entry.ticks == 0) {
                iterator.remove();
            }
        }

        for (Entity entity : mc.level.entitiesForRendering()) {
            if (entity instanceof RemotePlayer player) {
                UUID uuid = player.getGameProfile().id();
                CachedPlayerEntry entry = cachedPlayers.get(uuid);
                if (entry != null) {
                    entry.refreshTicks();
                } else {
                    cachedPlayers.put(uuid, new CachedPlayerEntry(player));
                }
            }
        }
    }

    private void onAfterRenderWorld(RenderWorldLastEvent event) {
        assert mc.level != null;

        if (!getConfig().enabled || entries.isEmpty()) {
            return;
        }

        LineRenderer renderer = RenderUtilities.instance.getLineRenderer();
        renderer.begin(event, true);
        for (LogoutSpotEntry entry : entries) {
            if (entry.dimension.equals(mc.level.dimension().location())) {
                renderer.cuboid(
                        entry.pos.x - entry.width / 2,
                        entry.pos.y,
                        entry.pos.z - entry.width / 2,
                        entry.pos.x + entry.width / 2,
                        entry.pos.y + entry.height,
                        entry.pos.z + entry.width / 2,
                        1f, 1f, 1f, 1f);
            }
        }
        renderer.end();
    }

    private LogoutSpotsConfig getConfig() {
        return ConfigStore.instance.getConfig().logoutSpots;
    }

    private static class CachedPlayerEntry {

        private final RemotePlayer player;
        private int ticks;

        public CachedPlayerEntry(RemotePlayer player) {
            this.player = player;
            this.refreshTicks();
        }

        public void refreshTicks() {
            this.ticks = TICKS_WINDOW;
        }
    }

    private static class LogoutSpotEntry {

        public final UUID profileId;
        public final ResourceLocation dimension;
        public final Vec3 pos;
        public final double width;
        public final double height;
        public final EntityLike entity;

        public LogoutSpotEntry(RemotePlayer player) {
            profileId = player.getGameProfile().id();
            dimension = player.level().dimension().location();
            pos = player.position();
            width = player.getBbWidth();
            height = player.getBbHeight();
            entity = EntityLike.asDisconnectedPlayer(player);
        }
    }
}