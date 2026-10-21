package com.zergatul.cheatutils.configs;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.zergatul.cheatutils.configs.adapters.ImmutableConfigAdapterFactory;
import com.zergatul.cheatutils.configs.modules.EntityTitleConfig;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

public class SettingHandle2Tests {

    @Test
    public void replacementIsPublishedBeforeNotificationAndSavedOnce() {
        Fixture fixture = new Fixture();
        EntityTitleConfig previous = fixture.title.get();
        EntityTitleConfig next = previous.withItemScale(4);
        List<String> calls = new ArrayList<>();
        fixture.title.onChanged((prev, curr) -> {
            assertSame(previous, prev);
            assertSame(next, curr);
            assertSame(curr, fixture.title.get());
            assertEquals(0, fixture.writes);
            calls.add("changed");
        });
        assertTrue(calls.isEmpty());

        fixture.title.set(next);

        assertEquals(List.of("changed"), calls);
        assertEquals(1, fixture.writes);
        assertEquals(0, previous.itemScale());
    }

    @Test
    public void profileSwitchPublishesAllSettingsAndPreservesOldFontValuesWithoutSaving() {
        Fixture fixture = new Fixture();
        Profile previousProfile = fixture.registry.getConfig();
        EntityTitleConfig previous = fixture.title.get();
        Profile next = new Profile();
        next.title = previous.withTitleFont(previous.titleFont().withScale(3));
        next.other = new CounterConfig(42);
        List<EntityTitleConfig> changes = new ArrayList<>();
        fixture.title.onChanged((prev, curr) -> {
            assertSame(next, fixture.registry.getConfig());
            assertSame(next.other, fixture.other.get());
            changes.add(prev);
            changes.add(curr);
        });

        fixture.registry.replace(next);

        assertEquals(List.of(previous, next.title), changes);
        assertFalse(changes.get(0).titleFont().equals(changes.get(1).titleFont()));
        assertEquals(0, fixture.writes);

        fixture.title.update(config -> config.withItemScale(5));

        assertEquals(5, next.title.itemScale());
        assertSame(previous, previousProfile.title);
        assertEquals(0, changes.get(1).itemScale());
        assertEquals(1, fixture.writes);
    }

    @Test
    public void equalProfileStillBecomesActiveAndEqualEditsDoNotNotifyOrSave() {
        Fixture fixture = new Fixture();
        fixture.title.onChanged((_, _) -> fail("Equal values must not notify"));
        Profile next = new Profile();

        fixture.registry.replace(next);
        fixture.title.set(EntityTitleConfig.defaults());
        fixture.title.update(config -> config.withItemScale(0));

        assertSame(next, fixture.registry.getConfig());
        assertSame(next.title, fixture.title.get());
        assertEquals(0, fixture.writes);
    }

    @Test
    public void switchingBackAndCreatingDefaultProfileUseTheSameHandle() {
        Fixture fixture = new Fixture();
        Profile first = fixture.registry.getConfig();
        fixture.title.update(config -> config.withItemScale(2));
        Profile second = new Profile();
        second.title = second.title.withItemScale(3);
        List<Integer> values = new ArrayList<>();
        fixture.title.onChanged((prev, curr) -> {
            values.add(prev.itemScale());
            values.add(curr.itemScale());
        });

        fixture.registry.replace(second);
        fixture.registry.replace(first);
        fixture.registry.replace(new Profile());

        assertEquals(List.of(2, 3, 3, 2, 2, 0), values);
        assertEquals(1, fixture.writes);
    }

    @Test
    public void deserializedWebReplacementAndFunctionalUpdatesShareTheSamePath() {
        Fixture fixture = new Fixture();
        Gson gson = new GsonBuilder().registerTypeAdapterFactory(new ImmutableConfigAdapterFactory()).create();
        List<EntityTitleConfig> changes = new ArrayList<>();
        fixture.title.onChanged((_, curr) -> changes.add(curr));

        fixture.title.set(gson.fromJson("{\"itemScale\":100,\"titleFont\":{\"scale\":2}}", EntityTitleConfig.class));
        fixture.title.update(config -> config.withHpPrefix("HP"));

        assertEquals(50, fixture.title.get().itemScale());
        assertEquals(2, fixture.title.get().titleFont().scale());
        assertEquals(EntityTitleConfig.defaults().hpPrefix(), changes.getFirst().hpPrefix());
        assertEquals("HP", fixture.title.get().hpPrefix());
        assertEquals(2, fixture.writes);
    }

    @Test
    public void concurrentFunctionalUpdatesDoNotLoseChanges() throws Exception {
        Fixture fixture = new Fixture();
        try (var executor = Executors.newFixedThreadPool(2)) {
            Runnable increment = () -> {
                for (int i = 0; i < 100; i++) {
                    fixture.other.update(config -> new CounterConfig(config.value() + 1));
                }
            };
            var first = executor.submit(increment);
            var second = executor.submit(increment);
            first.get();
            second.get();
        }

        assertEquals(200, fixture.other.get().value());
        assertEquals(200, fixture.writes);
    }

    @Test
    public void nullReplacementDoesNotPublishOrSave() {
        Fixture fixture = new Fixture();
        EntityTitleConfig previous = fixture.title.get();

        assertThrows(NullPointerException.class, () -> fixture.title.set(null));
        assertThrows(NullPointerException.class, () -> fixture.title.update(_ -> null));

        assertSame(previous, fixture.title.get());
        assertEquals(0, fixture.writes);
    }

    private static class Fixture {
        private int writes;
        private final SettingsRegistry2<Profile> registry = new SettingsRegistry2<>(new Object(), new Profile(), () -> writes++);
        private final SettingHandle2<EntityTitleConfig> title = registry.register(profile -> profile.title, (profile, value) -> profile.title = value);
        private final SettingHandle2<CounterConfig> other = registry.register(profile -> profile.other, (profile, value) -> profile.other = value);
    }

    private static class Profile {
        private EntityTitleConfig title = EntityTitleConfig.defaults();
        private CounterConfig other = new CounterConfig(0);
    }

    private record CounterConfig(int value) implements ImmutableConfig {}
}
