package com.zergatul.cheatutils.scripting;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;

public class ScriptCompilerRegistry {

    public static final ScriptCompilerRegistry INSTANCE = new ScriptCompilerRegistry();

    private final CompilationParameters[] parameters;
    private final Compiler[] compilers;

    private ScriptCompilerRegistry() {
        this.parameters = new CompilationParameters[ScriptType.values().length];
        this.compilers = new Compiler[ScriptType.values().length];

        for (ScriptType scriptType : ScriptType.values()) {
            int index = scriptType.ordinal();
            parameters[index] = scriptType.createParameters();
            compilers[index] = new Compiler(parameters[index]);
        }
    }

    public CompilationParameters getParameters(ScriptType type) {
        return parameters[type.ordinal()];
    }

    public CompilationResult compile(ScriptType type, String code) {
        if (code == null) {
            throw new IllegalArgumentException("Code is required.");
        }

        return compilers[type.ordinal()].compile(code);
    }
}