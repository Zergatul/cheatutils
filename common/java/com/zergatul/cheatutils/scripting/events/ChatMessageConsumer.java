package com.zergatul.cheatutils.scripting.events;

@FunctionalInterface
public interface ChatMessageConsumer {
    void accept(String text);
}