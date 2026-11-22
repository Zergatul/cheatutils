package com.zergatul.cheatutils.scripting;

import com.zergatul.scripting.compiler.*;
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

    /**
     * Generates the program class without running any script code, so it is safe on any thread.
     * Create the program with {@link #instantiate} on the thread that will run it.
     */
    public ClassCompilationResult compile(ScriptType scriptType, String code) {
        return compilers[scriptType.ordinal()].compileClass(code);
    }

    public ExpressionCompilationResult compileAsExpression(ScriptType scriptType, String code) {
        return compilers[scriptType.ordinal()].compileAsExpression(code);
    }

    /**
     * Creates the program, which runs the script's static initializers. Call it on the client thread.
     * Throws {@link ScriptInitializationException} when an initializer fails.
     */
    public static <T> T instantiate(ClassCompilationResult compilation) {
        Class<T> program = compilation.getProgram();
        try {
            return program.getConstructor().newInstance();
        } catch (ExceptionInInitializerError e) {
            throw new ScriptInitializationException(e.getCause() != null ? e.getCause() : e);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Cannot instantiate compiled script.", e);
        }
    }

    private void init() {
        for (ScriptType scriptType : ScriptType.values()) {
            int index = scriptType.ordinal();
            parameters[index] = scriptType.createParameters();
            compilers[index] = new Compiler(parameters[index]);
        }
    }
}