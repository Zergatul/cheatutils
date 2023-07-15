package com.zergatul.cheatutils.scripting.compiler.types;

import com.zergatul.cheatutils.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.cheatutils.scripting.compiler.operations.BinaryOperation;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import com.zergatul.cheatutils.scripting.compiler.operations.UnaryOperation;
import com.zergatul.cheatutils.scripting.generated.*;

public abstract class SType {

    public abstract Class<?> getJavaClass();
    public abstract void storeDefaultValue(CompilerMethodVisitor visitor);
    public abstract int getLoadInst();
    public abstract int getStoreInst();
    public abstract int getArrayLoadInst();
    public abstract int getArrayStoreInst();
    public abstract boolean isReference();

    public BinaryOperation add(SType other) {
        return null;
    }

    public BinaryOperation subtract(SType other) {
        return null;
    }

    public BinaryOperation multiply(SType other) {
        return null;
    }

    public BinaryOperation divide(SType other) {
        return null;
    }

    public BinaryOperation modulo(SType other) {
        return null;
    }

    public BinaryOperation floorMod(SType other) {
        return null;
    }

    public BinaryOperation floorDiv(SType other) {
        return null;
    }

    public BinaryOperation lessThan(SType other) {
        return null;
    }

    public BinaryOperation greaterThan(SType other) {
        return null;
    }

    public BinaryOperation lessEquals(SType other) {
        return null;
    }

    public BinaryOperation greaterEquals(SType other) {
        return null;
    }

    public BinaryOperation equalsOp(SType other) {
        return null;
    }

    public BinaryOperation notEqualsOp(SType other) {
        return null;
    }

    public BinaryOperation and(SType other) {
        return null;
    }

    public BinaryOperation or(SType other) {
        return null;
    }

    public BinaryOperation binary(Node node, SType other) throws ScriptCompileException {
        if (node instanceof ASTPlus) {
            return add(other);
        } if (node instanceof ASTMinus) {
            return subtract(other);
        } if (node instanceof ASTMult) {
            return multiply(other);
        } if (node instanceof ASTDiv) {
            return divide(other);
        } if (node instanceof ASTMod) {
            return modulo(other);
        } if (node instanceof ASTFloorDiv) {
            return floorDiv(other);
        } if (node instanceof ASTFloorMod) {
            return floorMod(other);
        } if (node instanceof ASTLessThan) {
            return lessThan(other);
        } if (node instanceof ASTGreaterThan) {
            return greaterThan(other);
        } if (node instanceof ASTLessEquals) {
            return lessEquals(other);
        } if (node instanceof ASTGreaterEquals) {
            return greaterEquals(other);
        } if (node instanceof ASTEquality) {
            return equalsOp(other);
        } if (node instanceof ASTInequality) {
            return notEqualsOp(other);
        } if (node instanceof ASTAnd) {
            return and(other);
        } if (node instanceof ASTOr) {
            return or(other);
        } else {
            throw new ScriptCompileException(String.format("Unexpected operator %s.", node.getClass().getSimpleName()));
        }
    }

    public UnaryOperation plus() {
        return null;
    }

    public UnaryOperation minus() {
        return null;
    }

    public UnaryOperation not() {
        return null;
    }

    public UnaryOperation unary(Node node) throws ScriptCompileException {
        if (node instanceof ASTPlus) {
            return plus();
        } else if (node instanceof ASTMinus) {
            return minus();
        } else if (node instanceof ASTNot) {
            return not();
        } else {
            throw new ScriptCompileException(String.format("Unexpected operator %s.", node.getClass().getSimpleName()));
        }
    }

    public SType compileGetField(String field, CompilerMethodVisitor visitor) throws ScriptCompileException {
        return null;
    }

    public static SType fromJavaClass(Class<?> type) throws ScriptCompileException {
        if (type == void.class) {
            return SVoidType.instance;
        }
        if (type == boolean.class) {
            return SBoolean.instance;
        }
        if (type == int.class) {
            return SIntType.instance;
        }
        if (type == double.class) {
            return SFloatType.instance;
        }
        if (type == String.class) {
            return SStringType.instance;
        }
        if (type.isArray()) {
            return new SArrayType(fromJavaClass(type.getComponentType()));
        }
        throw new ScriptCompileException("Invalid java type.");
    }
}