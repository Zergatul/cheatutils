package com.zergatul.cheatutils.configs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

@NullMarked
public class Migrations {

    public static void run(JsonElement element) {
        migration1(element);
        migration2(element);
        migration3(element);
        migration4(element);
        migration5(element);
        migration6(element);
        migration7(element);
        migration8(element);
    }

    private static void migration1(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject root = element.getAsJsonObject();
        if (!root.has("blocks")) {
            return;
        }

        element = root.get("blocks");
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject blocks = element.getAsJsonObject();
        if (!blocks.has("configs")) {
            return;
        }

        element = blocks.get("configs");
        if (!element.isJsonArray()) {
            return;
        }

        JsonArray configs = element.getAsJsonArray();
        for (JsonElement item: configs) {
            if (!item.isJsonObject()) {
                continue;
            }

            JsonObject config = item.getAsJsonObject();
            if (config.has("block")) {
                JsonArray array = new JsonArray();
                array.add(config.get("block"));
                config.add("blocks", array);
                config.remove("block");
            }
        }
    }

    private static void migration2(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject root = element.getAsJsonObject();
        if (!root.has("scriptedBlockPlacerConfig")) {
            return;
        }

        JsonElement config = root.get("scriptedBlockPlacerConfig");
        root.remove("scriptedBlockPlacerConfig");
        root.add("blockAutomationConfig", config);
    }

    private static void migration3(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject root = element.getAsJsonObject();
        if (!root.has("fogConfig")) {
            return;
        }

        JsonElement config = root.get("fogConfig");
        if (!config.isJsonObject()) {
            return;
        }

        JsonObject fog = config.getAsJsonObject();
        if (fog.has("disableFog")) {
            JsonElement value = fog.remove("disableFog");
            fog.add("enabled", value);
        }
    }

    private static void migration4(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject root = element.getAsJsonObject();
        if (!root.has("scriptsConfig")) {
            return;
        }

        JsonElement config = root.get("scriptsConfig");
        if (!config.isJsonObject()) {
            return;
        }

        root.remove("scriptsConfig");
        root.add("keyBindingScriptsConfig", config);
    }

    private static void migration5(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject root = element.getAsJsonObject();
        migrateBlockEspConfigFields(root);
        migrateEntityEspConfigFields(root);
    }

    private static void migration6(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        JsonElement config = element.getAsJsonObject().get("killAuraConfig");
        if (config == null || !config.isJsonObject()) {
            return;
        }

        JsonObject killAura = config.getAsJsonObject();
        JsonElement customEntries = killAura.get("customEntries");
        if (customEntries == null || !customEntries.isJsonArray()) {
            return;
        }

        JsonElement oldPriorities = killAura.get("priorities");
        if (oldPriorities == null || !oldPriorities.isJsonArray()) {
            return;
        }

        JsonArray priorities = new JsonArray();
        Set<String> classes = new HashSet<>();
        for (JsonElement item : oldPriorities.getAsJsonArray()) {
            JsonObject entry = migrateKillAuraPriorityEntry(item);
            if (entry == null) {
                continue;
            }

            priorities.add(entry);
            if (entry.has("class")) {
                classes.add(entry.get("class").getAsString());
            }
        }

        for (JsonElement item : customEntries.getAsJsonArray()) {
            JsonObject entry = migrateKillAuraPriorityEntry(item);
            if (entry != null && entry.has("class") && classes.add(entry.get("class").getAsString())) {
                entry.addProperty("enabled", false);
                priorities.add(entry);
            }
        }

        killAura.add("priorities", priorities);
        killAura.remove("customEntries");
    }

    private static void migration7(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        if (!element.getAsJsonObject().has("entities")) {
            return;
        }

        JsonElement oldEntityEspConfig = element.getAsJsonObject().remove("entities");
        if (!oldEntityEspConfig.isJsonObject()) {
            return;
        }

        JsonObject newEntityEspConfig = new JsonObject();
        element.getAsJsonObject().add("entityEspConfig", newEntityEspConfig);

        if (!oldEntityEspConfig.getAsJsonObject().has("configs")) {
            return;
        }

        JsonElement oldEntries = oldEntityEspConfig.getAsJsonObject().get("configs");
        if (!oldEntries.isJsonArray()) {
            return;
        }

        JsonArray newEntries = new JsonArray();
        newEntityEspConfig.add("entries", newEntries);

        for (JsonElement entry : oldEntries.getAsJsonArray()) {
            if (!entry.isJsonObject()) {
                continue;
            }

            if (entry.getAsJsonObject().has("clazz")) {
                JsonElement value = entry.getAsJsonObject().remove("clazz");
                entry.getAsJsonObject().add("type", value);
            }

            newEntries.add(entry);
        }
    }

    private static void migration8(JsonElement element) {
        if (!element.isJsonObject()) {
            return;
        }

        if (!element.getAsJsonObject().has("blocks")) {
            return;
        }

        JsonElement oldBlockEspConfig = element.getAsJsonObject().remove("blocks");
        if (!oldBlockEspConfig.isJsonObject()) {
            return;
        }

        JsonObject newBlockEspConfig = new JsonObject();
        element.getAsJsonObject().add("blockEspConfig", newBlockEspConfig);

        if (!oldBlockEspConfig.getAsJsonObject().has("configs")) {
            return;
        }

        JsonElement oldEntries = oldBlockEspConfig.getAsJsonObject().get("configs");
        if (!oldEntries.isJsonArray()) {
            return;
        }

        JsonArray newEntries = new JsonArray();
        newBlockEspConfig.add("entries", newEntries);

        for (JsonElement entry : oldEntries.getAsJsonArray()) {
            if (!entry.isJsonObject()) {
                continue;
            }

            newEntries.add(entry);
        }
    }

    private static @Nullable JsonObject migrateKillAuraPriorityEntry(JsonElement element) {
        if (!element.isJsonObject()) {
            return null;
        }

        JsonObject oldEntry = element.getAsJsonObject();
        if (oldEntry.has("type")) {
            return oldEntry;
        }
        if (!oldEntry.has("name") || oldEntry.get("name").isJsonNull()) {
            return null;
        }

        String name = oldEntry.get("name").getAsString();
        String className = oldEntry.has("className") && !oldEntry.get("className").isJsonNull() ?
                oldEntry.get("className").getAsString() : null;
        String priority = null;
        if (className == null) {
            switch (name) {
                case "Enemies" -> priority = "ENEMIES";
                case "Enemies w/o Piglins" -> priority = "ENEMIES_WO_PIGLINS";
                case "Players" -> className = RemotePlayer.class.getCanonicalName();
                case "Shulker Bullets" -> className = ShulkerBullet.class.getCanonicalName();
                case "Fireballs" -> className = Fireball.class.getCanonicalName();
                default -> {
                    return null;
                }
            }
        }

        // The priority adapter requires the discriminator to be the first property.
        JsonObject entry = new JsonObject();
        if (className == null) {
            entry.addProperty("type", "predefined");
            entry.addProperty("priority", priority);
        } else {
            entry.addProperty("type", "class");
            entry.addProperty("name", name);
            JsonElement description = oldEntry.get("description");
            if (description != null && !description.isJsonNull()) {
                entry.add("description", description);
            }
            entry.addProperty("class", className);
        }
        entry.addProperty("enabled", oldEntry.has("enabled") && oldEntry.get("enabled").getAsBoolean());
        return entry;
    }

    private static void migrateBlockEspConfigFields(JsonObject root) {
        migrateEspConfigs(root, "blocks", config -> {
            renameField(config, "drawOutline", "drawBoundingBox");
            renameField(config, "outlineWidth", "boundingBoxWidth");
            renameField(config, "outlineColor", "boundingBoxColor");
            renameField(config, "outlineMaxDistance", "boundingBoxMaxDistance");
        });
    }

    private static void migrateEntityEspConfigFields(JsonObject root) {
        migrateEspConfigs(root, "entities", config -> {
            if (config.has("glow")) {
                // we need this check because 'drawOutline' field exists pre- and post-migration
                // and they have different meaning
                renameField(config, "drawOutline", "drawBoundingBox");
                renameField(config, "outlineWidth", "boundingBoxWidth");
                renameField(config, "outlineColor", "boundingBoxColor");
                renameField(config, "outlineMaxDistance", "boundingBoxMaxDistance");
            }

            renameField(config, "glow", "drawOutline");
            renameField(config, "glowColor", "outlineColor");
            renameField(config, "glowMaxDistance", "outlineMaxDistance");
        });
    }

    private static void migrateEspConfigs(JsonObject root, String key, Consumer<JsonObject> migration) {
        if (!root.has(key)) {
            return;
        }

        JsonElement element = root.get(key);
        if (!element.isJsonObject()) {
            return;
        }

        JsonObject group = element.getAsJsonObject();
        if (!group.has("configs")) {
            return;
        }

        element = group.get("configs");
        if (!element.isJsonArray()) {
            return;
        }

        for (JsonElement item : element.getAsJsonArray()) {
            if (!item.isJsonObject()) {
                continue;
            }

            JsonObject config = item.getAsJsonObject();
            migration.accept(config);
        }
    }

    private static void renameField(JsonObject object, String oldName, String newName) {
        JsonElement value = object.remove(oldName);
        if (value != null && !object.has(newName)) {
            object.add(newName, value);
        }
    }
}