package com.zergatul.cheatutils.codegen;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MappingEnumerator {

    public static void enumerate(String path, BiConsumer<String, String> consumer) throws IOException {
        String mappings = Files.readString(Path.of(path));
        Pattern pattern = Pattern.compile("^c\\t[a-z$]+\\t(?<obf>[a-zA-Z/_0-9$]+)\\t(?<norm>[a-zA-Z/_0-9$]+)$", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(mappings);
        while (matcher.find()) {
            String obf = matcher.group("obf");
            String norm = matcher.group("norm");
            consumer.accept(obf, norm);
        }
    }
}