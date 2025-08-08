package com.zergatul.cheatutils.scripting.events;

@FunctionalInterface
public interface ContainerClickConsumer {
    void accept(int slot, int button, String type);
}