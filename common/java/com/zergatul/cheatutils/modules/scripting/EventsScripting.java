package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.NetworkPacketsController;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.automation.AimAssist;
import com.zergatul.cheatutils.modules.hacks.KillAura;
import com.zergatul.cheatutils.scripting.ScriptActivation;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.events.*;
import com.zergatul.cheatutils.scripting.modules.PacketEvent;
import com.zergatul.cheatutils.scripting.modules.PlayerMessageSendingEvent;
import com.zergatul.cheatutils.scripting.types.ComponentWrapper;
import com.zergatul.cheatutils.scripting.types.PlayerInfoWrapper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.server.IntegratedServer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class EventsScripting implements Module {

    public static final EventsScripting instance = new EventsScripting();

    private final Minecraft mc = Minecraft.getInstance();
    private volatile ScriptActivation<Runnable> script;
    private final ConcurrentLinkedQueue<ScriptActivation<Runnable>> pendingFailures = new ConcurrentLinkedQueue<>();
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
    private final List<PacketEventConsumer> onC2SPacket = new CopyOnWriteArrayList<>();
    private final List<PacketEventConsumer> onS2CPacket = new CopyOnWriteArrayList<>();

    private final List<AimAssist.TargetPredicate> aimAssistTargetPredicates = new ArrayList<>();
    private final List<KillAura.TargetPredicate> killAuraTargetPredicates = new ArrayList<>();

    private final Consumer<NetworkPacketsController.ClientPacketArgs> onClientPacketHandler = this::onClientPacket;
    private final Consumer<NetworkPacketsController.ServerPacketArgs> onServerPacketHandler = this::onServerPacket;

    private EventsScripting() {
        Events.ClientTickStart.add(this::processPendingFailures, -1000);
        Events.BeforeHandleKeyBindings.add(() -> {
            if (canTrigger()) {
                for (Runnable handler : onHandleKeys) {
                    if (!runCallback("key handling", handler)) return;
                }
            }
        });

        Events.ClientTickEnd.add(() -> {
            if (isScriptActive() && ConfigStore.instance.getConfig().eventsScriptingConfig.enabled) {
                if (mc.level == null && mc.player == null) {
                    for (Runnable handler : onMenuTickEnd) {
                        if (!runCallback("menu tick", handler)) return;
                    }
                }
            }
        });

        Events.InGameTickEnd.add(() -> {
            if (canTrigger()) {
                for (Runnable handler : onTickEnd) {
                    if (!runCallback("game tick", handler)) return;
                }
            }
        }, 1000); // we want to run this after all modules finish their job

        Events.EntityAdded.add(entity -> {
            if (canTrigger() && entity instanceof RemotePlayer) {
                for (EntityIdConsumer consumer : onPlayerAdded) {
                    if (!runCallback("player added", () -> consumer.accept(entity.getId()))) return;
                }
            }
        });

        Events.EntityRemoved.add(entity -> {
            if (canTrigger() && entity instanceof RemotePlayer) {
                for (EntityIdConsumer consumer : onPlayerRemoved) {
                    if (!runCallback("player removed", () -> consumer.accept(entity.getId()))) return;
                }
            }
        });

        Events.ChatMessageAdded.add(component -> {
            if (canTrigger()) {
                ComponentWrapper wrapper = new ComponentWrapper(component);
                for (ComponentWrapperConsumer consumer : onChatMessageRaw) {
                    if (!runCallback("raw chat message", () -> consumer.accept(wrapper))) return;
                }
                String text = component.getString();
                for (ChatMessageConsumer consumer : onChatMessage) {
                    if (!runCallback("chat message", () -> consumer.accept(text))) return;
                }
            }
        });

        Events.SendChat.add(event -> {
            if (canTrigger()) {
                for (PlayerMessageSendingConsumer consumer : onPlayerMessageSending) {
                    PlayerMessageSendingEvent sendingEvent = new PlayerMessageSendingEvent(event.getMessage());
                    if (!runCallback("sending chat message", () -> consumer.consume(sendingEvent))) return;
                    if (sendingEvent.cancel) {
                        event.cancel();
                        break;
                    }
                }
            }
        });

        Events.ClientPlayerLoggingIn.add(connection -> {
            if (isScriptActive() && ConfigStore.instance.getConfig().eventsScriptingConfig.enabled && !onJoinServer.isEmpty()) {
                String address = connection == null ? "" : connection.getRemoteAddress().toString();
                IntegratedServer integratedServer = mc.getSingleplayerServer();
                ServerInformation info = new ServerInformation(address, integratedServer);
                for (ServerInformationConsumer consumer : onJoinServer) {
                    if (!runCallback("joining server", () -> consumer.accept(info))) return;
                }
            }
        });

        Events.ContainerMenuClick.add(event -> {
            if (canTrigger()) {
                for (ContainerClickConsumer consumer : onContainerMenuClick) {
                    if (!runCallback("container click", () -> consumer.accept(event.slot(), event.button(), event.type().toString()))) return;
                }
            }
        });

        Events.PlayerInfoUpdated.add(event -> {
            if (canTrigger()) {
                for (PlayerInfoUpdateConsumer consumer : onPlayerInfoUpdate) {
                    if (!runCallback("player info update", () -> consumer.accept(new PlayerInfoWrapper(event.info()), event.type().toString()))) return;
                }
            }
        });

        NetworkPacketsController.instance.addClientPacketHandler(args -> {

        });
        NetworkPacketsController.instance.addServerPacketHandler(args -> {

        });
    }

    public void setScript(Runnable runnable) {
        ClientTickEndExecutor.instance.execute(() -> {
            clearNow();
            if (runnable == null) return;

            ScriptActivation<Runnable> activation = new ScriptActivation<>(
                    new ScriptRef(ScriptType.EVENTS), runnable, this::onScriptFailed);
            script = activation;
            boolean initialized = activation.run("initialization", runnable);
            if (!initialized) {
                // Even an expected early stop must not leave partially registered callbacks.
                if (activation.isActive()) clearNow();
                return;
            }

            if (!aimAssistTargetPredicates.isEmpty()) {
                List<AimAssist.TargetPredicate> predicates = List.copyOf(aimAssistTargetPredicates);
                AimAssist.instance.setTargetPredicate(entityId -> activation.test("Aim Assist target filter", () -> {
                    for (AimAssist.TargetPredicate predicate : predicates) {
                        if (!predicate.test(entityId)) return false;
                    }
                    return true;
                }));
            }
            if (!killAuraTargetPredicates.isEmpty()) {
                List<KillAura.TargetPredicate> predicates = List.copyOf(killAuraTargetPredicates);
                KillAura.instance.setTargetPredicate(entityId -> activation.test("Kill Aura target filter", () -> {
                    for (KillAura.TargetPredicate predicate : predicates) {
                        if (!predicate.test(entityId)) return false;
                    }
                    return true;
                }));
            }
        });
    }

    public void clear() {
        ClientTickEndExecutor.instance.execute(this::clearNow);
    }

    private void clearNow() {
        if (script != null) script.deactivate();
        script = null;
        clearCallbacks();
        AimAssist.instance.clearTargetPredicate();
        aimAssistTargetPredicates.clear();
        KillAura.instance.clearTargetPredicate();
        killAuraTargetPredicates.clear();
    }

    private void clearCallbacks() {
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

        NetworkPacketsController.instance.removeClientPacketHandler(onClientPacketHandler);
        NetworkPacketsController.instance.removeServerPacketHandler(onServerPacketHandler);
    }

    private void onScriptFailed(ScriptActivation<Runnable> activation) {
        if (mc != null && mc.isSameThread()) {
            blockFailedFilters(activation);
        }
        // An unbounded handoff is intentional: a script may have filled the normal executor queue.
        // Never remove callbacks from inside their dispatcher or from a network thread.
        pendingFailures.add(activation);
    }

    private void processPendingFailures() {
        ScriptActivation<Runnable> activation;
        while ((activation = pendingFailures.poll()) != null) {
            if (script != activation) continue;
            blockFailedFilters(activation);
            clearCallbacks();
            aimAssistTargetPredicates.clear();
            killAuraTargetPredicates.clear();
        }
    }

    private void blockFailedFilters(ScriptActivation<Runnable> activation) {
        if (script != activation) return;
        if (!aimAssistTargetPredicates.isEmpty()) AimAssist.instance.setTargetPredicate(_ -> false);
        if (!killAuraTargetPredicates.isEmpty()) KillAura.instance.setTargetPredicate(_ -> false);
    }

    private boolean runCallback(String context, Runnable callback) {
        ScriptActivation<Runnable> activation = script;
        return activation != null && activation.run(context, callback);
    }

    private boolean isScriptActive() {
        ScriptActivation<Runnable> activation = script;
        return activation != null && activation.isActive();
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

    public void addKillAuraTargetPredicate(KillAura.TargetPredicate predicate) {
        killAuraTargetPredicates.add(predicate);
    }

    private void onClientPacket(NetworkPacketsController.ClientPacketArgs args) {
        ScriptActivation<Runnable> activation = script;
        if (activation != null && canTrigger()) {
            PacketEvent event = new PacketEvent(args.packet);
            for (PacketEventConsumer consumer : onC2SPacket) {
                if (!activation.run("client-to-server packet", () -> consumer.accept(event))) return;
            }
        }
    }

    private void onServerPacket(NetworkPacketsController.ServerPacketArgs args) {
        ScriptActivation<Runnable> activation = script;
        if (activation != null && canTrigger()) {
            PacketEvent event = new PacketEvent(args.packet);
            for (PacketEventConsumer consumer : onS2CPacket) {
                if (!activation.run("server-to-client packet", () -> consumer.accept(event))) return;
            }
        }
    }

    private boolean canTrigger() {
        return isScriptActive() && mc.player != null && ConfigStore.instance.getConfig().eventsScriptingConfig.enabled;
    }
}