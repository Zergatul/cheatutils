package com.zergatul.cheatutils.font;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SystemFonts {

    private static final Logger logger = LogManager.getLogger(SystemFonts.class);
    private static CompletableFuture<List<SystemFontInfo>> future;

    public static void initAsync() {
        if (future == null) {
            future = CompletableFuture.supplyAsync(SystemFonts::scanForFonts);
        }
    }

    public static List<SystemFontInfo> getFontsBlocking() {
        return future.join();
    }

    private static List<SystemFontInfo> scanForFonts() {
        logger.info("Started scanning");
        long start = System.nanoTime();
        try {
            List<SystemFontInfo> list = new ArrayList<>();
            for (Path dir : getFontDirectories()) {
                if (!Files.isDirectory(dir)) {
                    continue;
                }
                try {
                    try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir, "*.{ttf,otf,ttc}")) {
                        for (Path file : stream) {
                            try {
                                list.addAll(SystemFontInfo.fromFile(file.toAbsolutePath().toString()));
                            } catch (IOException ex) {
                                logger.warn(String.format("Cannot parse font file %s.", file.toAbsolutePath()), ex);
                            }
                        }
                    }
                } catch (IOException ex) {
                    logger.warn(String.format("Error while enumerating font files in %s.", dir), ex);
                }
            }
            list.sort(Comparator.comparing((SystemFontInfo info) -> info.name).thenComparing(info -> info.path));
            return list;
        } finally {
            logger.info("Finished scanning in {} ms", (System.nanoTime() - start) / 1_000_000);
        }
    }

    private static List<Path> getFontDirectories() {
        String os = System.getProperty("os.name").toLowerCase();
        Path mcFonts = Paths.get(System.getProperty("user.dir"), "fonts");

        if (os.contains("win")) {
            return List.of(
                    Paths.get(System.getenv("WINDIR"), "Fonts"),
                    mcFonts);
        } else if (os.contains("mac")) {
            return List.of(
                    Paths.get("/System/Library/Fonts"),
                    Paths.get("/Library/Fonts"),
                    Paths.get(System.getProperty("user.home"), "Library/Fonts"),
                    mcFonts);
        } else {
            return List.of(
                    Paths.get("/usr/share/fonts"),
                    Paths.get("/usr/local/share/fonts"),
                    Paths.get(System.getProperty("user.home"), ".fonts"),
                    mcFonts);
        }
    }
}