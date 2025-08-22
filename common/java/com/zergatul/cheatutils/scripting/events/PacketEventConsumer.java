package com.zergatul.cheatutils.scripting.events;

import com.zergatul.cheatutils.scripting.modules.PacketEvent;

@FunctionalInterface
public interface PacketEventConsumer {
    void accept(PacketEvent event);
}