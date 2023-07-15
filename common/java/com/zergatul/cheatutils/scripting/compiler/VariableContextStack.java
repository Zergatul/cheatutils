package com.zergatul.cheatutils.scripting.compiler;

import com.zergatul.cheatutils.scripting.compiler.types.SFloatType;
import com.zergatul.cheatutils.scripting.compiler.types.SPrimitiveType;
import com.zergatul.cheatutils.scripting.compiler.types.SType;

import java.util.Stack;

public class VariableContextStack {

    private final Stack<VariableContext> stack = new Stack<>();
    private int index = 2; // index=1 reserved for StringBuilder

    public VariableContextStack() {
        stack.add(new VariableContext(index));
    }

    public VariableEntry add(String identifier, SType type) throws ScriptCompileException {
        if (identifier != null) {
            checkIdentifier(identifier);
        }

        VariableEntry variable = stack.peek().add(identifier, type, index);
        if (type == SFloatType.instance) {
            index += 2;
        } else {
            index += 1;
        }
        return variable;
    }

    public void begin() {
        stack.add(new VariableContext(index));
    }

    public void end() {
        index = stack.pop().getStartIndex();
    }

    public VariableEntry get(String identifier) {
        for (int i = stack.size() - 1; i >= 0; i--) {
            VariableEntry entry = stack.get(i).get(identifier);
            if (entry != null) {
                return entry;
            }
        }

        return null;
    }

    private void checkIdentifier(String identifier) throws ScriptCompileException {
        for (int i = stack.size() - 1; i >= 0; i--) {
            if (stack.get(i).contains(identifier)) {
                throw new ScriptCompileException(String.format("Identifier %s is already declared.", identifier));
            }
        }
    }
}