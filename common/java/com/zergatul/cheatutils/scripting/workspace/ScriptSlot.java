package com.zergatul.cheatutils.scripting.workspace;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public abstract class ScriptSlot {

    public @Nullable String getCode() {
        return getCode(null);
    }

    public @Nullable String getLastAttemptedCode() {
        return getLastAttemptedCode(null);
    }

    public ScriptSaveResult init(@Nullable String code) {
        return init(null, code);
    }

    public ScriptSaveResult save(String code) {
        return save(null, code);
    }

    public abstract List<ScriptDocument> getInstances();
    public abstract @Nullable String getCode(@Nullable String identifier);
    public abstract @Nullable String getLastAttemptedCode(@Nullable String identifier);
    public abstract ScriptSaveResult init(@Nullable String identifier, @Nullable String code);
    public abstract ScriptCompileResult compile(String code);
    public abstract ScriptSaveResult save(@Nullable String identifier, @Nullable String code);
}