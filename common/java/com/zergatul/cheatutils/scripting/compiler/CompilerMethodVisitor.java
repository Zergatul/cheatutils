package com.zergatul.cheatutils.scripting.compiler;

import org.objectweb.asm.Label;

public abstract class CompilerMethodVisitor {
    public abstract VariableContextStack getContextStack();
    public abstract LoopContextStack getLoops();
    public abstract void visitInsn(final int opcode);
    public abstract void visitIntInsn(final int opcode, final int operand);
    public abstract void visitFieldInsn(final int opcode, final String owner, final String name, final String descriptor);
    public abstract void visitJumpInsn(final int opcode, final Label label);
    public abstract void visitLabel(final Label label);
    public abstract void visitLdcInsn(final Object value);
    public abstract void visitMethodInsn(final int opcode, final String owner, final String name, final String descriptor, final boolean isInterface);
    public abstract void visitTypeInsn(final int opcode, final String descriptor);
    public abstract void visitVarInsn(final int opcode, final int index);
    public abstract void visitIincInsn(final int varIndex, final int increment);
}