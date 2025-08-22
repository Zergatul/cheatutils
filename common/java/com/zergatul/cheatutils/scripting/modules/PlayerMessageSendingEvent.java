package com.zergatul.cheatutils.scripting.modules;

public class PlayerMessageSendingEvent {

    public final String message;
    public boolean cancel;

    public PlayerMessageSendingEvent(String message) {
        this.message = message;
    }
}