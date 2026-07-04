package com.zergatul.cheatutils.scripting;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationResult;
import com.zergatul.scripting.compiler.Compiler;
import org.jspecify.annotations.NullMarked;

@NullMarked
public class ScriptCompilerRegistry {

    public static final ScriptCompilerRegistry INSTANCE = new ScriptCompilerRegistry();

    private final CompilationParameters[] parameters;
    private final Compiler[] compilers;

    private ScriptCompilerRegistry() {
        this.parameters = new CompilationParameters[ScriptType.values().length];
        this.compilers = new Compiler[ScriptType.values().length];
        this.init();
    }

    public CompilationParameters getParameters(ScriptType scriptType) {
        return parameters[scriptType.ordinal()];
    }

    public CompilationResult compile(ScriptType scriptType, String code) {
        return compilers[scriptType.ordinal()].compile(code);
    }

    private void init() {
        for (ScriptType scriptType : ScriptType.values()) {
            int index = scriptType.ordinal();
            parameters[index] = scriptType.createParameters();
            compilers[index] = new Compiler(parameters[index]);
        }
    }
}