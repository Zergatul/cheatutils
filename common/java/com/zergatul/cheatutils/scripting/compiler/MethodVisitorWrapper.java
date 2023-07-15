package com.zergatul.cheatutils.scripting.compiler;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

public class MethodVisitorWrapper extends CompilerMethodVisitor {

    private final MethodVisitor visitor;
    private final VariableContextStack contexts;
    private final LoopContextStack loops;

    public MethodVisitorWrapper(MethodVisitor visitor) {
        this.visitor = visitor;
        this.contexts = new VariableContextStack();
        this.loops = new LoopContextStack();
    }

    public VariableContextStack getContextStack() {
        return contexts;
    }

    public LoopContextStack getLoops() {
        return loops;
    }

    @Override
    public void visitInsn(int opcode) {
        visitor.visitInsn(opcode);
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        visitor.visitIntInsn(opcode, operand);
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        visitor.visitFieldInsn(opcode, owner, name, descriptor);
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        visitor.visitJumpInsn(opcode, label);
    }

    @Override
    public void visitLabel(Label label) {
        visitor.visitLabel(label);
    }

    @Override
    public void visitLdcInsn(Object value) {
        visitor.visitLdcInsn(value);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        visitor.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
    }

    @Override
    public void visitTypeInsn(int opcode, String descriptor) {
        visitor.visitTypeInsn(opcode, descriptor);
    }

    @Override
    public void visitVarInsn(int opcode, int index) {
        visitor.visitVarInsn(opcode, index);
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        visitor.visitIincInsn(varIndex, increment);
    }
}