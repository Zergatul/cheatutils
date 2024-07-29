package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.HelpText;
import com.zergatul.scripting.runtime.Action0;

@SuppressWarnings("unused")
public class EventsApi {

    @HelpText("Triggers every tick, unless you interact with some UI, like chat or crafting table. Best place to work with keys, for example to bind Zoom key.")
    public void onHandleKeys(Action0 action) {
        EventsScripting.instance.addOnHandleKeys(action);
    }

    @HelpText("Triggers at the end of every client tick.")
    public void onTickEnd(Action0 action) {
        EventsScripting.instance.addOnTickEnd(action);
    }

    @HelpText("Triggers when player enters your visibility range. Use currentPlayer API to get information about this player.")
    public void onPlayerAdded(Action0 action) {
        EventsScripting.instance.addOnPlayerAdded(action);
    }

    @HelpText("Triggers when player leaves your visibility range. Use currentPlayer API to get information about this player.")
    public void onPlayerRemoved(Action0 action) {
        EventsScripting.instance.addOnPlayerRemoved(action);
    }

    @HelpText("Triggers when new message appears on chat. Message may come from the server, from cheatutils, or from another mod. Use currentChatMessage API to get information about current message.")
    public void onChatMessage(Action0 action) {
        EventsScripting.instance.addOnChatMessage(action);
    }

    @HelpText("Triggers when you join any server. Use currentServer API to get more information.")
    public void onJoinServer(Action0 action) {
        EventsScripting.instance.addOnJoinServer(action);
    }

    @HelpText("Triggers when you (or automated tool) click on slot in ContainerMenu screen. Use currentContainerClick API to get more information.")
    public void onContainerMenuSlotClick(Action0 action) {
        EventsScripting.instance.addOnContainerMenuClick(action);
    }
}