package com.zergatul.cheatutils.modules.scripting;

import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.ContainerClickEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.scripting.runtime.Action0;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;

import java.util.ArrayList;
import java.util.List;

public class EventsScripting implements Module {

    public static final EventsScripting instance = new EventsScripting();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Action0> onHandleKeys = new ArrayList<>();
    private final List<Action0> onTickEnd = new ArrayList<>();
    private final List<Action0> onPlayerAdded = new ArrayList<>();
    private final List<Action0> onPlayerRemoved = new ArrayList<>();
    private final List<Action0> onChatMessage = new ArrayList<>();
    private final List<Action0> onJoinServer = new ArrayList<>();
    private final List<Action0> onContainerMenuClick = new ArrayList<>();
    private Entity currentEntity;
    private Component currentChatMessage;
    private Connection currentConnection;
    private ContainerClickEvent currentContainerClickEvent;

    private EventsScripting() {
        Events.BeforeHandleKeyBindings.add(() -> {
            if (canTrigger()) {
                for (Action0 handler : onHandleKeys) {
                    handler.invoke();
                }
            }
        });

        Events.ClientTickEnd.add(() -> {
            if (canTrigger()) {
                for (Action0 handler : onTickEnd) {
                    handler.invoke();
                }
            }
        });

        Events.EntityAdded.add(entity -> {
            if (canTrigger() && entity instanceof RemotePlayer) {
                currentEntity = entity;
                for (Action0 handler : onPlayerAdded) {
                    handler.invoke();
                }
            }
        });

        Events.EntityRemoved.add(entity -> {
            if (canTrigger() && entity instanceof RemotePlayer) {
                currentEntity = entity;
                for (Action0 handler : onPlayerRemoved) {
                    handler.invoke();
                }
            }
        });

        Events.ChatMessageAdded.add(component -> {
            if (canTrigger()) {
                currentChatMessage = component;
                for (Action0 handler : onChatMessage) {
                    handler.invoke();
                }
            }
        });

        Events.ClientPlayerLoggingIn.add(connection -> {
            if (ConfigStore.instance.getConfig().eventsScriptingConfig.enabled) {
                currentConnection = connection;
                for (Action0 handler : onJoinServer) {
                    handler.invoke();
                }
                currentConnection = null;
            }
        });

        Events.ContainerMenuClick.add(event -> {
            if (canTrigger()) {
                currentContainerClickEvent = event;
                for (Action0 handler : onContainerMenuClick) {
                    handler.invoke();
                }
                currentContainerClickEvent = null;
            }
        });
    }

    public Entity getCurrentEntity() {
        return currentEntity;
    }

    public Component getCurrentChatMessage() {
        return currentChatMessage;
    }

    public Connection getCurrentConnection() {
        return currentConnection;
    }

    public ContainerClickEvent getCurrentContainerClickEvent() {
        return currentContainerClickEvent;
    }

    public void setScript(Runnable runnable) {
        clear();
        if (runnable != null) {
            RenderSystem.recordRenderCall(runnable::run);
        }
    }

    public void clear() {
        RenderSystem.recordRenderCall(() -> {
            onHandleKeys.clear();
            onTickEnd.clear();
            onPlayerAdded.clear();
            onPlayerRemoved.clear();
            onChatMessage.clear();
            onJoinServer.clear();
            onContainerMenuClick.clear();
        });
    }

    public void addOnHandleKeys(Action0 action) {
        onHandleKeys.add(action);
    }

    public void addOnTickEnd(Action0 action) {
        onTickEnd.add(action);
    }

    public void addOnPlayerAdded(Action0 action) {
        onPlayerAdded.add(action);
    }

    public void addOnPlayerRemoved(Action0 action) {
        onPlayerRemoved.add(action);
    }

    public void addOnChatMessage(Action0 action) {
        onChatMessage.add(action);
    }

    public void addOnJoinServer(Action0 action) {
        onJoinServer.add(action);
    }

    public void addOnContainerMenuClick(Action0 action) {
        onContainerMenuClick.add(action);
    }

    private boolean canTrigger() {
        return mc.player != null && ConfigStore.instance.getConfig().eventsScriptingConfig.enabled;
    }
}