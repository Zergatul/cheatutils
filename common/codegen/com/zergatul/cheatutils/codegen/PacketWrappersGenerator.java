package com.zergatul.cheatutils.codegen;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodNode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PacketWrappersGenerator {

    private static final String DIRECTORY = "../common/java/com/zergatul/cheatutils/scripting/types/packets";
    private static final String EOL = "\r\n";

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new RuntimeException("Expected 2 args: <minecraft-jar> <mappings.tiny>");
        }

        Map<String, JarClassEntry> map = JarClassEntry.buildMap(args[0]);
        List<JarClassEntry> packetClasses = map.values().stream()
                .filter(e -> !e.isAbstract())
                .filter(e -> e.implementsInterface(map, "net/minecraft/network/protocol/Packet"))
                .toList();

        for (File file : Path.of(DIRECTORY).toFile().listFiles()) {
            file.delete();
        }

        List<PacketClassInfo> infos = new ArrayList<>();
        try (JarFile jar = new JarFile(args[0])) {
            for (JarClassEntry classEntry : packetClasses) {
                JarEntry jarEntry = jar.getJarEntry(classEntry.getJarEntryName());
                try (InputStream stream = jar.getInputStream(jarEntry)) {
                    PacketClassInfo info = getPacketClassInfo(new ClassReader(stream));
                    generateWrapper(classEntry, info);
                    infos.add(info);
                }
            }
        }
    }

    private static PacketClassInfo getPacketClassInfo(ClassReader reader) {
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, 0);

        PacketClassInfo info = new PacketClassInfo();
        for (MethodNode methodNode : classNode.methods) {
            if (methodNode.name.equals("type") && methodNode.desc.equals("()Lnet/minecraft/network/protocol/PacketType;")) {
                InsnList instructions = methodNode.instructions;
                if (instructions.size() != 5) {
                    throw new RuntimeException();
                }
                if (instructions.get(2) instanceof FieldInsnNode fieldInsnNode) {
                    info.typeOwner = fieldInsnNode.owner;
                    info.typeField = fieldInsnNode.name;
                } else {
                    throw new RuntimeException();
                }
            }
        }

        if (info.typeOwner == null || info.typeField == null) {
            throw new RuntimeException();
        }

        return info;
    }

    private static void generateWrapper(JarClassEntry entry, PacketClassInfo info) throws IOException {
        String fullClassName = entry.getClassName().replace('/', '.');
        String className = getClassName(entry);
        String wrapperClassName = className.replace("$", "") + "Wrapper";

        StringBuilder builder = new StringBuilder();
        builder.append("package com.zergatul.cheatutils.scripting.types.packets;").append(EOL);
        builder.append(EOL);
        builder.append("import com.zergatul.scripting.type.CustomType;").append(EOL);
        builder.append("import ").append(getNonNestedClass(fullClassName)).append(";").append(EOL);
        builder.append(EOL);
        builder.append("@CustomType(name = \"").append(className.replace('$', '_')).append("\")").append(EOL);
        builder.append("public class ").append(wrapperClassName).append("Wrapper {").append(EOL);
        builder.append(EOL);
        builder.append("    private final ").append(className.replace('$', '.')).append(" packet;").append(EOL);
        builder.append(EOL);
        builder.append("    ").append(wrapperClassName).append("Wrapper(").append(className.replace('$', '.')).append(" packet) {").append(EOL);
        builder.append("        this.packet = packet;").append(EOL);
        builder.append("    }").append(EOL);
        builder.append("}");

        Files.writeString(Path.of(DIRECTORY, wrapperClassName + ".java"), builder.toString());

        info.wrapperClassName = wrapperClassName;
    }

    private static String getClassName(JarClassEntry entry) {
        return entry.getClassName().substring(entry.getClassName().lastIndexOf('/') + 1);
    }

    private static String getNonNestedClass(String fullClassName) {
        if (fullClassName.contains("$")) {
            return fullClassName.substring(0, fullClassName.indexOf("$"));
        } else {
            return fullClassName;
        }
    }

    private static class PacketClassInfo {
        public String typeOwner;
        public String typeField;
        public String wrapperClassName;
    }
}