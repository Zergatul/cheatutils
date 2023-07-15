package com.zergatul.cheatutils.scripting.compiler.types;

import com.zergatul.cheatutils.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.cheatutils.scripting.compiler.operations.BinaryOperation;
import com.zergatul.cheatutils.scripting.compiler.operations.UnaryOperation;

import static org.objectweb.asm.Opcodes.*;

public class SBoolean extends SPrimitiveType {

    public static final SBoolean instance = new SBoolean();

    private SBoolean() {
        super(boolean.class);
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public int getLoadInst() {
        return ILOAD;
    }

    @Override
    public int getStoreInst() {
        return ISTORE;
    }

    @Override
    public void storeDefaultValue(CompilerMethodVisitor visitor) {
        visitor.visitInsn(ICONST_0);
    }

    @Override
    public int getArrayTypeInst() {
        return T_BOOLEAN;
    }

    @Override
    public int getArrayLoadInst() {
        return BALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return BASTORE;
    }

    @Override
    public BinaryOperation lessThan(SType other) {
        if (other == SBoolean.instance) {
            return BinaryOperation.INT_LESS_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation greaterThan(SType other) {
        if (other == SBoolean.instance) {
            return BinaryOperation.INT_GREATER_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation lessEquals(SType other) {
        if (other == SBoolean.instance) {
            return BinaryOperation.INT_LESS_EQUALS_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation greaterEquals(SType other) {
        if (other == SBoolean.instance) {
            return BinaryOperation.INT_GREATER_EQUALS_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        if (other == SBoolean.instance) {
            return BinaryOperation.INT_EQUALS_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        if (other == SBoolean.instance) {
            return BinaryOperation.INT_NOT_EQUALS_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation and(SType other) {
        if (other == SBoolean.instance) {
            return BinaryOperation.BOOLEAN_AND_BOOLEAN;
        }
        return null;
    }

    @Override
    public BinaryOperation or(SType other) {
        if (other == SBoolean.instance) {
            return BinaryOperation.BOOLEAN_OR_BOOLEAN;
        }
        return null;
    }

    @Override
    public UnaryOperation not() {
        return UnaryOperation.NOT;
    }
}