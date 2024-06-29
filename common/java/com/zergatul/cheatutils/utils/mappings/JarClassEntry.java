package com.zergatul.cheatutils.utils.mappings;

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

public class JarClassEntry {

    private final String className;
    private final String superClassName;
    private final String[] interfaces;

    public JarClassEntry(String className, String superClassName, String[] interfaces) {
        this.className = className;
        this.superClassName = superClassName;
        this.interfaces = interfaces;
    }

    public static Map<String, JarClassEntry> buildMap(String path) {
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
                                map.put(name, new JarClassEntry(name, superName, interfaces));
                            }
                        }, 0);
                    }
                }
            }
        } catch (IOException e) {
            throw new RuntimeException();
        }
        return map;
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
}