package com.zergatul.cheatutils.codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MappingGenerator {

    public static void main(String[] args) throws IOException {
        if (args.length != 2) {
            throw new RuntimeException("Expected 2 args: <minecraft-jar> <mappings.tiny>");
        }

        Map<String, JarClassEntry> map = JarClassEntry.buildMap(args[0]);

        String[] baseClasses = new String[] {
                "net.minecraft.world.entity.Entity",
                "net.minecraft.world.inventory.AbstractContainerMenu"
        };
        for (int i = 0; i < baseClasses.length; i++) {
            baseClasses[i] = baseClasses[i].replace('.', '/');
        }

        List<JarClassEntry> subClasses = new ArrayList<>();
        for (JarClassEntry entry : map.values()) {
            for (String baseClass : baseClasses) {
                if (entry.isInstanceOf(map, baseClass)) {
                    subClasses.add(entry);
                }
            }
        }

        Set<String> interfaces = new HashSet<>();
        for (JarClassEntry entry : subClasses) {
            Collections.addAll(interfaces, entry.getInterfaces());
        }

        StringBuilder builder = new StringBuilder();
        MappingEnumerator.enumerate(args[1], (obf, norm) -> {
            if (subClasses.stream().anyMatch(e -> e.getClassName().equals(norm)) || interfaces.contains(norm)) {
                builder
                        .append(obf.replace('/', '.'))
                        .append(":")
                        .append(norm.replace('/', '.'))
                        .append("\n");
            }
        });

        Files.writeString(Path.of("src\\main\\resources\\mappings.txt"), builder.toString());
    }
}