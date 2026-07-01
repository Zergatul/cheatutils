package com.zergatul.cheatutils.scripting.structure;

import com.zergatul.scripting.compiler.CompilationResult;
import org.jspecify.annotations.Nullable;

import java.util.List;

public abstract class ScriptStorageDescriptor {

    public String getCode() {
        return getCode(null);
    }

    public String getLastAttemptedCode() {
        return getLastAttemptedCode(null);
    }

    public abstract List<ScriptInstance> getInstances();
    public abstract String getCode(@Nullable String identifier);
    public abstract String getLastAttemptedCode(@Nullable String identifier);
    public abstract CompilationResult compile(String code);
    public abstract Object save();
}