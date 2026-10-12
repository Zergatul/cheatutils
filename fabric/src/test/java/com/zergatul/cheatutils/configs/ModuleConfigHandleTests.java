package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.scripting.modules.ModuleApi;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ModuleConfigHandleTests {

    @Test
    public void replacementIsSanitizedAndInstalledBeforeNotificationAndSavedOnce() {
        Fixture fixture = new Fixture();
        LightLevelConfig replacement = new LightLevelConfig();
        replacement.enabled = true;
        replacement.maxDistance = 2000;
        replacement.showLocations = true;

        fixture.settings.set(replacement);

        assertSame(replacement, fixture.current);
        assertSame(replacement, fixture.settings.get());
        assertTrue(fixture.current.showLocations);
        assertEquals(List.of("changed:true:1000.0", "save"), fixture.calls);
    }

    @Test
    public void scriptEntryPointsShareApplicationAndIgnoreUnchangedEnabledState() {
        Fixture fixture = new Fixture();
        fixture.current.maxDistance = 0;
        fixture.current.showLightLevelValue = true;
        ModuleApi<LightLevelConfig> api = new ModuleApi<>() {
            @Override
            protected LightLevelConfig getConfig() {
                return fixture.settings.get();
            }

            @Override
            protected void updateEnabled(boolean value) {
                fixture.settings.setEnabled(value);
            }
        };

        api.enable();
        api.enable();
        api.setEnabled(true);
        assertTrue(api.isEnabled());
        api.disable();
        api.disable();
        api.setEnabled(false);
        assertFalse(api.isEnabled());
        api.toggle();
        api.toggle();
        api.setEnabled(true);

        assertTrue(api.isEnabled());
        assertTrue(fixture.current.showLightLevelValue);
        assertEquals(List.of(
                "changed:true:1.0", "save",
                "changed:false:1.0", "save",
                "changed:true:1.0", "save",
                "changed:false:1.0", "save",
                "changed:true:1.0", "save"), fixture.calls);
    }

    @Test
    public void profileReloadUsesTheNewConfigWithoutSavingAndLaterEditsTargetIt() {
        Fixture fixture = new Fixture();
        LightLevelConfig previous = fixture.current;
        LightLevelConfig loaded = new LightLevelConfig();
        loaded.maxDistance = -10;
        fixture.current = loaded;

        fixture.settings.reload();

        assertSame(loaded, fixture.settings.get());
        assertEquals(List.of("changed:false:1.0"), fixture.calls);

        fixture.settings.setEnabled(true);

        assertTrue(loaded.enabled);
        assertFalse(previous.enabled);
        assertEquals(List.of("changed:false:1.0", "changed:true:1.0", "save"), fixture.calls);
    }

    @Test
    public void reloadStillNotifiesWhenEnabledStateIsUnchanged() {
        Fixture fixture = new Fixture();

        fixture.settings.reload();
        fixture.settings.reload();

        assertEquals(List.of("changed:false:20.0", "changed:false:20.0"), fixture.calls);
    }

    private static class Fixture {
        private LightLevelConfig current = new LightLevelConfig();
        private final List<String> calls = new ArrayList<>();
        private final ModuleConfigSettingHandle<LightLevelConfig> settings = new ModuleConfigSettingHandle<>(
                () -> current,
                config -> current = config,
                () -> calls.add("changed:" + current.enabled + ":" + current.maxDistance),
                () -> calls.add("save"));
    }
}
