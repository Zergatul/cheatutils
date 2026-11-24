package com.zergatul.cheatutils.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.collections.ImmutableList;
import com.zergatul.cheatutils.configs.adapters.ImmutableConfigAdapterFactory;
import com.zergatul.cheatutils.configs.adapters.ImmutableListSerializer;
import com.zergatul.cheatutils.configs.modules.KeyBindingEntry;
import com.zergatul.cheatutils.configs.modules.KeyBindingsConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class KeyBindingsConfigMigrationTests {

    private final Gson gson = new GsonBuilder()
            .registerTypeAdapterFactory(new ImmutableConfigAdapterFactory())
            .registerTypeAdapter(ImmutableList.class, new ImmutableListSerializer())
            .create();

    @Test
    public void preservesScriptsInOrderAndTheirAssignments() {
        JsonObject root = JsonParser.parseString("""
                {"keyBindingScriptsConfig":{"scripts":[
                    {"name":"Last key","code":"esp.toggle();"},
                    {"name":"Unassigned","code":"invalid code kept too"},
                    {"name":"","code":"freeCam.toggle();"}
                ]},"keyBindingsConfig":{"bindings":[]}}
                """).getAsJsonObject();
        JsonArray bindings = root.getAsJsonObject("keyBindingsConfig").getAsJsonArray("bindings");
        for (int i = 0; i < KeyBindingEntry.KEYS_COUNT; i++) {
            bindings.add(JsonNull.INSTANCE);
        }
        bindings.set(0, gson.toJsonTree(""));
        bindings.set(KeyBindingEntry.KEYS_COUNT - 1, gson.toJsonTree("Last key"));

        Migrations.run(root);

        assertFalse(root.has("keyBindingScriptsConfig"));
        assertFalse(root.getAsJsonObject("keyBindingsConfig").has("bindings"));
        assertEquals(ImmutableList.from(
                new KeyBindingEntry("Last key", "esp.toggle();", 29),
                new KeyBindingEntry("Unassigned", "invalid code kept too", -1),
                new KeyBindingEntry("", "freeCam.toggle();", 0)), read(root).entries());
        assertEquals(root, migrate(root.deepCopy()));
    }

    @Test
    public void migratesTheOlderScriptsConfigName() {
        JsonObject root = migrate(JsonParser.parseString("""
                {"scriptsConfig":{"scripts":[
                    {"name":"Toggle ESP","code":"esp.toggle();"},
                    {"name":"Toggle FreeCam","code":"freeCam.toggle();"}
                ]},"keyBindingsConfig":{"bindings":["Toggle ESP","Toggle FreeCam"]}}
                """).getAsJsonObject());

        assertFalse(root.has("scriptsConfig"));
        assertFalse(root.has("keyBindingScriptsConfig"));
        assertEquals(KeyBindingsConfig.defaults(), read(root));
    }

    @Test
    public void preservesAnEmptyScriptList() {
        JsonObject root = migrate(JsonParser.parseString("""
                {"keyBindingScriptsConfig":{"scripts":[]},"keyBindingsConfig":{"bindings":[null]}}
                """).getAsJsonObject());

        assertTrue(read(root).entries().isEmpty());
    }

    @Test
    public void currentFormatAndAbsentConfigAreUnchanged() {
        JsonObject root = JsonParser.parseString("""
                {"keyBindingsConfig":{"entries":[{"name":"Current","code":"esp.toggle();","keyIndex":4}]}}
                """).getAsJsonObject();
        assertEquals(root, migrate(root.deepCopy()));
        assertEquals(new JsonObject(), migrate(new JsonObject()));
    }

    @Test
    public void guardsMalformedLegacyValues() {
        for (String json : new String[] {
                "null", "[]", "true",
                "{\"keyBindingScriptsConfig\":null}",
                "{\"keyBindingScriptsConfig\":[]}",
                "{\"keyBindingScriptsConfig\":{}}",
                "{\"keyBindingScriptsConfig\":{\"scripts\":{}}}"
        }) {
            assertDoesNotThrow(() -> Migrations.run(JsonParser.parseString(json)));
        }

        for (String bindings : new String[] {"null", "[]", "{}", "{\"bindings\":true}", "{\"bindings\":[{},null,4]}"}) {
            JsonObject root = JsonParser.parseString("""
                    {"keyBindingScriptsConfig":{"scripts":[
                        null,[],{}, {"name":{},"code":"bad"}, {"name":"Bad","code":[]},
                        {"name":"Keep","code":"esp.toggle();"}
                    ]}}
                    """).getAsJsonObject();
            root.add("keyBindingsConfig", JsonParser.parseString(bindings));

            assertDoesNotThrow(() -> Migrations.run(root));
            assertEquals(ImmutableList.from(new KeyBindingEntry("Keep", "esp.toggle();", -1)), read(root).entries());
        }
    }

    private JsonObject migrate(JsonObject root) {
        Migrations.run(root);
        return root;
    }

    private KeyBindingsConfig read(JsonObject root) {
        return gson.fromJson(root.get("keyBindingsConfig"), KeyBindingsConfig.class);
    }
}