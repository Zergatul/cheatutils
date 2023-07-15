package com.zergatul.cheatutils.scripting.compiler.operations;

import com.zergatul.cheatutils.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import com.zergatul.cheatutils.scripting.compiler.types.SBoolean;
import com.zergatul.cheatutils.scripting.compiler.types.SFloatType;
import com.zergatul.cheatutils.scripting.compiler.types.SIntType;
import com.zergatul.cheatutils.scripting.compiler.types.SType;
import org.objectweb.asm.Label;
import org.objectweb.asm.Type;

import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public abstract class UnaryOperation {

    public abstract SType getType() throws ScriptCompileException;
    public abstract void apply(CompilerMethodVisitor visitor) throws ScriptCompileException;

    public static final UnaryOperation NONE = new UnaryOperation() {
        @Override
        public SType getType() throws ScriptCompileException {
            throw new ScriptCompileException("Should not be called.");
        }

        @Override
        public void apply(CompilerMethodVisitor visitor) throws ScriptCompileException {

        }
    };

    public static final UnaryOperation PLUS_INT = new UnaryOperation() {
        @Override
        public SType getType() throws ScriptCompileException {
            return SIntType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor visitor) throws ScriptCompileException {

        }
    };

    public static final UnaryOperation PLUS_FLOAT = new UnaryOperation() {
        @Override
        public SType getType() throws ScriptCompileException {
            return SFloatType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor visitor) throws ScriptCompileException {

        }
    };

    public static final UnaryOperation MINUS_INT = new UnaryOperation() {
        @Override
        public SType getType() throws ScriptCompileException {
            return SIntType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor visitor) throws ScriptCompileException {
            visitor.visitInsn(INEG);
        }
    };

    public static final UnaryOperation MINUS_FLOAT = new UnaryOperation() {
        @Override
        public SType getType() throws ScriptCompileException {
            return SFloatType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor visitor) throws ScriptCompileException {
            visitor.visitInsn(DNEG);
        }
    };

    public static final UnaryOperation INT_TO_FLOAT = new UnaryOperation() {
        @Override
        public SType getType() throws ScriptCompileException {
            return SFloatType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor visitor) {
            visitor.visitInsn(I2D);
        }
    };

    public static final UnaryOperation INT_TO_STRING = new UnaryOperation() {
        @Override
        public SType getType() throws ScriptCompileException {
            return SIntType.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor visitor) throws ScriptCompileException {
            Method method;
            try {
                method = Integer.class.getDeclaredMethod("toString", int.class);
            } catch (NoSuchMethodException e) {
                throw new ScriptCompileException("Cannot find Integer.toString() method.");
            }

            visitor.visitMethodInsn(
                    INVOKESTATIC,
                    Type.getInternalName(method.getDeclaringClass()),
                    method.getName(),
                    Type.getMethodDescriptor(method),
                    false);
        }
    };

    public static final UnaryOperation NOT = new UnaryOperation() {
        @Override
        public SType getType() throws ScriptCompileException {
            return SBoolean.instance;
        }

        @Override
        public void apply(CompilerMethodVisitor visitor) throws ScriptCompileException {
            Label elseLabel = new Label();
            Label endLabel = new Label();
            visitor.visitJumpInsn(IFNE, elseLabel);
            visitor.visitInsn(ICONST_1);
            visitor.visitJumpInsn(GOTO, endLabel);
            visitor.visitLabel(elseLabel);
            visitor.visitInsn(ICONST_0);
            visitor.visitLabel(endLabel);
        }
    };
}