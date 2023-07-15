package com.zergatul.cheatutils.scripting.compiler.operations;

import com.zergatul.cheatutils.scripting.compiler.BufferVisitor;
import com.zergatul.cheatutils.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import com.zergatul.cheatutils.scripting.compiler.types.SType;

public class CombinedBinaryOperation extends BinaryOperation {

    private final UnaryOperation leftOperation;
    private final BinaryOperation binaryOperation;
    private final UnaryOperation rightOperation;

    public CombinedBinaryOperation(UnaryOperation leftOperation, BinaryOperation binaryOperation, UnaryOperation rightOperation) {
        this.leftOperation = leftOperation;
        this.binaryOperation = binaryOperation;
        this.rightOperation = rightOperation;
    }

    @Override
    public SType getType() {
        return binaryOperation.getType();
    }

    @Override
    public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
        leftOperation.apply(left);
        rightOperation.apply(right);
        binaryOperation.apply(left, right);
    }
}