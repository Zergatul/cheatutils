package com.zergatul.cheatutils.scripting.events;

import com.zergatul.cheatutils.scripting.modules.PlayerMessageSendingEvent;

@FunctionalInterface
public interface PlayerMessageSendingConsumer {
    void consume(PlayerMessageSendingEvent event);
}