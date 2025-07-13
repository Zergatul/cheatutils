package com.zergatul.cheatutils.scripting.types;

import com.zergatul.scripting.type.CustomType;

import java.util.UUID;

@CustomType(name = "UUID")
public class UUIDWrapper {

    private final UUID id;

    public UUIDWrapper(UUID id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}