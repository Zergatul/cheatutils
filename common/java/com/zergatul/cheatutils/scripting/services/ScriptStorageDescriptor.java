package com.zergatul.cheatutils.scripting.services;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public abstract class ScriptStorageDescriptor {

    private final ScriptLocator locator;

    protected ScriptStorageDescriptor(ScriptLocator locator) {
        this.locator = locator;
    }

    public @Nullable String getCode() {
        return getCode(null);
    }

    public @Nullable String getLastAttemptedCode() {
        return getLastAttemptedCode(null);
    }

    public ScriptLocator getLocator() {
        return locator;
    }

    public ScriptSaveResult save(String code) {
        return save(null, code);
    }

    public abstract List<ScriptInstance> getInstances();
    public abstract String getCode(@Nullable String identifier);
    public abstract String getLastAttemptedCode(@Nullable String identifier);
    public abstract ScriptCompileResult compile(String code);
    public abstract ScriptSaveResult save(@Nullable String identifier, @Nullable String code);
}