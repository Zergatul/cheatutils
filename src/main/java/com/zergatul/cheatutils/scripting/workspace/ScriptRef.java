package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.cheatutils.scripting.ScriptType;

public final class ScriptRef {

    private final ScriptType type;
    private final String identifier;

    public ScriptRef(ScriptType type) {
        this(type, null);
    }

    public ScriptRef(ScriptType type, String identifier) {
        this.type = type;
        this.identifier = identifier;
    }

    public ScriptType getType() {
        return type;
    }

    public String getIdentifier() {
        return identifier;
    }
}