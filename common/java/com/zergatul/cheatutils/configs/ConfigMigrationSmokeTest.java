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

    public static void verifyEventsScripting() {
        verifyOldGameTickSourceDiscarded("int value = 1;");
        verifyOldGameTickSourceDiscarded("int value = ;");
        verifyEventsScriptingSource("events.onTickEnd(() => {});");
        verifyEventsScriptingSource("int value = ;");
    }

    public static void verifyAutoDisconnectRemoved() {
        verifyAutoDisconnectSourceDiscarded("player.disconnect(\"\", \"old\");");
        verifyAutoDisconnectSourceDiscarded("int value = ;");
    }

    public static void verifyCoreConfig() {
        Config defaults = ConfigStore.instance.gson.fromJson("{}", Config.class);
        defaults.sanitize();
        if (defaults.coreConfig.port != 5005 || defaults.coreConfig.advancedScripting) {
            throw new IllegalStateException("Core config defaults changed unexpectedly.");
        }

        Config minimum = ConfigStore.instance.gson.fromJson("""
                {
                  "coreConfig": {
                    "port": 0,
                    "advancedScripting": true
                  }
                }
                """, Config.class);
        minimum.sanitize();
        if (minimum.coreConfig.port != 1 || !minimum.coreConfig.advancedScripting) {
            throw new IllegalStateException("Core config minimum port sanitation failed.");
        }

        Config maximum = ConfigStore.instance.gson.fromJson("""
                {
                  "coreConfig": {
                    "port": 70000
                  }
                }
                """, Config.class);
        maximum.sanitize();
        if (maximum.coreConfig.port != 65535) {
            throw new IllegalStateException("Core config maximum port sanitation failed.");
        }
    }

    public static void verifyAutoAttack() {
        JsonElement tree = JsonParser.parseString("""
                {
                  "autoAttackConfig": {
                    "enabled": true,
                    "extraTicks": 3.6
                  }
                }
                """);

        ConfigStore.migrateConfigTree(tree);
        Config config = ConfigStore.instance.gson.fromJson(tree, Config.class);
        config.sanitize();

        JsonElement migrated = tree.getAsJsonObject().getAsJsonObject("autoAttackConfig").get("extraTicks");
        if (migrated != null || !config.autoAttackConfig.enabled ||
                config.autoAttackConfig.extraTicksMin != 4 || config.autoAttackConfig.extraTicksMax != 4 ||
                config.autoAttackConfig.maxRange != 2.5) {
            throw new IllegalStateException("Old Auto Attack delay was not migrated to the maintained config shape.");
        }

        Config sanitized = ConfigStore.instance.gson.fromJson("""
                {
                  "autoAttackConfig": {
                    "limitRange": true,
                    "maxRange": 20,
                    "extraTicksMin": -20,
                    "extraTicksMax": 20
                  }
                }
                """, Config.class);
        sanitized.sanitize();
        if (!sanitized.autoAttackConfig.limitRange || sanitized.autoAttackConfig.maxRange != 10 ||
                sanitized.autoAttackConfig.extraTicksMin != -10 || sanitized.autoAttackConfig.extraTicksMax != 10) {
            throw new IllegalStateException("Maintained Auto Attack config sanitation failed.");
        }
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

    private static void verifyOldGameTickSourceDiscarded(String code) {
        JsonElement tree = JsonParser.parseString("""
                {
                  "gameTickScriptingConfig": {
                    "enabled": true,
                    "code": %s
                  }
                }
                """.formatted(ConfigStore.instance.gson.toJson(code)));

        ConfigStore.migrateConfigTree(tree);
        Config config = ConfigStore.instance.gson.fromJson(tree, Config.class);

        if (tree.getAsJsonObject().has("gameTickScriptingConfig")) {
            throw new IllegalStateException("Old Game Tick Scripting config was not discarded.");
        }
        if (config.eventsScriptingConfig.enabled || config.eventsScriptingConfig.code != null) {
            throw new IllegalStateException("Old Game Tick source was unexpectedly migrated to Events Scripting.");
        }
    }

    private static void verifyEventsScriptingSource(String code) {
        JsonElement tree = JsonParser.parseString("""
                {
                  "eventsScriptingConfig": {
                    "enabled": true,
                    "code": %s
                  }
                }
                """.formatted(ConfigStore.instance.gson.toJson(code)));

        ConfigStore.migrateConfigTree(tree);
        Config config = ConfigStore.instance.gson.fromJson(tree, Config.class);

        if (!config.eventsScriptingConfig.enabled || !code.equals(config.eventsScriptingConfig.code)) {
            throw new IllegalStateException("Events Scripting config or source changed during config-tree loading.");
        }
    }

    private static void verifyAutoDisconnectSourceDiscarded(String code) {
        JsonElement tree = JsonParser.parseString("""
                {
                  "autoDisconnectConfig": {
                    "enabled": true,
                    "code": %s
                  }
                }
                """.formatted(ConfigStore.instance.gson.toJson(code)));

        ConfigStore.migrateConfigTree(tree);
        ConfigStore.instance.gson.fromJson(tree, Config.class);

        if (tree.getAsJsonObject().has("autoDisconnectConfig")) {
            throw new IllegalStateException("Old Auto Disconnect config or source was not discarded.");
        }
    }
}
