package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.HelpText;

public class EventsApi {

    @HelpText("Triggers every tick, unless you interact with some UI, like chat or crafting table. Best place to work with keys, for example to bind Zoom key.")
    public void onHandleKeys(Runnable action) {
        EventsScripting.instance.addOnHandleKeys(action);
    }

    @HelpText("Triggers at the end of every client tick.")
    public void onTickEnd(Runnable action) {
        EventsScripting.instance.addOnTickEnd(action);
    }

    @HelpText("Triggers when player enters your visibility range. Use currentPlayer API to get information about this player.")
    public void onPlayerAdded(Runnable action) {
        EventsScripting.instance.addOnPlayerAdded(action);
    }

    @HelpText("Triggers when player leaves your visibility range. Use currentPlayer API to get information about this player.")
    public void onPlayerRemoved(Runnable action) {
        EventsScripting.instance.addOnPlayerRemoved(action);
    }
}