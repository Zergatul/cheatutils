package com.zergatul.cheatutils.scripting.compiler;

import com.zergatul.cheatutils.scripting.compiler.types.SType;

import java.util.HashMap;
import java.util.Map;

public class VariableContext {

    private final int startIndex;
    private final Map<String, VariableEntry> variables = new HashMap<>();

    public VariableContext(int startIndex) {
        this.startIndex = startIndex;
    }

    public VariableEntry add(String identifier, SType type, int index) {
        VariableEntry variable = new VariableEntry(type, index);
        if (identifier != null) {
            variables.put(identifier, variable);
        }
        return variable;
    }

    public boolean contains(String identifier) {
        return variables.containsKey(identifier);
    }

    public VariableEntry get(String identifier) {
        return variables.get(identifier);
    }

    public int getStartIndex() {
        return startIndex;
    }
}