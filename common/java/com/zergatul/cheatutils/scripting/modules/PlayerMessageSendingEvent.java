package com.zergatul.cheatutils.scripting.modules;

import com.zergatul.scripting.type.CustomType;

@CustomType(name = "PlayerMessageSendingEvent")
public class PlayerMessageSendingEvent {

    public final String message;
    public boolean cancel;

    public PlayerMessageSendingEvent(String message) {
        this.message = message;
    }
}