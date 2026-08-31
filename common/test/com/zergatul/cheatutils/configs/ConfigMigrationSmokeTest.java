package com.zergatul.cheatutils.configs;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.common.Registries;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;

import java.awt.*;

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
        verifyStatusOverlaySource("overlay.add(\"valid\");");
        verifyStatusOverlaySource("int value = ;");
    }

    public static void verifyBlockAutomation() {
        verifyBlockAutomationSource("blockAutomation.useItem(\"minecraft:stone\");");
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

    public static void verifyAutoBucket() {
        Config oldConfig = ConfigStore.instance.gson.fromJson("""
                {
                  "autoBucketConfig": {
                    "enabled": true,
                    "useWaterBucket": true,
                    "useHoneyBlock": true,
                    "speedThreshold": 15,
                    "reachDistance": 4.5
                  }
                }
                """, Config.class);
        oldConfig.sanitize();
        if (!oldConfig.autoBucketConfig.enabled || !oldConfig.autoBucketConfig.useWaterBucket ||
                !oldConfig.autoBucketConfig.useHoneyBlock || oldConfig.autoBucketConfig.usePowderSnowBucket ||
                oldConfig.autoBucketConfig.speedThreshold != 15 || oldConfig.autoBucketConfig.reachDistance != 4.5) {
            throw new IllegalStateException("Old Auto Bucket config was not preserved after adding powder snow support.");
        }

        Config sanitized = ConfigStore.instance.gson.fromJson("""
                {
                  "autoBucketConfig": {
                    "usePowderSnowBucket": true,
                    "speedThreshold": 0,
                    "reachDistance": 100,
                    "hotbarSlot": 20
                  }
                }
                """, Config.class);
        sanitized.sanitize();
        if (!sanitized.autoBucketConfig.usePowderSnowBucket || sanitized.autoBucketConfig.speedThreshold != 0.1 ||
                sanitized.autoBucketConfig.reachDistance != 20 || sanitized.autoBucketConfig.hotbarSlot != 8) {
            throw new IllegalStateException("Maintained Auto Bucket config sanitation failed.");
        }
    }

    public static void verifySchematica() {
        JsonElement tree = JsonParser.parseString("""
                {
                  "schematicaConfig": {
                    "enabled": true,
                    "showMissingBlockGhosts": false,
                    "missingBlockGhostsMaxDistance": 42,
                    "autoBuild": true,
                    "maxRange": 8
                  }
                }
                """);

        ConfigStore.migrateConfigTree(tree);
        Config config = ConfigStore.instance.gson.fromJson(tree, Config.class);
        config.sanitize();

        JsonElement migrated = tree.getAsJsonObject().getAsJsonObject("schematicaConfig");
        if (migrated.getAsJsonObject().has("showMissingBlockGhosts") ||
                migrated.getAsJsonObject().has("missingBlockGhostsMaxDistance") ||
                !migrated.getAsJsonObject().has("renderBlocks")) {
            throw new IllegalStateException("Old Schematica ghost rendering fields were not migrated.");
        }

        SchematicaConfig schematica = config.schematicaConfig;
        if (!schematica.enabled || schematica.renderBlocks || !schematica.shadeBlocks ||
                !schematica.autoBuild || schematica.maxRange != 8 || schematica.placementRate != 1 ||
                schematica.create == null) {
            throw new IllegalStateException("Old Schematica behavior changed during config migration.");
        }
    }

    public static void verifyBlockEspGroups() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        JsonElement tree = JsonParser.parseString("""
                {
                  "blocks": {
                    "configs": [
                      {
                        "block": "minecraft:stone",
                        "enabled": true,
                        "drawTracers": true,
                        "tracerColor": -65536,
                        "drawOutline": true,
                        "outlineColor": -16711936,
                        "maxDistance": 123,
                        "tracerMaxDistance": 45,
                        "outlineMaxDistance": 67
                      },
                      {
                        "block": "minecraft:dirt",
                        "enabled": false,
                        "drawTracers": false,
                        "tracerColor": -1,
                        "drawOutline": false,
                        "outlineColor": -1,
                        "maxDistance": 80
                      }
                    ]
                  }
                }
                """);

        ConfigStore.migrateConfigTree(tree);
        Config config = ConfigStore.instance.gson.fromJson(tree, Config.class);
        config.sanitize();

        JsonElement firstTree = tree.getAsJsonObject()
                .getAsJsonObject("blocks")
                .getAsJsonArray("configs")
                .get(0);
        if (firstTree.getAsJsonObject().has("block") || firstTree.getAsJsonObject().has("drawOutline") ||
                firstTree.getAsJsonObject().has("outlineColor") || firstTree.getAsJsonObject().has("outlineMaxDistance") ||
                !firstTree.getAsJsonObject().has("blocks") || !firstTree.getAsJsonObject().has("drawBoundingBox") ||
                !firstTree.getAsJsonObject().has("boundingBoxColor") || !firstTree.getAsJsonObject().has("boundingBoxMaxDistance")) {
            throw new IllegalStateException("Old Block ESP fields were not migrated to the grouped config shape.");
        }

        if (config.blocks.getBlockConfigs().size() != 2) {
            throw new IllegalStateException("Old Block ESP entries were not preserved as singleton groups.");
        }

        BlockEspConfig stone = config.blocks.getBlockConfigs().get(0);
        if (stone.blocks.size() != 1 ||
                !"minecraft:stone".equals(Registries.BLOCKS.getKey(stone.blocks.get(0)).toString()) ||
                !stone.enabled || !stone.drawTracers || !stone.drawBoundingBox || stone.drawOverlay ||
                stone.tracerWidth != 1 || stone.boundingBoxWidth != 1 ||
                stone.tracerColor.getRGB() != Color.RED.getRGB() || stone.boundingBoxColor.getRGB() != Color.GREEN.getRGB() ||
                stone.overlayColor.getRGB() != new Color(0x80FFFFFF, true).getRGB() ||
                stone.maxDistance != 123 || stone.tracerMaxDistance != 45 || stone.boundingBoxMaxDistance != 67) {
            throw new IllegalStateException("Old Block ESP appearance changed during grouped config migration.");
        }

        JsonElement savedTree = ConfigStore.instance.gson.toJsonTree(config.blocks);
        JsonElement savedConfig = savedTree.getAsJsonObject().getAsJsonArray("configs").get(0);
        if (savedConfig.getAsJsonObject().has("block") || savedConfig.getAsJsonObject().has("drawOutline") ||
                savedConfig.getAsJsonObject().has("outlineColor") || savedConfig.getAsJsonObject().has("outlineMaxDistance") ||
                !savedConfig.getAsJsonObject().has("blocks") || !savedConfig.getAsJsonObject().has("drawBoundingBox") ||
                !savedConfig.getAsJsonObject().has("boundingBoxColor") || !savedConfig.getAsJsonObject().has("boundingBoxMaxDistance")) {
            throw new IllegalStateException("Grouped Block ESP config was not saved using maintained field names.");
        }

        Config duplicates = ConfigStore.instance.gson.fromJson("""
                {
                  "blocks": {
                    "configs": [
                      { "blocks": [ "minecraft:stone" ], "maxDistance": 100 },
                      { "blocks": [ "minecraft:stone", "minecraft:dirt" ], "maxDistance": 100 }
                    ]
                  }
                }
                """, Config.class);
        duplicates.sanitize();
        if (duplicates.blocks.getBlockConfigs().size() != 2 ||
                duplicates.blocks.getBlockConfigs().get(0).blocks.size() != 1 ||
                duplicates.blocks.getBlockConfigs().get(1).blocks.size() != 1 ||
                !"minecraft:dirt".equals(Registries.BLOCKS.getKey(
                        duplicates.blocks.getBlockConfigs().get(1).blocks.get(0)).toString())) {
            throw new IllegalStateException("One-group-per-block sanitation is not deterministic.");
        }
    }

    public static void verifyEntityEsp() {
        JsonElement tree = JsonParser.parseString("""
                {
                  "entities": {
                    "configs": [
                      {
                        "clazz": "java.lang.Object",
                        "enabled": true,
                        "drawTracers": true,
                        "tracerColor": -65536,
                        "drawOutline": true,
                        "outlineColor": -16711936,
                        "outlineMaxDistance": 67,
                        "glow": true,
                        "glowColor": -16776961,
                        "glowMaxDistance": 89,
                        "maxDistance": 123,
                        "tracerMaxDistance": 45,
                        "drawTitles": true,
                        "showDefaultNames": true,
                        "showHp": true,
                        "showEquippedItems": true,
                        "showOwner": true
                      },
                      {
                        "clazz": "missing.mod.RemovedEntity",
                        "maxDistance": 100
                      }
                    ]
                  }
                }
                """);

        ConfigStore.migrateConfigTree(tree);
        ConfigStore.migrateConfigTree(tree);
        Config config = ConfigStore.instance.gson.fromJson(tree, Config.class);
        config.sanitize();

        JsonElement migrated = tree.getAsJsonObject()
                .getAsJsonObject("entities")
                .getAsJsonArray("configs")
                .get(0);
        if (migrated.getAsJsonObject().has("glow") || migrated.getAsJsonObject().has("glowColor") ||
                migrated.getAsJsonObject().has("glowMaxDistance") ||
                !migrated.getAsJsonObject().has("drawBoundingBox") ||
                !migrated.getAsJsonObject().has("boundingBoxColor") ||
                !migrated.getAsJsonObject().has("boundingBoxMaxDistance") ||
                !migrated.getAsJsonObject().has("drawOutline") ||
                !migrated.getAsJsonObject().has("outlineColor") ||
                !migrated.getAsJsonObject().has("outlineMaxDistance")) {
            throw new IllegalStateException("Old Entity ESP fields were not migrated to the maintained config shape.");
        }

        if (config.entities.configs.size() != 1) {
            throw new IllegalStateException("Missing modded Entity ESP classes were not discarded safely.");
        }

        EntityEspConfig entity = config.entities.configs.get(0);
        if (entity.clazz != Object.class ||
                !entity.enabled || !entity.drawTracers || !entity.drawBoundingBox || !entity.drawOutline || entity.drawOverlay ||
                entity.tracerWidth != 1 || entity.boundingBoxWidth != 1 || entity.outlineMethod != 0 || entity.useRawNames ||
                entity.tracerColor.getRGB() != Color.RED.getRGB() ||
                entity.boundingBoxColor.getRGB() != Color.GREEN.getRGB() ||
                entity.outlineColor.getRGB() != Color.BLUE.getRGB() ||
                entity.overlayColor.getRGB() != new Color(0x80FFFFFF, true).getRGB() ||
                entity.maxDistance != 123 || entity.tracerMaxDistance != 45 ||
                entity.boundingBoxMaxDistance != 67 || entity.outlineMaxDistance != 89 ||
                !entity.drawTitles || !entity.showDefaultNames || !entity.showHp ||
                !entity.showEquippedItems || !entity.showOwner) {
            throw new IllegalStateException("Old Entity ESP behavior changed during config migration.");
        }

        JsonElement saved = ConfigStore.instance.gson.toJsonTree(entity);
        if (saved.getAsJsonObject().has("glow") || saved.getAsJsonObject().has("glowColor") ||
                saved.getAsJsonObject().has("glowMaxDistance") ||
                !saved.getAsJsonObject().has("drawBoundingBox") ||
                !saved.getAsJsonObject().has("drawOutline") ||
                !saved.getAsJsonObject().has("drawOverlay") ||
                !saved.getAsJsonObject().has("useRawNames")) {
            throw new IllegalStateException("Entity ESP config was not saved using maintained field names.");
        }

        EntityEspConfig sanitized = ConfigStore.instance.gson.fromJson("""
                {
                  "clazz": "java.lang.Object",
                  "tracerWidth": 0,
                  "boundingBoxWidth": 101,
                  "maxDistance": -1,
                  "outlineMaxDistance": "NaN",
                  "outlineMethod": 10
                }
                """, EntityEspConfig.class);
        sanitized.sanitize();
        if (sanitized.tracerWidth != 0.5 || sanitized.boundingBoxWidth != 100 ||
                sanitized.maxDistance != 1000 || sanitized.outlineMaxDistance != null ||
                sanitized.outlineMethod != 0 || sanitized.tracerColor == null ||
                sanitized.boundingBoxColor == null || sanitized.outlineColor == null || sanitized.overlayColor == null) {
            throw new IllegalStateException("Maintained Entity ESP config sanitation failed.");
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
