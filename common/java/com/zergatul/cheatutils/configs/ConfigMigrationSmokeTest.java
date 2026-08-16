package com.zergatul.cheatutils.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class ConfigMigrationSmokeTest {

    private ConfigMigrationSmokeTest() {}

    public static void verifyKeyBindingScripts() {
        String validCode = "esp.toggle();";
        String invalidCode = "int value = ;";
        JsonElement tree = JsonParser.parseString("""
                {
                  "scriptsConfig": {
                    "scripts": [
                      { "name": "valid", "code": "esp.toggle();" },
                      { "name": "invalid", "code": "int value = ;" }
                    ]
                  },
                  "keyBindingsConfig": {
                    "bindings": [ "valid", "invalid" ]
                  }
                }
                """);

        ConfigStore.migrateConfigTree(tree);
        Config config = ConfigStore.instance.gson.fromJson(tree, Config.class);
        config.sanitize();

        if (tree.getAsJsonObject().has("scriptsConfig") || !tree.getAsJsonObject().has("keyBindingScriptsConfig")) {
            throw new IllegalStateException("Old key-binding script config field was not migrated.");
        }
        if (config.keyBindingScriptsConfig.scripts.size() != 2 ||
                !validCode.equals(config.keyBindingScriptsConfig.scripts.get(0).code) ||
                !invalidCode.equals(config.keyBindingScriptsConfig.scripts.get(1).code)) {
            throw new IllegalStateException("Key-binding script names or sources changed during migration.");
        }
        if (config.keyBindingsConfig.bindings.length != KeyBindingsConfig.KeysCount ||
                !"valid".equals(config.keyBindingsConfig.bindings[0]) ||
                !"invalid".equals(config.keyBindingsConfig.bindings[1])) {
            throw new IllegalStateException("Key-binding assignments changed during migration.");
        }
    }
}
