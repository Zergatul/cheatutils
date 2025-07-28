package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.scripting.modules.PacketEvent;

@FunctionalInterface
public interface PacketEventConsumer {
    void accept(PacketEvent event);
}