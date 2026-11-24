package com.zergatul.cheatutils.concurrent;

import com.zergatul.cheatutils.modules.scripting.EventsScripting;
import com.zergatul.cheatutils.scripting.ScriptActivation;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/** Reflection access to EventsScripting internals for tests. */
final class EventsScriptingAccess {

    private EventsScriptingAccess() {}

    static @Nullable ScriptActivation<?> getActivation() throws Exception {
        Object installation = getInstallation();
        return installation == null ? null : (ScriptActivation<?>) getField(installation, "activation");
    }

    static List<?> getCallbacks(String name) throws Exception {
        Object installation = getInstallation();
        return installation == null ? List.of() : (List<?>) getField(installation, name);
    }

    static void drainFailures() throws Exception {
        Method method = EventsScripting.class.getDeclaredMethod("processPendingFailures");
        method.setAccessible(true);
        method.invoke(EventsScripting.INSTANCE);
    }

    private static @Nullable Object getInstallation() throws Exception {
        return getField(EventsScripting.INSTANCE, "installation");
    }

    private static Object getField(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
}
