package com.zergatul.cheatutils.scripting.events;

@FunctionalInterface
public interface BlockPosConsumer {
    void accept(int x, int y, int z);
}