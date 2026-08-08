package com.zergatul.cheatutils.scripting.workspace;

import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class ScriptSlot {

    public ScriptSaveResult init(@Nullable String code) {
        return init(null, code);
    }

    public ScriptSaveResult save(@Nullable String code) {
        return save(null, code);
    }

    public abstract List<ScriptDocument> getInstances();
    public abstract ScriptDocument getInstance(@Nullable String identifier);
    public abstract ScriptSaveResult init(@Nullable String identifier, @Nullable String code);
    public abstract ScriptCompileResult compile(String code);
    public abstract ScriptSaveResult save(@Nullable String identifier, @Nullable String code);
}