package com.zergatul.cheatutils.scripting;

@FunctionalInterface
public interface ContainerClickConsumer {
    void accept(int slot, int button, String type);
}