package com.zergatul.cheatutils.scripting.services;

import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class ScriptStorageDescriptor {

    public String getCode() {
        return getCode(null);
    }

    public String getLastAttemptedCode() {
        return getLastAttemptedCode(null);
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