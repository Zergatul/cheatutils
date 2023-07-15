package com.zergatul.cheatutils.scripting.compiler.types;

import com.zergatul.cheatutils.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import com.zergatul.cheatutils.scripting.compiler.operations.BinaryOperation;
import org.objectweb.asm.Type;

import java.io.PrintStream;
import java.lang.reflect.Method;

import static org.objectweb.asm.Opcodes.*;

public class SStringType extends SPrimitiveType {

    public static final SStringType instance = new SStringType();

    private SStringType() {
        super(String.class);
    }

    @Override
    public boolean isReference() {
        return true;
    }

    @Override
    public int getLoadInst() {
        return ALOAD;
    }

    @Override
    public int getStoreInst() {
        return ASTORE;
    }

    @Override
    public void storeDefaultValue(CompilerMethodVisitor visitor) {
        visitor.visitLdcInsn("");
    }

    @Override
    public int getArrayTypeInst() {
        throw new IllegalStateException();
    }

    @Override
    public int getArrayLoadInst() {
        return AALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return AASTORE;
    }

    @Override
    public BinaryOperation add(SType other) {
        if (other == SStringType.instance) {
            return BinaryOperation.STRING_ADD_STRING;
        }
        return null;
    }

    @Override
    public BinaryOperation equalsOp(SType other) {
        if (other == SStringType.instance) {
            return BinaryOperation.STRING_EQUALS_STRING;
        }
        return null;
    }

    @Override
    public BinaryOperation notEqualsOp(SType other) {
        if (other == SStringType.instance) {
            return BinaryOperation.STRING_NOT_EQUALS_STRING;
        }
        return null;
    }

    @Override
    public SType compileGetField(String field, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (field.equals("length")) {
            Method method;
            try {
                method = String.class.getDeclaredMethod("length");
            } catch (NoSuchMethodException e) {
                throw new ScriptCompileException("Cannot find String.length method.");
            }
            visitor.visitMethodInsn(
                    INVOKEVIRTUAL,
                    Type.getInternalName(String.class),
                    method.getName(),
                    Type.getMethodDescriptor(method),
                    false);
            return SIntType.instance;
        } else {
            return super.compileGetField(field, visitor);
        }
    }
}