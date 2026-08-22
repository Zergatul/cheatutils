package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.automation.AimAssist;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.*;
import com.zergatul.cheatutils.scripting.events.*;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class EventsApi {

    public final ModuleEventsApi modules = new ModuleEventsApi();

    @MethodDescription("""
            Triggers every tick, unless you interact with some UI, like chat or crafting table.
            Best place to work with keys, for example to bind Zoom key.
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onHandleKeys(Runnable action) {
        EventsScripting.instance.addOnHandleKeys(action);
    }

    @MethodDescription("""
            Runs at the end of each client tick while the player is in a world.
            Not called in the main menu.
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onTickEnd(Runnable action) {
        EventsScripting.instance.addOnTickEnd(action);
    }

    @MethodDescription("""
            Runs at the end of each client tick while no world is loaded
            (main menu, disconnect screen, server list).
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onMenuTickEnd(Runnable action) {
        EventsScripting.instance.addOnMenuTickEnd(action);
    }

    @MethodDescription("""
            Triggers when player enters your visibility range.
            Example: events.onPlayerAdded(id => {
                // id - entity id of a player
                // use game.entities to get information about this entity
            });
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onPlayerAdded(EntityIdConsumer consumer) {
        EventsScripting.instance.addOnPlayerAdded(consumer);
    }

    @MethodDescription("""
            Triggers when player leaves your visibility range.
            Example:
            events.onPlayerRemoved(id => {
                // id - entity id of a player
                // use game.entities to get information about this entity
            });
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onPlayerRemoved(EntityIdConsumer consumer) {
        EventsScripting.instance.addOnPlayerRemoved(consumer);
    }

    @MethodDescription("""
            Triggers when new message appears in the chat.
            Message may come from the server, from cheatutils, or from another mod.
            Example:
            events.onChatMessage(text => {
                // ...
            });
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onChatMessage(ChatMessageConsumer consumer) {
        EventsScripting.instance.addOnChatMessage(consumer);
    }

    @MethodDescription("""
            Triggers when new message appears in the chat.
            Message may come from the server, from cheatutils, or from another mod.
            This event provides low level access to internal message structure, like styles, links, hover messages.
            Example:
            events.onChatMessageRaw(message => {
                // ...
            });
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onChatMessageRaw(ComponentWrapperConsumer consumer) {
        EventsScripting.instance.addOnChatMessageRaw(consumer);
    }

    @MethodDescription("""
            Triggers when you are trying to send something to the server.
            You can see the message you are going to send, and cancel sending.
            Example:
            events.onPlayerMessageSending(event => {
                if (event.message.startsWith("y")) event.cancel = true;
            });
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onPlayerMessageSending(PlayerMessageSendingConsumer consumer) {
        EventsScripting.instance.addOnPlayerMessageSending(consumer);
    }

    @MethodDescription("""
            Triggers when you join any server.
            Example:
            events.onJoinServer(info => {
                // fields: info.address, info.isSinglePlayer, info.singlePlayerWorldName, info.singlePlayerWorldPath
            });
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onJoinServer(ServerInformationConsumer consumer) {
        EventsScripting.instance.addOnJoinServer(consumer);
    }

    @MethodDescription("""
            Triggers when you (or automated tool) click on slot in ContainerMenu screen.
            Example:
            events.onContainerMenuSlotClick((slot, button, type) => {
                // ...
            });
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onContainerMenuSlotClick(ContainerClickConsumer consumer) {
        EventsScripting.instance.addOnContainerMenuClick(consumer);
    }

    @MethodDescription("""
            Triggers when multiplayer server sends update about player list.
            Event is getting triggered on every player add/update/remove.
            Example:
            events.onPlayerInfoUpdate((info, type) => {
                // type can be either: ADD, UPDATE, REMOVE
                // ...
            });
            """)
    @ApiVisibility(ApiType.EVENTS)
    public void onPlayerInfoUpdate(PlayerInfoUpdateConsumer consumer) {
        EventsScripting.instance.addOnPlayerInfoUpdate(consumer);
    }

    @AdvancedApi
    @ApiVisibility(ApiType.EVENTS)
    public void onClientToServerPacket(PacketEventConsumer consumer) {
        EventsScripting.instance.addOnClientToServerPacket(consumer);
    }

    @AdvancedApi
    @ApiVisibility(ApiType.EVENTS)
    public void onServerToClientPacket(PacketEventConsumer consumer) {
        EventsScripting.instance.addOnServerToClientPacket(consumer);
    }

    public static class ModuleEventsApi {
        public final AimAssistEventsApi aimAssist = new AimAssistEventsApi();
    }

    public static class AimAssistEventsApi {

        @MethodDescription("""
                Triggers when Aim Assist module searches for the target.
                You can filter for specific entity type, class or other attributes.
                Use game.entities API to work with supplied entity id.
                Return true to allow the entity as a target candidate.
                Return false to reject it. Multiple filters are combined with AND.
                Example:
                events.modules.aimAssist.addTargetFilter(entityId => game.entities.getType(entityId) == "minecraft:cow");
                """)
        @ApiVisibility(ApiType.EVENTS)
        public void addTargetFilter(AimAssist.TargetPredicate predicate) {
            EventsScripting.instance.addAimAssistTargetPredicate(predicate);
        }
    }
}