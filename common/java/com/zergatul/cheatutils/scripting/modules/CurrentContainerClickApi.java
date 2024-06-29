package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.cheatutils.common.events.ContainerClickEvent;
import com.zergatul.cheatutils.modules.scripting.EventsScripting;

@SuppressWarnings("unused")
public class CurrentContainerClickApi {

    public int getSlot() {
        ContainerClickEvent event = EventsScripting.instance.getCurrentContainerClickEvent();
        return event == null ? Integer.MIN_VALUE : event.slot();
    }

    public int getButton() {
        ContainerClickEvent event = EventsScripting.instance.getCurrentContainerClickEvent();
        return event == null ? Integer.MIN_VALUE : event.button();
    }

    public String getClickType() {
        ContainerClickEvent event = EventsScripting.instance.getCurrentContainerClickEvent();
        return event == null ? "" : event.type().toString();
    }
}