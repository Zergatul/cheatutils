package com.zergatul.cheatutils.scripting.compiler.types;

import com.zergatul.cheatutils.scripting.compiler.CompilerMethodVisitor;

import static org.objectweb.asm.Opcodes.*;

public class SAction extends SType {

    public static final SAction instance = new SAction();

    private SAction() {

    }

    @Override
    public Class<?> getJavaClass() {
        return Runnable.class;
    }

    @Override
    public void storeDefaultValue(CompilerMethodVisitor visitor) {
        throw new IllegalStateException("SAction storeDefaultValue not implemented.");
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
    public boolean isReference() {
        return true;
    }
}