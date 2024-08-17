package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.ChatMessageConsumer;
import com.zergatul.cheatutils.scripting.ContainerClickConsumer;
import com.zergatul.cheatutils.scripting.EntityIdConsumer;
import com.zergatul.cheatutils.scripting.ServerAddressConsumer;
import com.zergatul.scripting.MethodDescription;

@SuppressWarnings("unused")
public class EventsApi {

    @MethodDescription("""
            Triggers every tick, unless you interact with some UI, like chat or crafting table.
            Best place to work with keys, for example to bind Zoom key.
            """)
    public void onHandleKeys(Runnable action) {
        EventsScripting.instance.addOnHandleKeys(action);
    }

    @MethodDescription("""
            Triggers at the end of every client tick
            """)
    public void onTickEnd(Runnable action) {
        EventsScripting.instance.addOnTickEnd(action);
    }

    @MethodDescription("""
            Triggers when player enters your visibility range.
            Example: events.onPlayerAdded(id => {
                // id - entity id of a player
                // use game.entities to get information about this entity
            });
            """)
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
    public void onPlayerRemoved(EntityIdConsumer consumer) {
        EventsScripting.instance.addOnPlayerRemoved(consumer);
    }

    @MethodDescription("""
            Triggers when new message appears on chat.
            Message may come from the server, from cheatutils, or from another mod.
            Example:
            events.onChatMessage(text => {
                // ...
            });
            """)
    public void onChatMessage(ChatMessageConsumer consumer) {
        EventsScripting.instance.addOnChatMessage(consumer);
    }

    @MethodDescription("""
            Triggers when you join any server.
            Example:
            events.onJoinServer(address => {
                // ...
            });
            """)
    public void onJoinServer(ServerAddressConsumer consumer) {
        EventsScripting.instance.addOnJoinServer(consumer);
    }

    @MethodDescription("""
            Triggers when you (or automated tool) click on slot in ContainerMenu screen.
            Example:
            events.onContainerMenuSlotClick((slot, button, type) => {
                // ...
            });
            """)
    public void onContainerMenuSlotClick(ContainerClickConsumer consumer) {
        EventsScripting.instance.addOnContainerMenuClick(consumer);
    }
}