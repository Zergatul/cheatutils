package com.zergatul.cheatutils.mcp.tool;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.mcp.utility.JsonObjectBuilder;
import com.zergatul.cheatutils.utils.ResourceHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ListScriptExamplesTool implements McpTool {

    private final JsonObject inputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {},
                "additionalProperties": false
            }
            """).getAsJsonObject();

    private final JsonObject outputSchema = JsonParser.parseString("""
            {
                "type": "object",
                "properties": {
                    "items": {
                        "type": "array",
                        "items": {
                            "type": "object",
                            "properties": {
                                "uri": {
                                    "type": "string",
                                    "description": "Resource URI for the example source"
                                },
                                "description": {
                                    "type": "string",
                                    "description": "Summary extracted from the example header"
                                }
                            },
                            "required": ["uri", "description"],
                            "additionalProperties": false
                        }
                    }
                },
                "required": ["items"],
                "additionalProperties": false
            }
            """).getAsJsonObject();

    @Override
    public String getName() {
        return "list_script_examples";
    }

    @Override
    public String getTitle() {
        return "List Script Examples";
    }

    @Override
    public String getDescription() {
        return "List example script resource URIs.";
    }

    @Override
    public JsonObject getInputSchema() {
        return inputSchema;
    }

    @Override
    public JsonObject getOutputSchema() {
        return outputSchema;
    }

    @Override
    public McpToolCallResult invoke(JsonElement arguments) throws IOException {
        JsonArray items = new JsonArray();
        for (String resource : getExampleResources()) {
            if (resource.isEmpty()) {
                continue;
            }

            items.add(new JsonObjectBuilder()
                    .withProperty("uri", extractUri(resource))
                    .withProperty("description", extractDescription(resource))
                    .build());
        }

        JsonObject result = new JsonObjectBuilder()
                .withProperty("items", items)
                .build();
        return McpToolCallResult.success(result);
    }

    private List<String> getExampleResources() throws IOException {
        InputStream stream = ResourceHelper.get("llm/script-examples/index");
        if (stream == null) {
            throw new IllegalStateException();
        }

        try (stream) {
            return new InputStreamReader(stream, StandardCharsets.UTF_8).readAllLines();
        }
    }

    private String extractUri(String path) {
        String prefix = "llm/script-examples/";
        if (!path.startsWith(prefix)) {
            throw new IllegalStateException();
        }

        path = path.substring(prefix.length());

        if (!path.endsWith(".cs")) {
            throw new IllegalStateException();
        }

        path = path.substring(0, path.length() - 3);

        return "cheatutils://docs/examples/" + path;
    }

    private String extractDescription(String path) throws IOException {
        InputStream stream = ResourceHelper.get(path);
        if (stream == null) {
            throw new IllegalStateException();
        }

        List<String> lines;
        try (stream) {
            lines = new InputStreamReader(stream, StandardCharsets.UTF_8).readAllLines();
        }

        if (lines.isEmpty()) {
            return "";
        }

        if (!lines.getFirst().equals("/*")) {
            return "";
        }

        int index = lines.indexOf("*/");
        if (index < 0) {
            return "";
        }

        StringBuilder description = new StringBuilder();
        for (int i = 1; i < index; i++) {
            String line = lines.get(i);
            if (line.startsWith("* ")) {
                line = line.substring(2);
            }
            description.append(line).append('\n');
        }
        description.deleteCharAt(description.length() - 1);

        return description.toString();
    }
}