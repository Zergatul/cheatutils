package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.scripting.modules.EntityEspEvent;

@FunctionalInterface
public interface EntityEspConsumer {
    void accept(int id, EntityEspEvent event);
}