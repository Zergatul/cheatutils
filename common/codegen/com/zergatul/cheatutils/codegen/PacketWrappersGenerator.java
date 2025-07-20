package com.zergatul.cheatutils.codegen;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static org.objectweb.asm.Opcodes.*;

public class PacketWrappersGenerator {

    private static final String DIRECTORY = "../common/java/com/zergatul/cheatutils/scripting/types/packets";
    private static final String EOL = "\r\n";

    private static final Map<String, ClassNode> parsedClasses = new HashMap<>();

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new RuntimeException("Expected 2 args: <minecraft-jar> <mappings.tiny>");
        }

        parsedClasses.clear();

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

                PacketClassInfo info;
                try (InputStream stream = jar.getInputStream(jarEntry)) {
                    info = getPacketClassInfo(new ClassReader(stream));
                }

                extractPacketType(map, jar, info);
                generateWrapper(classEntry, info);
                infos.add(info);
            }
        }

        generateBaseWrapper(infos);
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

    private static void extractPacketType(Map<String, JarClassEntry> map, JarFile jar, PacketClassInfo info) throws IOException {
        if (!parsedClasses.containsKey(info.typeOwner)) {
            JarClassEntry classEntry = map.get(info.typeOwner);
            JarEntry jarEntry = jar.getJarEntry(classEntry.getJarEntryName());

            ClassNode classNode = new ClassNode();
            try (InputStream stream = jar.getInputStream(jarEntry)) {
                new ClassReader(stream).accept(classNode, 0);
            }

            parsedClasses.put(info.typeOwner, classNode);
        }

        ClassNode classNode = parsedClasses.get(info.typeOwner);
        MethodNode staticConstructor = classNode.methods.stream().filter(m -> m.name.equals("<clinit>")).findFirst().orElseThrow();

        /*
            LDC "chat_ack"
            INVOKESTATIC net/minecraft/network/protocol/game/GamePacketTypes.createServerbound (Ljava/lang/String;)Lnet/minecraft/network/protocol/PacketType;
            PUTSTATIC net/minecraft/network/protocol/game/GamePacketTypes.SERVERBOUND_CHAT_ACK : Lnet/minecraft/network/protocol/PacketType;
        */

        for (AbstractInsnNode insn : staticConstructor.instructions) {
            if (insn.getOpcode() != PUTSTATIC) {
                continue;
            }

            FieldInsnNode fieldInsnNode = (FieldInsnNode) insn;
            if (fieldInsnNode.owner.equals(info.typeOwner) && fieldInsnNode.name.equals(info.typeField)) {
                // validate prev instruction
                if (insn.getPrevious().getOpcode() != INVOKESTATIC) {
                    throw new RuntimeException();
                }
                if (insn.getPrevious().getPrevious().getOpcode() != LDC) {
                    throw new RuntimeException();
                }

                String methodName = ((MethodInsnNode) insn.getPrevious()).name;
                if (methodName.equals("createServerbound")) {
                    info.isServerbound = true;
                } else if (methodName.equals("createClientbound")) {
                    info.isServerbound = false;
                } else {
                    throw new RuntimeException();
                }

                info.type = (String) ((LdcInsnNode) insn.getPrevious().getPrevious()).cst;
            }
        }
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
        builder.append("public class ").append(wrapperClassName).append(" extends PacketWrapper {").append(EOL);
        builder.append(EOL);
        builder.append("    private final ").append(className.replace('$', '.')).append(" packet;").append(EOL);
        builder.append(EOL);
        builder.append("    ").append(wrapperClassName).append("(").append(className.replace('$', '.')).append(" packet) {").append(EOL);
        builder.append("        this.packet = packet;").append(EOL);
        builder.append("    }").append(EOL);
        builder.append("}");

        Files.writeString(Path.of(DIRECTORY, wrapperClassName + ".java"), builder.toString());

        info.vanillaClassName = fullClassName.replace('$', '.');
        info.wrapperClassName = wrapperClassName;
    }

    private static void generateBaseWrapper(List<PacketClassInfo> infos) throws IOException {
        StringBuilder builder = new StringBuilder();
        builder.append("package com.zergatul.cheatutils.scripting.types.packets;").append(EOL);
        builder.append(EOL);
        builder.append("import com.zergatul.scripting.type.CustomType;").append(EOL);
        builder.append("import net.minecraft.network.protocol.Packet;").append(EOL);
        builder.append("import net.minecraft.network.protocol.PacketFlow;").append(EOL);
        builder.append("import net.minecraft.resources.ResourceLocation;").append(EOL);
        builder.append(EOL);
        builder.append("@CustomType(name = \"Packet\")").append(EOL);
        builder.append("public abstract class PacketWrapper {").append(EOL);
        builder.append(EOL);
        builder.append("    public static PacketWrapper fromPacket(Packet<?> packet) {").append(EOL);
        builder.append("        ResourceLocation id = packet.type().id();").append(EOL);
        builder.append("        if (packet.type().flow() == PacketFlow.SERVERBOUND) {").append(EOL);
        builder.append("            return switch (id.getPath()) {").append(EOL);
        for (PacketClassInfo info : infos) {
            if (info.isServerbound) {
                builder.append("                case \"").append(info.type).append("\"");
                builder.append(" -> ").append("new ").append(info.wrapperClassName).append("((").append(info.vanillaClassName).append(") packet);").append(EOL);
            }
        }
        builder.append("                default -> null;").append(EOL);
        builder.append("            };").append(EOL);
        builder.append("        } else {").append(EOL);
        builder.append("            return switch (id.getPath()) {").append(EOL);
        for (PacketClassInfo info : infos) {
            if (!info.isServerbound) {
                builder.append("                case \"").append(info.type).append("\"");
                builder.append(" -> ").append("new ").append(info.wrapperClassName).append("((").append(info.vanillaClassName).append(") packet);").append(EOL);
            }
        }
        builder.append("                default -> null;").append(EOL);
        builder.append("            };").append(EOL);
        builder.append("        }").append(EOL);
        builder.append("    }").append(EOL);
        builder.append("}");

        Files.writeString(Path.of(DIRECTORY, "PacketWrapper.java"), builder.toString());
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
        public String type;
        public boolean isServerbound;
        public String vanillaClassName;
        public String wrapperClassName;
    }
}