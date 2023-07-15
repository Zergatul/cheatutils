package com.zergatul.cheatutils.scripting.compiler.types;

import com.zergatul.cheatutils.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.cheatutils.scripting.compiler.operations.BinaryOperation;
import com.zergatul.cheatutils.scripting.compiler.operations.UnaryOperation;

import static org.objectweb.asm.Opcodes.*;

public class SFloatType extends SPrimitiveType {

    public static final SFloatType instance = new SFloatType();

    private SFloatType() {
        super(double.class);
    }

    @Override
    public boolean isReference() {
        return false;
    }

    @Override
    public int getLoadInst() {
        return DLOAD;
    }

    @Override
    public int getStoreInst() {
        return DSTORE;
    }

    @Override
    public void storeDefaultValue(CompilerMethodVisitor visitor) {
        visitor.visitLdcInsn(0.0);
    }

    @Override
    public int getArrayTypeInst() {
        return T_DOUBLE;
    }

    @Override
    public int getArrayLoadInst() {
        return DALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return DASTORE;
    }

    @Override
    public BinaryOperation add(SType other) {
        if (other == SFloatType.instance) {
            return BinaryOperation.FLOAT_ADD_FLOAT;
        }
        if (other == SIntType.instance) {
            return BinaryOperation.FLOAT_ADD_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation subtract(SType other) {
        if (other == SFloatType.instance) {
            return BinaryOperation.FLOAT_SUBTRACT_FLOAT;
        }
        if (other == SIntType.instance) {
            return BinaryOperation.FLOAT_SUBTRACT_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation multiply(SType other) {
        if (other == SFloatType.instance) {
            return BinaryOperation.FLOAT_MULTIPLY_FLOAT;
        }
        if (other == SIntType.instance) {
            return BinaryOperation.FLOAT_MULTIPLY_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation divide(SType other) {
        if (other == SFloatType.instance) {
            return BinaryOperation.FLOAT_DIVIDE_FLOAT;
        }
        if (other == SIntType.instance) {
            return BinaryOperation.FLOAT_DIVIDE_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation modulo(SType other) {
        if (other == SFloatType.instance) {
            return BinaryOperation.FLOAT_MODULO_FLOAT;
        }
        if (other == SIntType.instance) {
            return BinaryOperation.FLOAT_MODULO_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation lessThan(SType other) {
        if (other == SFloatType.instance) {
            return BinaryOperation.FLOAT_LESS_FLOAT;
        }
        if (other == SIntType.instance) {
            return BinaryOperation.FLOAT_LESS_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation greaterThan(SType other) {
        if (other == SFloatType.instance) {
            return BinaryOperation.FLOAT_GREATER_FLOAT;
        }
        if (other == SIntType.instance) {
            return BinaryOperation.FLOAT_GREATER_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation lessEquals(SType other) {
        if (other == SFloatType.instance) {
            return BinaryOperation.FLOAT_LESS_EQUALS_FLOAT;
        }
        if (other == SIntType.instance) {
            return BinaryOperation.FLOAT_LESS_EQUALS_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation greaterEquals(SType other) {
        if (other == SFloatType.instance) {
            return BinaryOperation.FLOAT_GREATER_EQUALS_FLOAT;
        }
        if (other == SIntType.instance) {
            return BinaryOperation.FLOAT_GREATER_EQUALS_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        if (other == SFloatType.instance) {
            return BinaryOperation.FLOAT_EQUALS_FLOAT;
        }
        if (other == SIntType.instance) {
            return BinaryOperation.FLOAT_EQUALS_INT;
        }
        return null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        if (other == SFloatType.instance) {
            return BinaryOperation.FLOAT_NOT_EQUALS_FLOAT;
        }
        if (other == SIntType.instance) {
            return BinaryOperation.FLOAT_NOT_EQUALS_INT;
        }
        return null;
    }

    @Override
    public UnaryOperation plus() {
        return UnaryOperation.PLUS_FLOAT;
    }

    @Override
    public UnaryOperation minus() {
        return UnaryOperation.MINUS_FLOAT;
    }
}