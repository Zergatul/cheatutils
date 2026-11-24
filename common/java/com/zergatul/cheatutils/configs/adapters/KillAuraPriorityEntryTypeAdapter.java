package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.configs.modules.KillAuraClassPriorityEntry;
import com.zergatul.cheatutils.configs.modules.KillAuraPredefinedPriority;
import com.zergatul.cheatutils.configs.modules.KillAuraPredefinedPriorityEntry;
import com.zergatul.cheatutils.configs.modules.KillAuraPriorityEntry;
import com.zergatul.cheatutils.utils.ClassUtils;

import java.io.IOException;

public class KillAuraPriorityEntryTypeAdapter extends TypeAdapter<KillAuraPriorityEntry> {

    private static final String PREDEFINED_TYPE = "predefined";
    private static final String CLASS_TYPE = "class";

    @Override
    public void write(JsonWriter out, KillAuraPriorityEntry entry) throws IOException {
        if (entry == null) {
            throw new IllegalStateException("value is null.");
        }

        out.beginObject();

        switch (entry) {
            case KillAuraPredefinedPriorityEntry predefinedPriorityEntry -> {
                out.name("type").value(PREDEFINED_TYPE);
                out.name("priority").value(predefinedPriorityEntry.priority().name());
                out.name("enabled").value(predefinedPriorityEntry.enabled());
            }
            case KillAuraClassPriorityEntry classPriorityEntry -> {
                out.name("type").value(CLASS_TYPE);
                out.name("name").value(classPriorityEntry.name());
                out.name("description").value(classPriorityEntry.description());
                out.name("class").value(classPriorityEntry.type().getName());
                out.name("enabled").value(classPriorityEntry.enabled());
            }
        }

        out.endObject();
    }

    @Override
    public KillAuraPriorityEntry read(JsonReader in) throws IOException {
        if (in.peek() != JsonToken.BEGIN_OBJECT) {
            in.skipValue();
            return null;
        }

        in.beginObject();

        if (!in.nextName().equals("type")) {
            throw new IllegalStateException("KillAuraPriorityType read: 'type' property not found.");
        }

        String entryType = in.nextString();
        switch (entryType) {
            case PREDEFINED_TYPE -> {
                String priority = null;
                Boolean enabled = null;

                while (in.peek() == JsonToken.NAME) {
                    switch (in.nextName()) {
                        case "priority" -> priority = in.nextString();
                        case "enabled" -> enabled = in.nextBoolean();
                        default -> in.skipValue();
                    }
                }

                in.endObject();

                if (priority == null) {
                    throw new IllegalStateException("KillAuraPriorityType read: 'priority' property not found.");
                }
                if (enabled == null) {
                    throw new IllegalStateException("KillAuraPriorityType read: 'enabled' property not found.");
                }

                return new KillAuraPredefinedPriorityEntry(KillAuraPredefinedPriority.valueOf(priority), enabled);
            }

            case CLASS_TYPE -> {
                String name = null;
                String description = null;
                String className = null;
                Boolean enabled = null;

                while (in.peek() == JsonToken.NAME) {
                    switch (in.nextName()) {
                        case "name" -> name = in.nextString();
                        case "description" -> description = in.nextString();
                        case "class" -> className = in.nextString();
                        case "enabled" -> enabled = in.nextBoolean();
                        default -> in.skipValue();
                    }
                }

                in.endObject();

                if (name == null) {
                    throw new IllegalStateException("KillAuraPriorityType read: 'name' property not found.");
                }
                if (className == null) {
                    throw new IllegalStateException("KillAuraPriorityType read: 'class' property not found.");
                }
                if (enabled == null) {
                    throw new IllegalStateException("KillAuraPriorityType read: 'enabled' property not found.");
                }

                Class<?> type;
                try {
                    type = ClassUtils.forName(className);
                } catch (ClassNotFoundException e) {
                    return null;
                }

                return new KillAuraClassPriorityEntry(name, description, type, enabled);
            }

            default -> throw new IllegalStateException("Unknown KillAuraPriorityEntry type '" + entryType + "'.");
        }
    }
}