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

    public static void verifyStatusOverlay() {
        verifyStatusOverlaySource("main.addText(\"valid\");");
        verifyStatusOverlaySource("int value = ;");
    }

    public static void verifyBlockAutomation() {
        verifyBlockAutomationSource("blockPlacer.setBlockId(\"minecraft:stone\");");
        verifyBlockAutomationSource("int value = ;");
    }

    public static void verifyVillagerRoller() {
        verifyVillagerRollerSource("if (villagerRoller.isBestPrice()) { villagerRoller.stop(); }");
        verifyVillagerRollerSource("int value = ;");
    }

    private static void verifyStatusOverlaySource(String code) {
        JsonElement tree = JsonParser.parseString("""
                {
                  "statusOverlayConfig": {
                    "enabled": true,
                    "code": %s
                  }
                }
                """.formatted(ConfigStore.instance.gson.toJson(code)));

        ConfigStore.migrateConfigTree(tree);
        Config config = ConfigStore.instance.gson.fromJson(tree, Config.class);
        config.sanitize();

        if (!config.statusOverlayConfig.enabled || !code.equals(config.statusOverlayConfig.code)) {
            throw new IllegalStateException("Status Overlay config or source changed during config-tree loading.");
        }
    }

    private static void verifyBlockAutomationSource(String code) {
        JsonElement tree = JsonParser.parseString("""
                {
                  "scriptedBlockPlacerConfig": {
                    "enabled": true,
                    "code": %s,
                    "debugMode": true,
                    "maxRange": 7.5,
                    "autoSelectSlots": [ 2, 4 ],
                    "attachToAir": true,
                    "useShift": true
                  }
                }
                """.formatted(ConfigStore.instance.gson.toJson(code)));

        ConfigStore.migrateConfigTree(tree);
        Config config = ConfigStore.instance.gson.fromJson(tree, Config.class);
        config.sanitize();

        if (tree.getAsJsonObject().has("scriptedBlockPlacerConfig") ||
                !tree.getAsJsonObject().has("blockAutomationConfig")) {
            throw new IllegalStateException("Old Scripted Block Placer config field was not migrated.");
        }

        BlockAutomationConfig block = config.blockAutomationConfig;
        if (!block.enabled || !code.equals(block.code) || !block.debugMode ||
                block.maxRange != 7.5 || !block.attachToAir || !block.useShift ||
                block.autoSelectSlots.length != 2 ||
                block.autoSelectSlots[0] != 2 || block.autoSelectSlots[1] != 4) {
            throw new IllegalStateException("Block Automation settings or source changed during migration.");
        }
    }

    private static void verifyVillagerRollerSource(String code) {
        JsonElement tree = JsonParser.parseString("""
                {
                  "villagerRollerConfig": {
                    "code": %s
                  }
                }
                """.formatted(ConfigStore.instance.gson.toJson(code)));

        ConfigStore.migrateConfigTree(tree);
        Config config = ConfigStore.instance.gson.fromJson(tree, Config.class);
        config.sanitize();

        if (!code.equals(config.villagerRollerConfig.code)) {
            throw new IllegalStateException("Villager Roller source changed during config-tree loading.");
        }
    }
}
