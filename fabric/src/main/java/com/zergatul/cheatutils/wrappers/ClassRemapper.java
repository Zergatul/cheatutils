package com.zergatul.cheatutils.wrappers;

import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class ClassRemapper {

    private static final boolean enabled = !FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace().equals("named");
    private static final Map<String, String> obfToNorm = new HashMap<>();
    private static final Map<String, String> normToObf = new HashMap<>();

    public static boolean isEnabled() {
        return enabled;
    }

    public static String fromObf(String className) {
        if (!enabled) {
            return className;
        }
        return obfToNorm.getOrDefault(className, className);
    }

    public static String toObf(String className) {
        if (!enabled) {
            return className;
        }
        return normToObf.getOrDefault(className, className);
    }

    static {
        ClassLoader classLoader = ClassRemapper.class.getClassLoader();
        try (InputStream stream = classLoader.getResourceAsStream("mappings.txt")) {
            if (stream == null) {
                throw new RuntimeException("mappings.txt not found.");
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            while (reader.ready()) {
                String line = reader.readLine();
                String[] parts = line.split(":");
                String obf = parts[0];
                String norm = parts[1];
                obfToNorm.put(obf, norm);
                normToObf.put(norm, obf);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}