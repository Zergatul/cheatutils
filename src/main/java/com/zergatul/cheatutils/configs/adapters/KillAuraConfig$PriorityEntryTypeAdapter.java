package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.configs.KillAuraConfig;

import java.io.IOException;

public class KillAuraConfig$PriorityEntryTypeAdapter extends TypeAdapter<KillAuraConfig.PriorityEntry> {
    @Override
    public void write(JsonWriter out, KillAuraConfig.PriorityEntry value) throws IOException {
        if (value == null) {
            out.nullValue();
        } else {
            out.value(value.name);
        }
    }

    @Override
    public KillAuraConfig.PriorityEntry read(JsonReader in) throws IOException {
        String value = in.nextString();
        if (value == null) {
            return null;
        } else {
            return KillAuraConfig.PriorityEntry.entries.get(value);
        }
    }
}
