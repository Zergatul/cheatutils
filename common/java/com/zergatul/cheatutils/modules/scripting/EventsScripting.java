package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.ClientThread;
import com.zergatul.cheatutils.configs.SettingHandle;
import com.zergatul.cheatutils.configs.Settings;
import com.zergatul.cheatutils.configs.modules.EventsScriptingConfig;
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
import com.zergatul.cheatutils.web.CodeWebApiBase;
import com.zergatul.cheatutils.web.WebApiBase;
import com.zergatul.cheatutils.web.WebApiRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.server.IntegratedServer;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class EventsScripting implements Module {

    public static final EventsScripting INSTANCE = new EventsScripting();

    private final SettingHandle<EventsScriptingConfig> settings = Settings.get(EventsScriptingConfig.class);
    private final Minecraft mc = Minecraft.getInstance();
    private final ConcurrentLinkedQueue<ScriptActivation<Runnable>> pendingFailures = new ConcurrentLinkedQueue<>();

    // Replaced only on the client thread. Dispatchers read it once, packet callbacks also from the Netty thread.
    private volatile @Nullable Installation installation;

    private EventsScripting() {
        Events.ClientTickStart.add(this::processPendingFailures, -1000);

        Events.BeforeHandleKeyBindings.add(() -> {
            Installation current = installation;
            if (canTrigger(current)) {
                for (Runnable handler : current.onHandleKeys) {
                    if (!current.run("key handling", handler)) return;
                }
            }
        });

        Events.ClientTickEnd.add(() -> {
            Installation current = installation;
            if (isActive(current) && settings.get().enabled()) {
                if (mc.level == null && mc.player == null) {
                    for (Runnable handler : current.onMenuTickEnd) {
                        if (!current.run("menu tick", handler)) return;
                    }
                }
            }
        });

        Events.InGameTickEnd.add(() -> {
            Installation current = installation;
            if (canTrigger(current)) {
                for (Runnable handler : current.onTickEnd) {
                    if (!current.run("game tick", handler)) return;
                }
            }
        }, 1000); // we want to run this after all modules finish their job

        Events.EntityAdded.add(entity -> {
            Installation current = installation;
            if (canTrigger(current) && entity instanceof RemotePlayer) {
                for (EntityIdConsumer consumer : current.onPlayerAdded) {
                    if (!current.run("player added", () -> consumer.accept(entity.getId()))) return;
                }
            }
        });

        Events.EntityRemoved.add(entity -> {
            Installation current = installation;
            if (canTrigger(current) && entity instanceof RemotePlayer) {
                for (EntityIdConsumer consumer : current.onPlayerRemoved) {
                    if (!current.run("player removed", () -> consumer.accept(entity.getId()))) return;
                }
            }
        });

        Events.ChatMessageAdded.add(component -> {
            Installation current = installation;
            if (canTrigger(current)) {
                ComponentWrapper wrapper = new ComponentWrapper(component);
                for (ComponentWrapperConsumer consumer : current.onChatMessageRaw) {
                    if (!current.run("raw chat message", () -> consumer.accept(wrapper))) return;
                }
                String text = component.getString();
                for (ChatMessageConsumer consumer : current.onChatMessage) {
                    if (!current.run("chat message", () -> consumer.accept(text))) return;
                }
            }
        });

        Events.SendChat.add(event -> {
            Installation current = installation;
            if (canTrigger(current)) {
                for (PlayerMessageSendingConsumer consumer : current.onPlayerMessageSending) {
                    PlayerMessageSendingEvent sendingEvent = new PlayerMessageSendingEvent(event.getMessage());
                    if (!current.run("sending chat message", () -> consumer.consume(sendingEvent))) return;
                    if (sendingEvent.cancel) {
                        event.cancel();
                        break;
                    }
                }
            }
        });

        Events.ClientPlayerLoggingIn.add(connection -> {
            Installation current = installation;
            if (isActive(current) && settings.get().enabled() && !current.onJoinServer.isEmpty()) {
                String address = connection == null ? "" : connection.getRemoteAddress().toString();
                IntegratedServer integratedServer = mc.getSingleplayerServer();
                ServerInformation info = new ServerInformation(address, integratedServer);
                for (ServerInformationConsumer consumer : current.onJoinServer) {
                    if (!current.run("joining server", () -> consumer.accept(info))) return;
                }
            }
        });

        Events.ContainerMenuClick.add(event -> {
            Installation current = installation;
            if (canTrigger(current)) {
                for (ContainerClickConsumer consumer : current.onContainerMenuClick) {
                    if (!current.run("container click", () -> consumer.accept(event.slot(), event.button(), event.type().toString()))) return;
                }
            }
        });

        Events.PlayerInfoUpdated.add(event -> {
            Installation current = installation;
            if (canTrigger(current)) {
                for (PlayerInfoUpdateConsumer consumer : current.onPlayerInfoUpdate) {
                    if (!current.run("player info update", () -> consumer.accept(new PlayerInfoWrapper(event.info()), event.type().toString()))) return;
                }
            }
        });

        // we want packet handlers to run after all modules
        // so user handlers don't see canceled packets
        Events.BeforeHandlePacketClientThread.add(packet -> {
            Installation current = installation;
            if (canTrigger(current) && !current.onS2CPacket.isEmpty()) {
                PacketEvent event = new PacketEvent(packet);
                for (PacketEventConsumer consumer : current.onS2CPacket) {
                    if (!current.run("S2C packet", () -> consumer.accept(event))) {
                        break;
                    }
                }
                return !event.getCancel();
            }
            return true;
        }, 1000);

        Events.BeforeSendPacketClientThread.add(packet -> {
            Installation current = installation;
            if (canTrigger(current) && !current.onC2SPacket.isEmpty()) {
                PacketEvent event = new PacketEvent(packet);
                for (PacketEventConsumer consumer : current.onC2SPacket) {
                    if (!current.run("C2S packet", () -> consumer.accept(event))) {
                        break;
                    }
                }
                return !event.getCancel();
            }
            return true;
        }, 1000);

        WebApiRegistry.INSTANCE.register(new ConfigWebApi());
        WebApiRegistry.INSTANCE.register(new CodeWebApi());
    }

    public SettingHandle<EventsScriptingConfig> getSettings() {
        return settings;
    }

    /** Client thread. Unloads the previous script, then initializes the new one. */
    public void setScript(@Nullable Runnable program) {
        ClientThread.assertCurrent();

        clear();
        if (program == null) {
            return;
        }

        Installation current = new Installation(new ScriptActivation<>(new ScriptRef(ScriptType.EVENTS), program, this::onScriptFailed));
        installation = current;
        if (!current.run("initialization", program)) {
            // Even an expected early stop must not leave partially registered callbacks.
            // A failure is cleaned up by processPendingFailures(), the same way as a failed callback.
            if (current.activation.isActive()) {
                clear();
            }
            return;
        }

        if (!current.aimAssistTargetPredicates.isEmpty()) {
            List<AimAssist.TargetPredicate> predicates = List.copyOf(current.aimAssistTargetPredicates);
            AimAssist.INSTANCE.setTargetPredicate(entityId -> current.activation.test("Aim Assist target filter", () -> {
                for (AimAssist.TargetPredicate predicate : predicates) {
                    if (!predicate.test(entityId)) return false;
                }
                return true;
            }));
        }
        if (!current.killAuraTargetPredicates.isEmpty()) {
            List<KillAura.TargetPredicate> predicates = List.copyOf(current.killAuraTargetPredicates);
            KillAura.INSTANCE.setTargetPredicate(entityId -> current.activation.test("Kill Aura target filter", () -> {
                for (KillAura.TargetPredicate predicate : predicates) {
                    if (!predicate.test(entityId)) return false;
                }
                return true;
            }));
        }
    }

    /** Client thread. */
    public void clear() {
        ClientThread.assertCurrent();

        Installation previous = installation;
        if (previous == null) {
            return;
        }

        installation = null;
        previous.activation.deactivate();
        unload(previous);

        AimAssist.INSTANCE.clearTargetPredicate();
        KillAura.INSTANCE.clearTargetPredicate();
    }

    private void onScriptFailed(ScriptActivation<Runnable> activation) {
        // Can run on any thread, inside the failing callback. Callbacks and filters of a failed activation
        // already do nothing, so cleanup can wait for the client thread.
        // An unbounded handoff is intentional: a script may have filled the normal executor queue.
        pendingFailures.add(activation);
    }

    private void processPendingFailures() {
        ScriptActivation<Runnable> activation;
        while ((activation = pendingFailures.poll()) != null) {
            Installation current = installation;
            if (current == null || current.activation != activation) {
                // already replaced, and the replacement unloaded the failed script
                continue;
            }

            // Keep the failed installation, so filters keep rejecting all targets until the script is saved again.
            if (!current.aimAssistTargetPredicates.isEmpty()) AimAssist.INSTANCE.setTargetPredicate(_ -> false);
            if (!current.killAuraTargetPredicates.isEmpty()) KillAura.INSTANCE.setTargetPredicate(_ -> false);
            unload(current);
        }
    }

    /** Runs unload handlers once and detaches all callbacks of an installation. */
    private void unload(Installation target) {
        target.clearCallbacks();

        // copy, since bad code may attach new handlers from existing handlers
        List<Runnable> handlers = List.copyOf(target.onUnload);
        target.onUnload.clear();
        for (Runnable action : handlers) {
            target.activation.runCleanup("EventScripting unload", action);
        }
    }

    private static boolean isActive(@Nullable Installation installation) {
        return installation != null && installation.activation.isActive();
    }

    private boolean canTrigger(@Nullable Installation installation) {
        return isActive(installation) && mc.player != null && settings.get().enabled();
    }

    /**
     * Adds a callback to the current installation, unless the callback belongs to a replaced or failed script
     * (for example its packet callback still running on the Netty thread).
     */
    private <T> boolean register(T callback, Function<Installation, List<T>> list) {
        Installation current = installation;
        if (!isActive(current) || !current.activation.owns(callback)) {
            return false;
        }

        list.apply(current).add(callback);
        return true;
    }

    public void addOnHandleKeys(Runnable action) {
        register(action, i -> i.onHandleKeys);
    }

    public void addOnTickEnd(Runnable action) {
        register(action, i -> i.onTickEnd);
    }

    public void addOnMenuTickEnd(Runnable action) {
        register(action, i -> i.onMenuTickEnd);
    }

    public void addOnPlayerAdded(EntityIdConsumer consumer) {
        register(consumer, i -> i.onPlayerAdded);
    }

    public void addOnPlayerRemoved(EntityIdConsumer consumer) {
        register(consumer, i -> i.onPlayerRemoved);
    }

    public void addOnChatMessageRaw(ComponentWrapperConsumer consumer) {
        register(consumer, i -> i.onChatMessageRaw);
    }

    public void addOnChatMessage(ChatMessageConsumer consumer) {
        register(consumer, i -> i.onChatMessage);
    }

    public void addOnPlayerMessageSending(PlayerMessageSendingConsumer consumer) {
        register(consumer, i -> i.onPlayerMessageSending);
    }

    public void addOnJoinServer(ServerInformationConsumer consumer) {
        register(consumer, i -> i.onJoinServer);
    }

    public void addOnContainerMenuClick(ContainerClickConsumer consumer) {
        register(consumer, i -> i.onContainerMenuClick);
    }

    public void addOnPlayerInfoUpdate(PlayerInfoUpdateConsumer consumer) {
        register(consumer, i -> i.onPlayerInfoUpdate);
    }

    public void addOnClientToServerPacket(PacketEventConsumer consumer) {
        register(consumer, i -> i.onC2SPacket);
    }

    public void addOnServerToClientPacket(PacketEventConsumer consumer) {
        register(consumer, i -> i.onS2CPacket);
    }

    public void addOnUnload(Runnable action) {
        register(action, i -> i.onUnload);
    }

    public void addAimAssistTargetPredicate(AimAssist.TargetPredicate predicate) {
        register(predicate, i -> i.aimAssistTargetPredicates);
    }

    public void addKillAuraTargetPredicate(KillAura.TargetPredicate predicate) {
        register(predicate, i -> i.killAuraTargetPredicates);
    }

    /**
     * Everything one installed script registered. Lists are copy-on-write: callbacks can register more callbacks
     * while a list is being iterated, and packet callbacks iterate them on the Netty thread.
     */
    private static final class Installation {

        private final ScriptActivation<Runnable> activation;
        private final List<Runnable> onHandleKeys = new CopyOnWriteArrayList<>();
        private final List<Runnable> onTickEnd = new CopyOnWriteArrayList<>();
        private final List<Runnable> onMenuTickEnd = new CopyOnWriteArrayList<>();
        private final List<EntityIdConsumer> onPlayerAdded = new CopyOnWriteArrayList<>();
        private final List<EntityIdConsumer> onPlayerRemoved = new CopyOnWriteArrayList<>();
        private final List<ComponentWrapperConsumer> onChatMessageRaw = new CopyOnWriteArrayList<>();
        private final List<ChatMessageConsumer> onChatMessage = new CopyOnWriteArrayList<>();
        private final List<PlayerMessageSendingConsumer> onPlayerMessageSending = new CopyOnWriteArrayList<>();
        private final List<ServerInformationConsumer> onJoinServer = new CopyOnWriteArrayList<>();
        private final List<ContainerClickConsumer> onContainerMenuClick = new CopyOnWriteArrayList<>();
        private final List<PlayerInfoUpdateConsumer> onPlayerInfoUpdate = new CopyOnWriteArrayList<>();
        private final List<PacketEventConsumer> onC2SPacket = new CopyOnWriteArrayList<>();
        private final List<PacketEventConsumer> onS2CPacket = new CopyOnWriteArrayList<>();
        private final List<Runnable> onUnload = new CopyOnWriteArrayList<>();
        private final List<AimAssist.TargetPredicate> aimAssistTargetPredicates = new CopyOnWriteArrayList<>();
        private final List<KillAura.TargetPredicate> killAuraTargetPredicates = new CopyOnWriteArrayList<>();

        private Installation(ScriptActivation<Runnable> activation) {
            this.activation = activation;
        }

        private boolean run(String context, Runnable callback) {
            return activation.run(context, callback);
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
            aimAssistTargetPredicates.clear();
            killAuraTargetPredicates.clear();
        }
    }

    private final class ConfigWebApi extends WebApiBase {

        @Override
        public String getRoute() {
            return "events-scripting";
        }

        @Override
        public String get() {
            return gson.toJson(settings.get());
        }

        @Override
        public String post(String body) {
            EventsScriptingConfig.UpdateOptions options = gson.fromJson(body, EventsScriptingConfig.UpdateOptions.class);
            if (options != null) {
                settings.update(config -> config.withOptions(options));
            }
            return get();
        }
    }

    private static final class CodeWebApi extends CodeWebApiBase {

        @Override
        public String getRoute() {
            return "events-scripting-code";
        }

        @Override
        protected ScriptType getScriptType() {
            return ScriptType.EVENTS;
        }
    }
}
