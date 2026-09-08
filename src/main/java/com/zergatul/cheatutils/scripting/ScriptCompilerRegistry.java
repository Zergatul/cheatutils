package com.zergatul.cheatutils.scripting;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;

public class ScriptCompilerRegistry {
    public static final ScriptCompilerRegistry INSTANCE = new ScriptCompilerRegistry();
    private final CompilationParameters[] parameters = new CompilationParameters[ScriptType.values().length];
    private final Compiler[] compilers = new Compiler[ScriptType.values().length];

    private ScriptCompilerRegistry() {
        for (ScriptType type : ScriptType.values()) {
            parameters[type.ordinal()] = type.createParameters();
            compilers[type.ordinal()] = new Compiler(parameters[type.ordinal()]);
        }
    }

    public CompilationParameters getParameters(ScriptType type) {
        return parameters[type.ordinal()];
    }

    public CompilationResult compile(ScriptType type, String code) {
        if (code == null) throw new IllegalArgumentException("Code is required.");
        return compilers[type.ordinal()].compile(code);
    }
}
