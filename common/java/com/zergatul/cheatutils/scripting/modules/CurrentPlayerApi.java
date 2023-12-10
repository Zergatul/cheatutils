package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.ApiType;
import com.zergatul.cheatutils.scripting.ApiVisibility;
import com.zergatul.cheatutils.scripting.HelpText;
import net.minecraft.world.entity.Entity;

public class CurrentPlayerApi {

    @HelpText("Use from onPlayerAdded/onPlayerRemoved events.")
    @ApiVisibility(ApiType.EVENTS)
    public String getName() {
        Entity entity = EventsScripting.instance.getCurrentEntity();
        if (entity != null) {
            return entity.getName().getString();
        } else {
            return "";
        }
    }
}