package com.zergatul.cheatutils.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.zergatul.cheatutils.configs.adapters.ImmutableConfigAdapterFactory;
import com.zergatul.cheatutils.configs.modules.EntityTitleConfig;
import com.zergatul.cheatutils.configs.modules.StepUpConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class SettingsSerializerTests {

    private final Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ImmutableConfigAdapterFactory()).create();
    private final SettingsRegistry registry = new SettingsRegistry(new Object(), () -> {});
    private final SettingHandle<EntityTitleConfig> title = registry.register(EntityTitleConfig::defaults);
    private final SettingHandle<StepUpConfig> stepUp = registry.register(StepUpConfig::defaults);
    private final SettingsSerializer serializer = new SettingsSerializer(gson, registry);

    @Test
    public void savedProfileLoadsBackWithTheSameValues() {
        title.update(config -> config.withItemScale(3));
        stepUp.update(config -> config.withEnabled(true).withHeight(2));

        JsonObject json = serializer.toJson(registry.getSnapshot());
        SettingsSnapshot loaded = serializer.fromJson(JsonParser.parseString(gson.toJson(json)).getAsJsonObject());

        assertEquals(List.of("entityTitleConfig", "stepUpConfig"), List.copyOf(json.keySet()));
        registry.replace(SettingNotificationReason.PROFILE_LOAD, loaded);
        assertEquals(3, title.get().itemScale());
        assertEquals(new StepUpConfig(true, 2), stepUp.get());
    }

    @Test
    public void brokenSettingFallsBackToDefaultsAlone() {
        JsonObject json = JsonParser.parseString("""
                {
                    "entityTitleConfig": { "itemScale": "not a number" },
                    "stepUpConfig": { "enabled": true, "height": 2 }
                }
                """).getAsJsonObject();

        registry.replace(SettingNotificationReason.PROFILE_LOAD, serializer.fromJson(json));

        assertEquals(EntityTitleConfig.defaults(), title.get());
        assertEquals(new StepUpConfig(true, 2), stepUp.get());
    }

    @Test
    public void missingAndUnknownSettingsAreIgnored() {
        JsonObject json = JsonParser.parseString("""
                { "removedModuleConfig": { "enabled": true }, "stepUpConfig": null }
                """).getAsJsonObject();

        registry.replace(SettingNotificationReason.PROFILE_LOAD, serializer.fromJson(json));

        assertEquals(EntityTitleConfig.defaults(), title.get());
        assertEquals(StepUpConfig.defaults(), stepUp.get());
    }

    @Test
    public void irregularLegacyKeyIsMigratedAndLoaded() {
        JsonObject json = JsonParser.parseString("""
                { "stepUp": { "enabled": true, "height": 2 } }
                """).getAsJsonObject();

        Migrations.run(json);
        registry.replace(SettingNotificationReason.PROFILE_LOAD, serializer.fromJson(json));

        assertFalse(json.has("stepUp"));
        assertEquals(new StepUpConfig(true, 2), stepUp.get());
    }

    @Test
    public void migrationRenamesAllIrregularKeys() {
        JsonObject json = new JsonObject();
        List<String> legacy = List.of("aimAssist", "autoTool", "blockEntityDistance", "logoutSpots", "monacoEditor", "stepUp");
        for (String key : legacy) {
            json.add(key, new JsonObject());
        }

        Migrations.run(json);

        assertEquals(legacy.stream().map(key -> key + "Config").sorted().toList(), json.keySet().stream().sorted().toList());
    }
}
