package com.zergatul.cheatutils.scripting.events;

import com.zergatul.cheatutils.scripting.modules.EntityEspEvent;

@FunctionalInterface
public interface EntityEspConsumer {
    void accept(int id, EntityEspEvent event);
}