package com.zergatul.cheatutils.scripting.events;

@FunctionalInterface
public interface EntityIdConsumer {
    void accept(int id);
}