package com.zergatul.cheatutils.utils.mappings;

import net.minecraft.world.inventory.AbstractContainerMenu;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContainerMenuMappingGenerator {

    public static String generate() {
        String path = "..\\..\\fabric\\.gradle\\loom-cache\\minecraftMaven\\net\\minecraft\\minecraft-merged-ee9961217e\\1.21-loom.mappings.1_21.layered+hash.2198-v2\\minecraft-merged-ee9961217e-1.21-loom.mappings.1_21.layered+hash.2198-v2.jar";
        Map<String, JarClassEntry> map = JarClassEntry.buildMap(path);
        String baseClassName = AbstractContainerMenu.class.getName().replace('.', '/');
        List<JarClassEntry> subClasses = new ArrayList<>();
        for (JarClassEntry entry : map.values()) {
            if (entry.isSubClassOf(map, baseClassName)) {
                subClasses.add(entry);
            }
        }

        StringBuilder builder = new StringBuilder();
        Mappings.process((obf, norm) -> {
             if (subClasses.stream().anyMatch(e -> e.getClassName().equals(norm))) {
                 builder
                         .append('"')
                         .append(obf.replace('/', '.'))
                         .append(":")
                         .append(norm.replace('/', '.'))
                         .append('"')
                         .append(",\r\n");
             }
        });

        return builder.toString();
    }
}