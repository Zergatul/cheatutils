package com.zergatul.cheatutils.utils;

import java.io.*;
import java.nio.file.*;

public class ResourceHelper {
    public static InputStream get(String path) throws IOException {
        if (!isSafe(path)) {
            return null;
        }
        String directory = System.getProperty("cheatutils.web.dir");
        if (directory != null && path.startsWith("web/")) {
            return openLocal(Paths.get(directory), path.substring(4));
        }
        return ResourceHelper.class.getClassLoader().getResourceAsStream(path);
    }

    public static boolean isSafe(String path) {
        if (path.startsWith("/") || path.contains("\\") || path.contains(":")) return false;
        for (String part : path.split("/")) {
            if (part.equals("..") || part.equals(".") || part.indexOf('\0') >= 0) return false;
        }
        return true;
    }

    public static InputStream openLocal(Path directory, String relative) throws IOException {
        if (!isSafe(relative) || !Files.isDirectory(directory)) return null;
        Path root = directory.toRealPath();
        Path file = root.resolve(relative).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) return null;
        file = file.toRealPath();
        if (!file.startsWith(root)) return null;
        return Files.newInputStream(file);
    }
}
