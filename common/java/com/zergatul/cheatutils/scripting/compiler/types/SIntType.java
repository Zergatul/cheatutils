package com.zergatul.cheatutils.scripting.compiler.types;

import com.zergatul.cheatutils.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.cheatutils.scripting.compiler.operations.BinaryOperation;
import com.zergatul.cheatutils.scripting.compiler.operations.UnaryOperation;

import static org.objectweb.asm.Opcodes.*;

public class SIntType extends SPrimitiveType {

    public static final SIntType instance = new SIntType();

    private SIntType() {
        super(int.class);
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
        return T_INT;
    }

    @Override
    public int getArrayLoadInst() {
        return IALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return IASTORE;
    }

    @Override
    public BinaryOperation add(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_ADD_INT;
        }
        if (other == SFloatType.instance) {
            return BinaryOperation.INT_ADD_FLOAT;
        }
        return null;
    }

    @Override
    public BinaryOperation subtract(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_SUBTRACT_INT;
        }
        if (other == SFloatType.instance) {
            return BinaryOperation.INT_SUBTRACT_FLOAT;
        }
        return null;
    }

    @Override
    public BinaryOperation multiply(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_MULTIPLY_INT;
        }
        if (other == SFloatType.instance) {
            return BinaryOperation.INT_MULTIPLY_FLOAT;
        }
        return null;
    }

    @Override
    public BinaryOperation divide(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_DIVIDE_INT;
        }
        if (other == SFloatType.instance) {
            return BinaryOperation.INT_DIVIDE_FLOAT;
        }
        return null;
    }

    @Override
    public BinaryOperation modulo(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_MODULO_INT;
        }
        if (other == SFloatType.instance) {
            return BinaryOperation.INT_MODULO_FLOAT;
        }
        return null;
    }

    @Override
    public BinaryOperation floorDiv(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_FLOORDIV_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation floorMod(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_FLOORMOD_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation lessThan(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_LESS_INT;
        }
        if (other == SFloatType.instance) {
            return BinaryOperation.INT_LESS_FLOAT;
        }
        return null;
    }

    @Override
    public BinaryOperation greaterThan(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_GREATER_INT;
        }
        if (other == SFloatType.instance) {
            return BinaryOperation.INT_GREATER_FLOAT;
        }
        return null;
    }

    @Override
    public BinaryOperation lessEquals(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_LESS_EQUALS_INT;
        }
        if (other == SFloatType.instance) {
            return BinaryOperation.INT_LESS_EQUALS_FLOAT;
        }
        return null;
    }

    @Override
    public BinaryOperation greaterEquals(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_GREATER_EQUALS_INT;
        }
        if (other == SFloatType.instance) {
            return BinaryOperation.INT_GREATER_EQUALS_FLOAT;
        }
        return null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_EQUALS_INT;
        }
        if (other == SFloatType.instance) {
            return BinaryOperation.INT_EQUALS_FLOAT;
        }
        return null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        if (other == SIntType.instance) {
            return BinaryOperation.INT_NOT_EQUALS_INT;
        }
        if (other == SFloatType.instance) {
            return BinaryOperation.INT_NOT_EQUALS_FLOAT;
        }
        return null;
    }

    @Override
    public UnaryOperation plus() {
        return UnaryOperation.PLUS_INT;
    }

    @Override
    public UnaryOperation minus() {
        return UnaryOperation.MINUS_INT;
    }
}