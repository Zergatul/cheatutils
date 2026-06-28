package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.cheatutils.scripting.ScriptType;
import org.jspecify.annotations.Nullable;

public record ScriptRef(ScriptType type, @Nullable String identifier) {
    public ScriptRef(ScriptType type) {
        this(type, null);
    }
}