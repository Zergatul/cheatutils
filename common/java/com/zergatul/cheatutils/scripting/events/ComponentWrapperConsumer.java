package com.zergatul.cheatutils.scripting.events;

import com.zergatul.cheatutils.scripting.types.ComponentWrapper;

@FunctionalInterface
public interface ComponentWrapperConsumer {
    void accept(ComponentWrapper text);
}
