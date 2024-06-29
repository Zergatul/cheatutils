package com.zergatul.cheatutils.utils.mappings;

import com.zergatul.cheatutils.utils.EntityUtils;
import net.minecraft.client.Minecraft;

import java.util.List;

public class EntityMappingGenerator {

    public static String generate() {
        List<EntityUtils.EntityInfo> classes = EntityUtils.getEntityClasses();
        StringBuilder builder = new StringBuilder();
        Mappings.process((obf, norm) -> {
            obf = obf.replace('/', '.');
            norm = norm.replace('/', '.');

            Class<?> clazz;
            try {
                clazz = Class.forName(norm, false, Minecraft.class.getClassLoader());
            } catch (Throwable e) {
                e.printStackTrace();
                return;
            }
            if (classes.stream().anyMatch(i -> i.clazz == clazz)) {
                builder.append('"').append(obf).append(":").append(norm).append('"').append(",\r\n");
            }
        });

        return builder.toString();
    }
}