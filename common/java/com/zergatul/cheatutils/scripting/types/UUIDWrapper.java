package com.zergatul.cheatutils.scripting.types;

import com.zergatul.cheatutils.scripting.HiddenMethod;
import com.zergatul.scripting.type.CustomType;

import java.util.UUID;

@CustomType(name = "UUID")
public class UUIDWrapper {

    private final UUID id;

    public UUIDWrapper(UUID id) {
        this.id = id;
    }

    @HiddenMethod
    public UUID getRaw() {
        return id;
    }

    @Override
    public String toString() {
        return id.toString();
    }
}