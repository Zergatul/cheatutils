package com.zergatul.cheatutils.scripting.compiler;

@FunctionalInterface
public interface CompileConsumer {
    void apply(CompilerMethodVisitor visitor) throws ScriptCompileException;
}
