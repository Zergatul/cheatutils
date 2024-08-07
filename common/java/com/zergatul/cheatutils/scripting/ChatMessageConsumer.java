package com.zergatul.cheatutils.scripting;

@FunctionalInterface
public interface ChatMessageConsumer {
    void accept(String text);
}