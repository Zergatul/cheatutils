package com.zergatul.cheatutils.scripting;

@FunctionalInterface
public interface BlockPosConsumer {
    void accept(int x, int y, int z);
}