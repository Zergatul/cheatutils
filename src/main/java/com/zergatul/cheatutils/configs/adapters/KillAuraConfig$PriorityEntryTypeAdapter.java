package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.configs.KillAuraConfig;

import java.io.IOException;

public class KillAuraConfig$PriorityEntryTypeAdapter extends TypeAdapter<KillAuraConfig.PriorityEntry> {
    @Override
    public void write(JsonWriter out, KillAuraConfig.PriorityEntry entry) throws IOException {
        if (entry == null) {
            throw new IllegalStateException("value is null.");
        }

        out.beginObject();

        out.name("name");
        out.value(entry.name);

        out.name("enabled");
        out.value(entry.enabled);

        out.name("description");
        out.value(entry.description);

        if (entry instanceof KillAuraConfig.CustomPriorityEntry customPriorityEntry) {
            out.name("className");
            out.value(customPriorityEntry.className);
        }

        out.endObject();
    }

    @Override
    public KillAuraConfig.PriorityEntry read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.STRING) {
            // migration from previous version
            return null;
        }

        in.beginObject();

        String name = null;
        String description = null;
        String className = null;
        boolean enabled = false;

        while (in.peek() == JsonToken.NAME) {
            switch (in.nextName()) {
                case "name" -> name = in.nextString();
                case "description" -> description = in.nextString();
                case "className" -> className = in.nextString();
                case "enabled" -> enabled = in.nextBoolean();
                default -> in.skipValue();
            }
        }

        in.endObject();

        KillAuraConfig.PriorityEntry entry;
        if (className != null) {
            entry = KillAuraConfig.CustomPriorityEntry.create(name, description, className);
        } else {
            entry = KillAuraConfig.PredefinedPriorityEntry.fromName(name);
        }

        if (entry != null) {
            entry.enabled = enabled;
        }

        return entry;
    }
}