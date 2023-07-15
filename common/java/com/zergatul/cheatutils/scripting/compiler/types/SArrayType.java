package com.zergatul.cheatutils.scripting.compiler.types;

import com.zergatul.cheatutils.scripting.compiler.CompilerMethodVisitor;
import com.zergatul.cheatutils.scripting.compiler.ScriptCompileException;
import org.objectweb.asm.Type;

import static org.objectweb.asm.Opcodes.*;

public class SArrayType extends SType {

    private final SType type;

    public SArrayType(SType type) {
        this.type = type;
    }

    public SType getElementsType() {
        return type;
    }

    @Override
    public Class<?> getJavaClass() {
        return type.getJavaClass().arrayType();
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
    public int getArrayLoadInst() {
        return AALOAD;
    }

    @Override
    public int getArrayStoreInst() {
        return AASTORE;
    }

    @Override
    public void storeDefaultValue(CompilerMethodVisitor visitor) {
        visitor.visitInsn(ICONST_0);
        if (type.isReference()) {
            visitor.visitTypeInsn(ANEWARRAY, Type.getInternalName(type.getJavaClass()));
        } else {
            visitor.visitIntInsn(NEWARRAY, ((SPrimitiveType) type).getArrayTypeInst());
        }
    }

    @Override
    public SType compileGetField(String field, CompilerMethodVisitor visitor) throws ScriptCompileException {
        if (field.equals("length")) {
            visitor.visitInsn(ARRAYLENGTH);
            return SIntType.instance;
        } else {
            return super.compileGetField(field, visitor);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof SArrayType other) {
            return type.equals(other.type);
        } else {
            return super.equals(obj);
        }
    }
}