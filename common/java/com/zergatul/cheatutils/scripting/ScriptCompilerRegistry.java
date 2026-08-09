package com.zergatul.cheatutils.scripting;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.compiler.ExpressionCompilationResult;

public class ScriptCompilerRegistry {

    public static final ScriptCompilerRegistry INSTANCE = new ScriptCompilerRegistry();

    private final CompilationParameters[] parameters;
    private final Compiler[] compilers;

    private ScriptCompilerRegistry() {
        ScriptType[] types = ScriptType.values();
        parameters = new CompilationParameters[types.length];
        compilers = new Compiler[types.length];

        for (ScriptType type : types) {
            int index = type.ordinal();
            parameters[index] = type.createParameters();
            compilers[index] = new Compiler(parameters[index]);
        }
    }

    public CompilationParameters getParameters(ScriptType scriptType) {
        return parameters[scriptType.ordinal()];
    }

    public CompilationResult compile(ScriptType scriptType, String code) {
        return compilers[scriptType.ordinal()].compile(code);
    }

    public ExpressionCompilationResult compileAsExpression(ScriptType scriptType, String code) {
        return compilers[scriptType.ordinal()].compileAsExpression(code);
    }
}