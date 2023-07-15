package com.zergatul.cheatutils.scripting.compiler.operations;

import com.zergatul.cheatutils.scripting.compiler.BufferVisitor;
import com.zergatul.cheatutils.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import com.zergatul.cheatutils.scripting.compiler.types.*;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Objects;

import static org.objectweb.asm.Opcodes.*;

public abstract class BinaryOperation {

    public abstract SType getType();
    public abstract void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException;

    public static final BinaryOperation BOOLEAN_OR_BOOLEAN = new BinaryOperation() {
        @Override
        public SType getType() {
            return SBoolean.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(IOR);
        }
    };

    public static final BinaryOperation BOOLEAN_AND_BOOLEAN = new BinaryOperation() {
        @Override
        public SType getType() {
            return SBoolean.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(IAND);
        }
    };

    public static final BinaryOperation INT_ADD_INT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SIntType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(IADD);
        }
    };

    public static final BinaryOperation INT_SUBTRACT_INT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SIntType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(ISUB);
        }
    };

    public static final BinaryOperation INT_MULTIPLY_INT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SIntType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(IMUL);
        }
    };

    public static final BinaryOperation INT_DIVIDE_INT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SIntType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(IDIV);
        }
    };

    public static final BinaryOperation INT_MODULO_INT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SIntType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(IREM);
        }
    };

    public static final BinaryOperation INT_FLOORDIV_INT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SIntType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);

            Method method;
            try {
                method = Math.class.getDeclaredMethod("floorDiv", int.class, int.class);
            } catch (NoSuchMethodException e) {
                throw new ScriptCompileException("Cannot find Math.floorDiv(int, int) method.");
            }
            left.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(Math.class),
                    method.getName(),
                    Type.getMethodDescriptor(method),
                    false);
        }
    };

    public static final BinaryOperation INT_FLOORMOD_INT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SIntType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);

            Method method;
            try {
                method = Math.class.getDeclaredMethod("floorMod", int.class, int.class);
            } catch (NoSuchMethodException e) {
                throw new ScriptCompileException("Cannot find Math.floorMod(int, int) method.");
            }
            left.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(Math.class),
                    method.getName(),
                    Type.getMethodDescriptor(method),
                    false);
        }
    };

    public static final BinaryOperation INT_LESS_INT = new IntCompare(IF_ICMPLT);

    public static final BinaryOperation INT_GREATER_INT = new IntCompare(IF_ICMPGT);

    public static final BinaryOperation INT_LESS_EQUALS_INT = new IntCompare(IF_ICMPLE);

    public static final BinaryOperation INT_GREATER_EQUALS_INT = new IntCompare(IF_ICMPGE);

    public static final BinaryOperation INT_EQUALS_INT = new IntCompare(IF_ICMPEQ);

    public static final BinaryOperation INT_NOT_EQUALS_INT = new IntCompare(IF_ICMPNE);

    public static final BinaryOperation FLOAT_ADD_FLOAT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SFloatType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(DADD);
        }
    };

    public static final BinaryOperation FLOAT_SUBTRACT_FLOAT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SFloatType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(DSUB);
        }
    };

    public static final BinaryOperation FLOAT_MULTIPLY_FLOAT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SFloatType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(DMUL);
        }
    };

    public static final BinaryOperation FLOAT_DIVIDE_FLOAT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SFloatType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(DDIV);
        }
    };

    public static final BinaryOperation FLOAT_MODULO_FLOAT = new BinaryOperation() {
        @Override
        public SType getType() {
            return SFloatType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            left.visitInsn(DREM);
        }
    };

    public static final BinaryOperation FLOAT_LESS_FLOAT = new FloatCompare(IF_ICMPLT);

    public static final BinaryOperation FLOAT_GREATER_FLOAT = new FloatCompare(IF_ICMPGT);

    public static final BinaryOperation FLOAT_LESS_EQUALS_FLOAT = new FloatCompare(IF_ICMPLE);

    public static final BinaryOperation FLOAT_GREATER_EQUALS_FLOAT = new FloatCompare(IF_ICMPGE);

    public static final BinaryOperation FLOAT_EQUALS_FLOAT = new FloatCompare(IF_ICMPEQ);

    public static final BinaryOperation FLOAT_NOT_EQUALS_FLOAT = new FloatCompare(IF_ICMPNE);

    public static final BinaryOperation INT_ADD_FLOAT = new CombinedBinaryOperation(
            UnaryOperation.INT_TO_FLOAT,
            FLOAT_ADD_FLOAT,
            UnaryOperation.NONE);

    public static final BinaryOperation FLOAT_ADD_INT = new CombinedBinaryOperation(
            UnaryOperation.NONE,
            FLOAT_ADD_FLOAT,
            UnaryOperation.INT_TO_FLOAT);

    public static final BinaryOperation INT_SUBTRACT_FLOAT = new CombinedBinaryOperation(
            UnaryOperation.INT_TO_FLOAT,
            FLOAT_SUBTRACT_FLOAT,
            UnaryOperation.NONE);

    public static final BinaryOperation FLOAT_SUBTRACT_INT = new CombinedBinaryOperation(
            UnaryOperation.NONE,
            FLOAT_SUBTRACT_FLOAT,
            UnaryOperation.INT_TO_FLOAT);

    public static final BinaryOperation INT_MULTIPLY_FLOAT = new CombinedBinaryOperation(
            UnaryOperation.INT_TO_FLOAT,
            FLOAT_MULTIPLY_FLOAT,
            UnaryOperation.NONE);

    public static final BinaryOperation FLOAT_MULTIPLY_INT = new CombinedBinaryOperation(
            UnaryOperation.NONE,
            FLOAT_MULTIPLY_FLOAT,
            UnaryOperation.INT_TO_FLOAT);

    public static final BinaryOperation INT_DIVIDE_FLOAT = new CombinedBinaryOperation(
            UnaryOperation.INT_TO_FLOAT,
            FLOAT_DIVIDE_FLOAT,
            UnaryOperation.NONE);

    public static final BinaryOperation FLOAT_DIVIDE_INT = new CombinedBinaryOperation(
            UnaryOperation.NONE,
            FLOAT_DIVIDE_FLOAT,
            UnaryOperation.INT_TO_FLOAT);

    public static final BinaryOperation INT_MODULO_FLOAT = new CombinedBinaryOperation(
            UnaryOperation.INT_TO_FLOAT,
            FLOAT_MODULO_FLOAT,
            UnaryOperation.NONE);

    public static final BinaryOperation FLOAT_MODULO_INT = new CombinedBinaryOperation(
            UnaryOperation.NONE,
            FLOAT_MODULO_FLOAT,
            UnaryOperation.INT_TO_FLOAT);

    public static final BinaryOperation INT_LESS_FLOAT = new CombinedBinaryOperation(
            UnaryOperation.INT_TO_FLOAT,
            FLOAT_LESS_FLOAT,
            UnaryOperation.NONE);

    public static final BinaryOperation FLOAT_LESS_INT = new CombinedBinaryOperation(
            UnaryOperation.NONE,
            FLOAT_LESS_FLOAT,
            UnaryOperation.INT_TO_FLOAT);

    public static final BinaryOperation INT_GREATER_FLOAT = new CombinedBinaryOperation(
            UnaryOperation.INT_TO_FLOAT,
            FLOAT_GREATER_FLOAT,
            UnaryOperation.NONE);

    public static final BinaryOperation FLOAT_GREATER_INT = new CombinedBinaryOperation(
            UnaryOperation.NONE,
            FLOAT_GREATER_FLOAT,
            UnaryOperation.INT_TO_FLOAT);

    public static final BinaryOperation INT_LESS_EQUALS_FLOAT = new CombinedBinaryOperation(
            UnaryOperation.INT_TO_FLOAT,
            FLOAT_LESS_EQUALS_FLOAT,
            UnaryOperation.NONE);

    public static final BinaryOperation FLOAT_LESS_EQUALS_INT = new CombinedBinaryOperation(
            UnaryOperation.NONE,
            FLOAT_LESS_EQUALS_FLOAT,
            UnaryOperation.INT_TO_FLOAT);

    public static final BinaryOperation INT_GREATER_EQUALS_FLOAT = new CombinedBinaryOperation(
            UnaryOperation.INT_TO_FLOAT,
            FLOAT_GREATER_EQUALS_FLOAT,
            UnaryOperation.NONE);

    public static final BinaryOperation FLOAT_GREATER_EQUALS_INT = new CombinedBinaryOperation(
            UnaryOperation.NONE,
            FLOAT_GREATER_EQUALS_FLOAT,
            UnaryOperation.INT_TO_FLOAT);

    public static final BinaryOperation INT_EQUALS_FLOAT = new CombinedBinaryOperation(
            UnaryOperation.INT_TO_FLOAT,
            FLOAT_EQUALS_FLOAT,
            UnaryOperation.NONE);

    public static final BinaryOperation FLOAT_EQUALS_INT = new CombinedBinaryOperation(
            UnaryOperation.NONE,
            FLOAT_EQUALS_FLOAT,
            UnaryOperation.INT_TO_FLOAT);

    public static final BinaryOperation INT_NOT_EQUALS_FLOAT = new CombinedBinaryOperation(
            UnaryOperation.INT_TO_FLOAT,
            FLOAT_NOT_EQUALS_FLOAT,
            UnaryOperation.NONE);

    public static final BinaryOperation FLOAT_NOT_EQUALS_INT = new CombinedBinaryOperation(
            UnaryOperation.NONE,
            FLOAT_NOT_EQUALS_FLOAT,
            UnaryOperation.INT_TO_FLOAT);

    public static final BinaryOperation STRING_ADD_STRING = new BinaryOperation() {
        @Override
        public SType getType() {
            return SStringType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            final int stringBuilderLocalVarIndex = 1;

            Constructor<StringBuilder> constructor;
            try {
                constructor = StringBuilder.class.getConstructor();
            } catch (NoSuchMethodException e) {
                throw new ScriptCompileException("ASTAdditiveExpression cannot find StringBuilder constructor.");
            }

            left.visitTypeInsn(NEW, Type.getInternalName(StringBuilder.class));
            left.visitVarInsn(ASTORE, stringBuilderLocalVarIndex);

            left.visitVarInsn(ALOAD, stringBuilderLocalVarIndex);
            left.visitMethodInsn(
                    INVOKESPECIAL,
                    Type.getInternalName(StringBuilder.class),
                    "<init>",
                    Type.getConstructorDescriptor(constructor),
                    false);

            left.visitVarInsn(ALOAD, stringBuilderLocalVarIndex);
            left.visitInsn(SWAP);

            Method method;
            try {
                method = StringBuilder.class.getDeclaredMethod("append", String.class);
            } catch (NoSuchMethodException e) {
                throw new ScriptCompileException("ASTAdditiveExpression cannot find StringBuilder.list method.");
            }

            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    method.getName(),
                    Type.getMethodDescriptor(method),
                    false);

            right.releaseBuffer(left);

            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    method.getName(),
                    Type.getMethodDescriptor(method),
                    false);

            try {
                method = StringBuilder.class.getDeclaredMethod("toString");
            } catch (NoSuchMethodException e) {
                throw new ScriptCompileException("ASTAdditiveExpression cannot find StringBuilder.toString method.");
            }

            left.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(StringBuilder.class),
                    method.getName(),
                    Type.getMethodDescriptor(method),
                    false);
        }
    };

    public static final BinaryOperation STRING_EQUALS_STRING = new BinaryOperation() {
        @Override
        public SType getType() {
            return SBoolean.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);

            Method equalsMethod;
            try {
                equalsMethod = Objects.class.getDeclaredMethod("equals", Object.class, Object.class);
            } catch (NoSuchMethodException e) {
                throw new ScriptCompileException("ASTEqualityExpression cannot find Objects.equals method.");
            }

            left.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(Objects.class),
                    equalsMethod.getName(),
                    Type.getMethodDescriptor(equalsMethod),
                    false);
        }
    };

    public static final BinaryOperation STRING_NOT_EQUALS_STRING = new BinaryOperation() {
        @Override
        public SType getType() {
            return SBoolean.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            STRING_EQUALS_STRING.apply(left, right);
            UnaryOperation.NOT.apply(left);
        }
    };

    private static class IntCompare extends BinaryOperation {

        private final int opcode;

        public IntCompare(int opcode) {
            this.opcode = opcode;
        }

        @Override
        public SType getType() {
            return SBoolean.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            Label elseLabel = new Label();
            Label endLabel = new Label();
            left.visitJumpInsn(opcode, elseLabel);
            left.visitInsn(ICONST_0);
            left.visitJumpInsn(GOTO, endLabel);
            left.visitLabel(elseLabel);
            left.visitInsn(ICONST_1);
            left.visitLabel(endLabel);
        }
    }

    private static class FloatCompare extends BinaryOperation {

        private final int opcode;

        public FloatCompare(int opcode) {
            this.opcode = opcode;
        }

        @Override
        public SType getType() {
            return SBoolean.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor left, BufferVisitor right) throws ScriptCompileException {
            right.releaseBuffer(left);
            Label elseLabel = new Label();
            Label endLabel = new Label();
            left.visitInsn(DCMPG);
            left.visitInsn(ICONST_0);
            left.visitJumpInsn(opcode, elseLabel);
            left.visitInsn(ICONST_0);
            left.visitJumpInsn(GOTO, endLabel);
            left.visitLabel(elseLabel);
            left.visitInsn(ICONST_1);
            left.visitLabel(endLabel);
        }
    }
}