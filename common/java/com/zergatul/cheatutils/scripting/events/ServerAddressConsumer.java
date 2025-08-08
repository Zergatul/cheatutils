package com.zergatul.cheatutils.scripting.events;

@FunctionalInterface
public interface ServerAddressConsumer {
    void accept(String address);
}