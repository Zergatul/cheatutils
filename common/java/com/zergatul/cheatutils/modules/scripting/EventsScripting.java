package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.automation.AimAssist;
import com.zergatul.cheatutils.scripting.events.*;
import com.zergatul.cheatutils.scripting.modules.PacketEvent;
import com.zergatul.cheatutils.scripting.modules.PlayerMessageSendingEvent;
import com.zergatul.cheatutils.scripting.types.ComponentWrapper;
import com.zergatul.cheatutils.scripting.types.PlayerInfoWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.server.IntegratedServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class EventsScripting implements Module {

    public static final EventsScripting instance = new EventsScripting();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Runnable> onHandleKeys = new ArrayList<>();
    private final List<Runnable> onTickEnd = new ArrayList<>();
    private final List<Runnable> onMenuTickEnd = new ArrayList<>();
    private final List<EntityIdConsumer> onPlayerAdded = new ArrayList<>();
    private final List<EntityIdConsumer> onPlayerRemoved = new ArrayList<>();
    private final List<ComponentWrapperConsumer> onChatMessageRaw = new ArrayList<>();
    private final List<ChatMessageConsumer> onChatMessage = new ArrayList<>();
    private final List<PlayerMessageSendingConsumer> onPlayerMessageSending = new ArrayList<>();
    private final List<ServerInformationConsumer> onJoinServer = new ArrayList<>();
    private final List<ContainerClickConsumer> onContainerMenuClick = new ArrayList<>();
    private final List<PlayerInfoUpdateConsumer> onPlayerInfoUpdate = new ArrayList<>();
    private final List<PacketEventConsumer> onC2SPacket = new ArrayList<>();
    private final List<PacketEventConsumer> onS2CPacket = new ArrayList<>();

    private final List<AimAssist.TargetPredicate> aimAssistTargetPredicates = new ArrayList<>();

    private final Consumer<NetworkPacketsController.ClientPacketArgs> onClientPacketHandler = this::onClientPacket;
    private final Consumer<NetworkPacketsController.ServerPacketArgs> onServerPacketHandler = this::onServerPacket;

    private EventsScripting() {
        Events.BeforeHandleKeyBindings.add(() -> {
            if (canTrigger()) {
                for (Runnable handler : onHandleKeys) {
                    handler.run();
                }
            }
        });

        Events.ClientTickEnd.add(() -> {
            if (ConfigStore.instance.getConfig().eventsScriptingConfig.enabled) {
                if (mc.level == null && mc.player == null) {
                    for (Runnable handler : onMenuTickEnd) {
                        handler.run();
                    }
                }
            }
        });

        Events.InGameTickEnd.add(() -> {
            if (canTrigger()) {
                for (Runnable handler : onTickEnd) {
                    handler.run();
                }
            }
        }, 1000); // we want to run this after all modules finish their job

        Events.EntityAdded.add(entity -> {
            if (canTrigger() && entity instanceof RemotePlayer) {
                for (EntityIdConsumer consumer : onPlayerAdded) {
                    consumer.accept(entity.getId());
                }
            }
        });

        Events.EntityRemoved.add(entity -> {
            if (canTrigger() && entity instanceof RemotePlayer) {
                for (EntityIdConsumer consumer : onPlayerRemoved) {
                    consumer.accept(entity.getId());
                }
            }
        });

        Events.ChatMessageAdded.add(component -> {
            if (canTrigger()) {
                ComponentWrapper wrapper = new ComponentWrapper(component);
                for (ComponentWrapperConsumer consumer : onChatMessageRaw) {
                    consumer.accept(wrapper);
                }
                String text = component.getString();
                for (ChatMessageConsumer consumer : onChatMessage) {
                    consumer.accept(text);
                }
            }
        });

        Events.SendChat.add(event -> {
            if (canTrigger()) {
                for (PlayerMessageSendingConsumer consumer : onPlayerMessageSending) {
                    PlayerMessageSendingEvent sendingEvent = new PlayerMessageSendingEvent(event.getMessage());
                    consumer.consume(sendingEvent);
                    if (sendingEvent.cancel) {
                        event.cancel();
                        break;
                    }
                }
            }
        });

        Events.ClientPlayerLoggingIn.add(connection -> {
            if (ConfigStore.instance.getConfig().eventsScriptingConfig.enabled && !onJoinServer.isEmpty()) {
                String address = connection == null ? "" : connection.getRemoteAddress().toString();
                IntegratedServer integratedServer = mc.getSingleplayerServer();
                ServerInformation info = new ServerInformation(address, integratedServer);
                for (ServerInformationConsumer consumer : onJoinServer) {
                    consumer.accept(info);
                }
            }
        });

        Events.ContainerMenuClick.add(event -> {
            if (canTrigger()) {
                for (ContainerClickConsumer consumer : onContainerMenuClick) {
                    consumer.accept(event.slot(), event.button(), event.type().toString());
                }
            }
        });

        Events.PlayerInfoUpdated.add(event -> {
            if (canTrigger()) {
                for (PlayerInfoUpdateConsumer consumer : onPlayerInfoUpdate) {
                    consumer.accept(new PlayerInfoWrapper(event.info()), event.type().toString());
                }
            }
        });

        NetworkPacketsController.instance.addClientPacketHandler(args -> {

        });
        NetworkPacketsController.instance.addServerPacketHandler(args -> {

        });
    }

    public void setScript(Runnable runnable) {
        clear();
        if (runnable != null) {
            ClientTickEndExecutor.instance.execute(runnable);

            ClientTickEndExecutor.instance.execute(() -> {
                if (aimAssistTargetPredicates.isEmpty()) {
                    return;
                }

                if (aimAssistTargetPredicates.size() == 1) {
                    AimAssist.instance.setTargetPredicate(aimAssistTargetPredicates.getFirst());
                } else {
                    List<AimAssist.TargetPredicate> predicates = Collections.unmodifiableList(aimAssistTargetPredicates);
                    AimAssist.instance.setTargetPredicate(entityId -> predicates.stream().allMatch(p -> p.test(entityId)));
                }
            });
        }
    }

    public void clear() {
        ClientTickEndExecutor.instance.execute(() -> {
            onHandleKeys.clear();
            onTickEnd.clear();
            onMenuTickEnd.clear();
            onPlayerAdded.clear();
            onPlayerRemoved.clear();
            onChatMessageRaw.clear();
            onChatMessage.clear();
            onPlayerMessageSending.clear();
            onJoinServer.clear();
            onContainerMenuClick.clear();
            onPlayerInfoUpdate.clear();
            onC2SPacket.clear();
            onS2CPacket.clear();

            AimAssist.instance.clearTargetPredicate();
            aimAssistTargetPredicates.clear();

            NetworkPacketsController.instance.removeClientPacketHandler(onClientPacketHandler);
            NetworkPacketsController.instance.removeServerPacketHandler(onServerPacketHandler);
        });
    }

    public void addOnHandleKeys(Runnable action) {
        onHandleKeys.add(action);
    }

    public void addOnTickEnd(Runnable action) {
        onTickEnd.add(action);
    }

    public void addOnMenuTickEnd(Runnable action) {
        onMenuTickEnd.add(action);
    }

    public void addOnPlayerAdded(EntityIdConsumer consumer) {
        onPlayerAdded.add(consumer);
    }

    public void addOnPlayerRemoved(EntityIdConsumer consumer) {
        onPlayerRemoved.add(consumer);
    }

    public void addOnChatMessageRaw(ComponentWrapperConsumer consumer) {
        onChatMessageRaw.add(consumer);
    }

    public void addOnChatMessage(ChatMessageConsumer consumer) {
        onChatMessage.add(consumer);
    }

    public void addOnPlayerMessageSending(PlayerMessageSendingConsumer consumer) {
        onPlayerMessageSending.add(consumer);
    }

    public void addOnJoinServer(ServerInformationConsumer consumer) {
        onJoinServer.add(consumer);
    }

    public void addOnContainerMenuClick(ContainerClickConsumer consumer) {
        onContainerMenuClick.add(consumer);
    }

    public void addOnPlayerInfoUpdate(PlayerInfoUpdateConsumer consumer) {
        onPlayerInfoUpdate.add(consumer);
    }

    public void addOnClientToServerPacket(PacketEventConsumer consumer) {
        onC2SPacket.add(consumer);
        NetworkPacketsController.instance.addClientPacketHandlerIfAbsent(onClientPacketHandler);
    }

    public void addOnServerToClientPacket(PacketEventConsumer consumer) {
        onS2CPacket.add(consumer);
        NetworkPacketsController.instance.addServerPacketHandlerIfAbsent(onServerPacketHandler);
    }

    public void addAimAssistTargetPredicate(AimAssist.TargetPredicate predicate) {
        aimAssistTargetPredicates.add(predicate);
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        if (canTrigger()) {
            PacketEvent event = new PacketEvent(args.packet);
            for (PacketEventConsumer consumer : onC2SPacket) {
                consumer.accept(event);
            }
        }
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        if (canTrigger()) {
            PacketEvent event = new PacketEvent(args.packet);
            for (PacketEventConsumer consumer : onS2CPacket) {
                consumer.accept(event);
            }
        }
    }

    private boolean canTrigger() {
        return mc.player != null && ConfigStore.instance.getConfig().eventsScriptingConfig.enabled;
    }
}