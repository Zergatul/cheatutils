package com.zergatul.cheatutils.scripting;

@FunctionalInterface
public interface ServerAddressConsumer {
    void accept(String address);
}