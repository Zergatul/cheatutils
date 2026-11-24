package com.zergatul.cheatutils.configs;

import com.google.gson.Gson;
import com.zergatul.cheatutils.configs.modules.EventsScriptingConfig;
import com.zergatul.cheatutils.configs.modules.VillagerRollerConfig;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ImmutableConfigUpdateOptionsTests {

    @Test
    public void villagerOptionsPreserveTheLatestCode() {
        VillagerRollerConfig original = VillagerRollerConfig.defaults().withCode("old code");
        VillagerRollerConfig updated = original.withCode("new code")
                .withOptions(new VillagerRollerConfig.UpdateOptions(true));

        assertTrue(updated.autoRotate());
        assertEquals("new code", updated.code());
        assertFalse(original.autoRotate());
        assertEquals("old code", original.code());
    }

    @Test
    public void eventsOptionsAndCodePreserveEachOtherInEitherOrder() {
        EventsScriptingConfig original = EventsScriptingConfig.defaults().withCode("old code");
        EventsScriptingConfig.UpdateOptions options = new EventsScriptingConfig.UpdateOptions(true);

        EventsScriptingConfig first = original.withOptions(options).withCode("new code");
        EventsScriptingConfig second = original.withCode("new code").withOptions(options);

        assertEquals(first, second);
        assertTrue(first.enabled());
        assertEquals("new code", first.code());
    }

    @Test
    public void optionsJsonCannotSupplyCode() {
        VillagerRollerConfig.UpdateOptions options = new Gson().fromJson(
                "{\"autoRotate\":true,\"code\":\"uncompiled code\"}",
                VillagerRollerConfig.UpdateOptions.class);
        VillagerRollerConfig updated = VillagerRollerConfig.defaults().withCode("compiled code").withOptions(options);

        assertTrue(updated.autoRotate());
        assertEquals("compiled code", updated.code());
    }
}