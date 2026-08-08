package com.zergatul.cheatutils.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public class ModMetadata {

    public static Map<String, String> getCommitsSafe() {
        try {
            return getCommits();
        } catch (IOException e) {
            return Map.of();
        }
    }

    public static Map<String, String> getCommits() throws IOException {
        InputStream stream = ModMetadata.class.getClassLoader().getResourceAsStream("commits.json");
        try (stream) {
            if (stream == null) {
                return Map.of();
            }

            byte[] bytes = org.apache.commons.io.IOUtils.toByteArray(stream);
            String json = new String(bytes, StandardCharsets.US_ASCII);
            Map<String, String> commits = new Gson().fromJson(json, new TypeToken<Map<String, String>>() {}.getType());
            return commits == null ? Map.of() : commits;
        }
    }

    private ModMetadata() {}
}