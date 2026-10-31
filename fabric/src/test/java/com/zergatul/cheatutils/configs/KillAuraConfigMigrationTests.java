package com.zergatul.cheatutils.configs;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.configs.modules.KillAuraClassPriorityEntry;
import com.zergatul.cheatutils.configs.modules.KillAuraConfig;
import com.zergatul.cheatutils.configs.modules.KillAuraPredefinedPriority;
import com.zergatul.cheatutils.configs.modules.KillAuraPredefinedPriorityEntry;
import net.minecraft.SharedConstants;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.entity.projectile.ShulkerBullet;
import net.minecraft.world.entity.projectile.hurtingprojectile.Fireball;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

public class KillAuraConfigMigrationTests {

    @BeforeAll
    public static void bootstrap() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void convertsLegacyPrioritiesInOrderAndPreservesSettings() throws Exception {
        JsonObject root = migrate("""
                {"killAuraConfig":{"enabled":true,"maxRange":9,"priorities":[
                    {"name":"Players","enabled":false},
                    {"name":"Enemies w/o Piglins","enabled":true},
                    {"name":"Shulker Bullets","enabled":true},
                    {"name":"Fireballs","description":"Ghast projectiles.","enabled":false},
                    {"name":"Enemies","enabled":false}
                ],"customEntries":[]}}
                """);

        JsonObject json = root.getAsJsonObject("killAuraConfig");
        assertFalse(json.has("customEntries"));
        KillAuraConfig config = ConfigStore.instance.gson.fromJson(json, KillAuraConfig.class);
        assertTrue(config.enabled());
        assertEquals(9, config.maxRange());
        assertEquals(5, config.priorities().size());
        assertEquals(new KillAuraClassPriorityEntry("Players", null, RemotePlayer.class, false), config.priorities().get(0));
        assertEquals(new KillAuraPredefinedPriorityEntry(KillAuraPredefinedPriority.ENEMIES_WO_PIGLINS, true), config.priorities().get(1));
        assertEquals(new KillAuraClassPriorityEntry("Shulker Bullets", null, ShulkerBullet.class, true), config.priorities().get(2));
        assertEquals(new KillAuraClassPriorityEntry("Fireballs", "Ghast projectiles.", Fireball.class, false), config.priorities().get(3));
        assertEquals(new KillAuraPredefinedPriorityEntry(KillAuraPredefinedPriority.ENEMIES, false), config.priorities().get(4));
    }

    @Test
    public void appendsOnlyMissingCustomClassesDisabled() throws Exception {
        JsonObject root = migrate("""
                {"killAuraConfig":{"priorities":[
                    {"name":"Active","className":"java.lang.String","description":"Keep this","enabled":true},
                    {"name":"Inactive","className":"java.lang.Integer","enabled":false}
                ],"customEntries":[
                    {"name":"Different name","className":"java.lang.String","enabled":false},
                    {"name":"Inactive","className":"java.lang.Integer","enabled":true},
                    {"name":"Missing","description":"Saved entry","className":"java.lang.Double","enabled":true},
                    {"name":"Duplicate","className":"java.lang.Double","enabled":true},
                    {"name":"Last","className":"java.lang.Long","enabled":true}
                ]}}
                """);

        JsonArray entries = root.getAsJsonObject("killAuraConfig").getAsJsonArray("priorities");
        assertEquals(4, entries.size());
        KillAuraConfig config = ConfigStore.instance.gson.fromJson(root.get("killAuraConfig"), KillAuraConfig.class);
        assertEquals(new KillAuraClassPriorityEntry("Active", "Keep this", String.class, true), config.priorities().get(0));
        assertEquals(new KillAuraClassPriorityEntry("Inactive", null, Integer.class, false), config.priorities().get(1));
        assertEquals(new KillAuraClassPriorityEntry("Missing", "Saved entry", Double.class, false), config.priorities().get(2));
        assertEquals(new KillAuraClassPriorityEntry("Last", null, Long.class, false), config.priorities().get(3));
        assertEquals(root, migrate(root.deepCopy()));
    }

    @Test
    public void currentFormatIsUnchanged() throws Exception {
        JsonObject root = JsonParser.parseString("""
                {"killAuraConfig":{"priorities":[
                    {"type":"predefined","priority":"ENEMIES","enabled":true},
                    {"type":"class","name":"Custom","class":"java.lang.String","enabled":false}
                ]}}
                """).getAsJsonObject();
        assertEquals(root, migrate(root.deepCopy()));
        assertEquals(migrate(root.deepCopy()), migrate(migrate(root.deepCopy())));
    }

    @Test
    public void skipsMigrationWhenCustomEntriesIsAbsentOrNull() throws Exception {
        JsonObject root = JsonParser.parseString("""
                {"killAuraConfig":{"priorities":[{"name":"Enemies","enabled":true}]}}
                """).getAsJsonObject();
        assertEquals(root, migrate(root.deepCopy()));

        root.getAsJsonObject("killAuraConfig").add("customEntries", JsonNull.INSTANCE);
        assertEquals(root, migrate(root.deepCopy()));
    }

    @Test
    public void ignoresInvalidLegacyEntries() throws Exception {
        JsonObject root = migrate("""
                {"killAuraConfig":{"priorities":[null,"old entry",{}, {"name":"Unknown"}],"customEntries":[null,{}]}}
                """);
        assertTrue(root.getAsJsonObject("killAuraConfig").getAsJsonArray("priorities").isEmpty());
        assertEquals(2, ConfigStore.instance.gson.fromJson(root.get("killAuraConfig"), KillAuraConfig.class).priorities().size());
        assertEquals(JsonParser.parseString("{}"), migrate("{}"));
        assertEquals(JsonParser.parseString("{\"killAuraConfig\":null}"), migrate("{\"killAuraConfig\":null}"));
    }

    private JsonObject migrate(String json) throws Exception {
        return migrate(JsonParser.parseString(json).getAsJsonObject());
    }

    private JsonObject migrate(JsonObject root) throws Exception {
        Method method = ConfigStore.class.getDeclaredMethod("migration6", JsonElement.class);
        method.setAccessible(true);
        method.invoke(ConfigStore.instance, root);
        return root;
    }
}