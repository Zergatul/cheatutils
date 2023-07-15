package com.zergatul.cheatutils.scripting.compiler;

public class LoopContext {

    private final CompileConsumer continueConsumer;
    private final CompileConsumer breakConsumer;

    public LoopContext(CompileConsumer continueConsumer, CompileConsumer breakConsumer) {
        this.continueConsumer = continueConsumer;
        this.breakConsumer = breakConsumer;
    }

    public void compileContinue(CompilerMethodVisitor visitor) throws ScriptCompileException {
        continueConsumer.apply(visitor);
    }

    public void compileBreak(CompilerMethodVisitor visitor) throws ScriptCompileException {
        breakConsumer.apply(visitor);
    }
}