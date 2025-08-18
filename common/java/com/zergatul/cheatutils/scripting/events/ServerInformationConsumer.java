package com.zergatul.cheatutils.scripting.events;

@FunctionalInterface
public interface ServerInformationConsumer {
    void accept(ServerInformation info);
}