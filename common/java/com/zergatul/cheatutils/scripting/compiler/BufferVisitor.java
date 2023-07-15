package com.zergatul.cheatutils.scripting.compiler;

import org.objectweb.asm.Label;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class BufferVisitor extends CompilerMethodVisitor {

    private final VariableContextStack contexts;
    private final List<Consumer<CompilerMethodVisitor>> list = new ArrayList<>();

    public BufferVisitor(VariableContextStack contexts) {
        this.contexts = contexts;
    }

    public VariableContextStack getContextStack() {
        return contexts;
    }

    public LoopContextStack getLoops() {
        return null;
    }

    public void releaseBuffer(CompilerMethodVisitor visitor) {
        list.forEach(c -> c.accept(visitor));
        list.clear();
    }

    @Override
    public void visitInsn(int opcode) {
        list.add(v -> v.visitInsn(opcode));
    }

    @Override
    public void visitIntInsn(int opcode, int operand) {
        list.add(v -> v.visitIntInsn(opcode, operand));
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
        list.add(v -> v.visitFieldInsn(opcode, owner, name, descriptor));
    }

    @Override
    public void visitJumpInsn(final int opcode, final Label label) {
        list.add(v -> v.visitJumpInsn(opcode, label));
    }

    @Override
    public void visitLabel(Label label) {
        list.add(v -> v.visitLabel(label));
    }

    @Override
    public void visitLdcInsn(Object value) {
        list.add(v -> v.visitLdcInsn(value));
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
        list.add(v -> v.visitMethodInsn(opcode, owner, name, descriptor, isInterface));
    }

    @Override
    public void visitTypeInsn(int opcode, String descriptor) {
        list.add(v -> v.visitTypeInsn(opcode, descriptor));
    }

    @Override
    public void visitVarInsn(int opcode, int index) {
        list.add(v -> v.visitVarInsn(opcode, index));
    }

    @Override
    public void visitIincInsn(int varIndex, int increment) {
        list.add(v -> v.visitIincInsn(varIndex, increment));
    }
}