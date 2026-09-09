package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.common.ModLoaderInfo;

import java.io.*;
import java.nio.file.*;

public final class ResourceHelper {

    public static InputStream get(String path) throws IOException {
        if (!isSafe(path)) {
            return null;
        }

        if (path.startsWith("web/")) {
            return ModLoaderInfo.INSTANCE.isProduction() ? openStreamProduction(path) : openStreamDevelopment(path);
        } else {
            return openResource(path);
        }
    }

    public static InputStream openLocal(Path directory, String relative) throws IOException {
        if (!isSafe(relative)) {
            return null;
        }
        if (!Files.isDirectory(directory)) {
            return null;
        }

        Path root = directory.toRealPath();
        Path file = root.resolve(relative).normalize();
        if (!file.startsWith(root) || !Files.isRegularFile(file)) {
            return null;
        }

        file = file.toRealPath();
        if (!file.startsWith(root)) {
            return null;
        }

        return Files.newInputStream(file);
    }

    private static InputStream openStreamProduction(String path) throws IOException {
        if (path.startsWith("web/")) {
            String dir = System.getProperty("cheatutils.web.dir");
            if (dir != null) {
                return openLocal(Paths.get(dir), path.substring(4));
            }
        }

        return openResource(path);
    }

    private static InputStream openStreamDevelopment(String path) throws IOException {
        return openLocal(Paths.get(System.getProperty("user.dir"), "../src/main/resources"), path);
    }

    private static InputStream openResource(String path) {
        return ResourceHelper.class.getClassLoader().getResourceAsStream(path);
    }

    private static boolean isSafe(String path) {
        if (path.startsWith("/") || path.contains("\\") || path.contains(":")) {
            return false;
        }
        for (String part : path.split("/")) {
            if (part.equals("..") || part.equals(".") || part.indexOf('\0') >= 0) {
                return false;
            }
        }
        return true;
    }
}