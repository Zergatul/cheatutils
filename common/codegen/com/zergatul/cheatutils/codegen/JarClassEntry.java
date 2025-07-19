package com.zergatul.cheatutils.codegen;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.objectweb.asm.Opcodes.ACC_ABSTRACT;

public class JarClassEntry {

    private final String jarEntryName;
    private final int access;
    private final String className;
    private final String superClassName;
    private final String[] interfaces;

    public JarClassEntry(String jarEntryName, int access, String className, String superClassName, String[] interfaces) {
        this.jarEntryName = jarEntryName;
        this.access = access;
        this.className = className;
        this.superClassName = superClassName;
        this.interfaces = interfaces;
    }

    public static Map<String, JarClassEntry> buildMap(String path) throws IOException {
        Map<String, JarClassEntry> map = new HashMap<>();
        try (JarFile jar = new JarFile(path)) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".class")) {
                    try (InputStream stream = jar.getInputStream(entry)) {
                        ClassReader reader = new ClassReader(stream);
                        reader.accept(new ClassVisitor(Opcodes.ASM9) {
                            @Override
                            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                                map.put(name, new JarClassEntry(entry.getName(), access, name, superName, interfaces));
                            }
                        }, 0);
                    }
                }
            }
        }
        return map;
    }

    public String getJarEntryName() {
        return jarEntryName;
    }

    public String getClassName() {
        return className;
    }

    public String getSuperClassName() {
        return superClassName;
    }

    public String[] getInterfaces() {
        return interfaces;
    }

    public boolean isAbstract() {
        return (access & ACC_ABSTRACT) != 0;
    }

    public boolean isInstanceOf(Map<String, JarClassEntry> map, String name) {
        if (className.equals(name)) {
            return true;
        } else {
            return isSubClassOf(map, name);
        }
    }

    public boolean isSubClassOf(Map<String, JarClassEntry> map, String name) {
        if (superClassName.equals(name)) {
            return true;
        }

        JarClassEntry superClassEntry = map.get(superClassName);
        if (superClassEntry == null) {
            return false;
        } else {
            return superClassEntry.isSubClassOf(map, name);
        }
    }

    public boolean implementsInterface(Map<String, JarClassEntry> map, String name) {
        for (int i = 0; i < interfaces.length; i++) {
            if (interfaces[i].equals(name)) {
                return true;
            }
        }

        JarClassEntry superClassEntry = map.get(superClassName);
        if (superClassEntry != null) {
            return superClassEntry.implementsInterface(map, name);
        } else {
            return false;
        }
    }
}