package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.cheatutils.scripting.ScriptType;

import java.util.Objects;

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScriptRef) {
            ScriptRef other = (ScriptRef) obj;
            return other.type.equals(this.type) && other.identifier.equals(this.identifier);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, identifier);
    }
}