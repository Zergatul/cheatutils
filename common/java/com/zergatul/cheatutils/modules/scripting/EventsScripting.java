package com.zergatul.cheatutils.modules.scripting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class EventsScripting implements Module {

    public static final EventsScripting instance = new EventsScripting();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Runnable> onHandleKeys = new ArrayList<>();
    private final List<Runnable> onTickEnd = new ArrayList<>();
    private final List<Runnable> onPlayerAdded = new ArrayList<>();
    private final List<Runnable> onPlayerRemoved = new ArrayList<>();
    private Entity currentEntity;

    private EventsScripting() {
        Events.BeforeHandleKeyBindings.add(() -> {
            if (canTrigger()) {
                for (Runnable handler : onHandleKeys) {
                    handler.run();
                }
            }
        });

        Events.ClientTickEnd.add(() -> {
            if (canTrigger()) {
                for (Runnable handler : onTickEnd) {
                    handler.run();
                }
            }
        });

        Events.EntityAdded.add(entity -> {
            if (canTrigger() && entity instanceof RemotePlayer) {
                currentEntity = entity;
                for (Runnable handler : onPlayerAdded) {
                    handler.run();
                }
            }
        });

        Events.EntityRemoved.add(entity -> {
            if (canTrigger() && entity instanceof RemotePlayer) {
                currentEntity = entity;
                for (Runnable handler : onPlayerRemoved) {
                    handler.run();
                }
            }
        });
    }

    public Entity getCurrentEntity() {
        return currentEntity;
    }

    public void setScript(Runnable runnable) {
        clear();
        if (runnable != null) {
            RenderSystem.recordRenderCall(runnable::run);
        }
    }

    public void clear() {
        onHandleKeys.clear();
        onTickEnd.clear();
        onPlayerAdded.clear();
        onPlayerRemoved.clear();
    }

    public void addOnHandleKeys(Runnable runnable) {
        onHandleKeys.add(runnable);
    }

    public void addOnTickEnd(Runnable runnable) {
        onTickEnd.add(runnable);
    }

    public void addOnPlayerAdded(Runnable runnable) {
        onPlayerAdded.add(runnable);
    }

    public void addOnPlayerRemoved(Runnable runnable) {
        onPlayerRemoved.add(runnable);
    }

    private boolean canTrigger() {
        return mc.player != null && ConfigStore.instance.getConfig().eventsScriptingConfig.enabled;
    }
}