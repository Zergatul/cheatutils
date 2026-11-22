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

public class SettingHandleTests {

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
        SettingsSnapshot previousProfile = fixture.registry.getSnapshot();
        EntityTitleConfig previous = fixture.title.get();
        EntityTitleConfig nextTitle = previous.withTitleFont(previous.titleFont().withScale(3));
        CounterConfig nextOther = new CounterConfig(42);
        SettingsSnapshot next = fixture.profile(nextTitle, nextOther);
        List<EntityTitleConfig> changes = new ArrayList<>();
        fixture.title.onChanged((prev, curr) -> {
            assertSame(curr, fixture.title.get());
            assertSame(nextOther, fixture.other.get());
            changes.add(prev);
            changes.add(curr);
        });

        fixture.registry.replace(SettingNotificationReason.UPDATE, next);

        assertEquals(List.of(previous, nextTitle), changes);
        assertFalse(changes.get(0).titleFont().equals(changes.get(1).titleFont()));
        assertEquals(0, fixture.writes);

        fixture.title.update(config -> config.withItemScale(5));

        assertEquals(5, fixture.title.get().itemScale());
        // published snapshots never change
        assertSame(nextTitle, fixture.titleOf(next));
        assertSame(previous, fixture.titleOf(previousProfile));
        assertEquals(0, changes.get(1).itemScale());
        assertEquals(1, fixture.writes);
    }

    @Test
    public void equalProfileStillBecomesActiveAndEqualEditsDoNotNotifyOrSave() {
        Fixture fixture = new Fixture();
        fixture.title.onChanged((_, _) -> fail("Equal values must not notify"));
        SettingsSnapshot next = fixture.registry.createDefaults();

        fixture.registry.replace(SettingNotificationReason.UPDATE, next);
        fixture.title.set(EntityTitleConfig.defaults());
        fixture.title.update(config -> config.withItemScale(0));

        assertSame(next, fixture.registry.getSnapshot());
        assertSame(fixture.titleOf(next), fixture.title.get());
        assertEquals(0, fixture.writes);
    }

    @Test
    public void switchingBackAndCreatingDefaultProfileUseTheSameHandle() {
        Fixture fixture = new Fixture();
        fixture.title.update(config -> config.withItemScale(2));
        SettingsSnapshot first = fixture.registry.getSnapshot();
        SettingsSnapshot second = fixture.profile(EntityTitleConfig.defaults().withItemScale(3), new CounterConfig(0));
        List<Integer> values = new ArrayList<>();
        fixture.title.onChanged((prev, curr) -> {
            values.add(prev.itemScale());
            values.add(curr.itemScale());
        });

        fixture.registry.replace(SettingNotificationReason.UPDATE, second);
        fixture.registry.replace(SettingNotificationReason.UPDATE, first);
        fixture.registry.replace(SettingNotificationReason.UPDATE, fixture.registry.createDefaults());

        assertEquals(List.of(2, 3, 3, 2, 2, 0), values);
        assertEquals(1, fixture.writes);
    }

    @Test
    public void capturedSnapshotDoesNotChangeWithLaterUpdates() {
        Fixture fixture = new Fixture();
        SettingsSnapshot captured = fixture.registry.getSnapshot();

        fixture.title.update(config -> config.withItemScale(7));
        fixture.other.set(new CounterConfig(1));

        assertEquals(0, fixture.titleOf(captured).itemScale());
        assertEquals(7, fixture.titleOf(fixture.registry.getSnapshot()).itemScale());
        assertNotSame(captured, fixture.registry.getSnapshot());
    }

    @Test
    public void settingsAreKeyedByTypeNameAndRegisteredOnce() {
        Fixture fixture = new Fixture();

        assertEquals(List.of("entityTitleConfig", "counterConfig"),
                fixture.registry.getEntries().stream().map(SettingsRegistry.Entry::key).toList());
        assertSame(fixture.title, fixture.registry.find(EntityTitleConfig.class));
        assertThrows(IllegalArgumentException.class, () -> fixture.registry.register(EntityTitleConfig::defaults));
        assertThrows(IllegalArgumentException.class, () -> fixture.registry.find(UnregisteredConfig.class));
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

    @Test
    public void profileListenersRunUnderTheStoreLockInOrderWithUpdates() {
        Fixture fixture = new Fixture();
        List<Boolean> lockHeld = new ArrayList<>();
        fixture.title.onChanged((_, _) -> lockHeld.add(Thread.holdsLock(fixture.lock)));
        fixture.title.observe((_, _, _) -> lockHeld.add(Thread.holdsLock(fixture.lock)));

        SettingsSnapshot next = fixture.profile(fixture.title.get().withItemScale(4), new CounterConfig(0));
        fixture.registry.replace(SettingNotificationReason.PROFILE_LOAD, next);
        fixture.title.update(config -> config.withItemScale(5));

        assertEquals(List.of(true, true, true, true), lockHeld);
    }

    private static class Fixture {
        private int writes;
        private final Object lock = new Object();
        private final SettingsRegistry registry = new SettingsRegistry(lock, () -> writes++);
        private final SettingHandle<EntityTitleConfig> title = registry.register(EntityTitleConfig::defaults);
        private final SettingHandle<CounterConfig> other = registry.register(CounterConfig::defaults);

        private SettingsSnapshot profile(EntityTitleConfig title, CounterConfig other) {
            return registry.createSnapshot(entry -> entry.type() == EntityTitleConfig.class ? title : other);
        }

        @SuppressWarnings("unchecked")
        private EntityTitleConfig titleOf(SettingsSnapshot snapshot) {
            return snapshot.get((SettingsRegistry.Entry<EntityTitleConfig>) registry.getEntries().getFirst());
        }
    }

    private record CounterConfig(int value) implements ImmutableConfig {
        public static CounterConfig defaults() {
            return new CounterConfig(0);
        }
    }

    private record UnregisteredConfig(int value) implements ImmutableConfig {}
}
