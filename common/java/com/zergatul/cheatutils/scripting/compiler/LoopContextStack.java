package com.zergatul.cheatutils.scripting.compiler;

import java.util.Stack;

public class LoopContextStack {

    private final Stack<LoopContext> stack = new Stack<>();

    public void compileContinue(CompilerMethodVisitor visitor) throws ScriptCompileException {
        stack.peek().compileContinue(visitor);
    }

    public void compileBreak(CompilerMethodVisitor visitor) throws ScriptCompileException {
        stack.peek().compileBreak(visitor);
    }

    public void push(CompileConsumer continueConsumer, CompileConsumer breakConsumer) {
        stack.push(new LoopContext(continueConsumer, breakConsumer));
    }

    public void pop() {
        stack.pop();
    }
}