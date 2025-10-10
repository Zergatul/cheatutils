package com.zergatul.cheatutils.configs.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.zergatul.cheatutils.common.Registries;
import com.zergatul.cheatutils.configs.BlockTracerConfig;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;

import java.awt.*;
import java.io.IOException;

public class BlockTracerConfigTypeAdapter extends TypeAdapter<BlockTracerConfig> {

    @Override
    public BlockTracerConfig read(JsonReader in) throws IOException {
        JsonToken token = in.peek();
        if (token == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        BlockTracerConfig config = new BlockTracerConfig();

        in.beginObject();
        while (in.peek() != JsonToken.END_OBJECT) {
            String property = in.nextName();
            switch (property) {
                case "block" -> config.block = deserializeBlock(in);
                case "enabled" -> config.enabled = in.nextBoolean();
                case "drawTracers" -> config.drawTracers = in.nextBoolean();
                case "tracerColor" -> config.tracerColor = new Color(in.nextInt(), true);
                case "drawOutline" -> config.drawOutline = in.nextBoolean();
                case "outlineColor" -> config.outlineColor = new Color(in.nextInt(), true);
                case "maxDistance" -> config.maxDistance = in.nextDouble();
                case "tracerMaxDistance" -> config.tracerMaxDistance = deserializeNullableDouble(in);
                case "outlineMaxDistance" -> config.outlineMaxDistance = deserializeNullableDouble(in);
            }
        }
        in.endObject();

        return config;
    }

    @Override
    public void write(JsonWriter out, BlockTracerConfig config) throws IOException {
        if (config == null) {
            throw new IOException();
        }

        out.beginObject();

        out.name("block");
        serializeBlock(out, config.block);

        out.name("enabled").value(config.enabled);
        out.name("drawTracers").value(config.drawTracers);
        out.name("tracerColor").value(config.tracerColor.getRGB());
        out.name("drawOutline").value(config.drawOutline);
        out.name("outlineColor").value(config.outlineColor.getRGB());
        out.name("maxDistance").value(config.maxDistance);
        out.name("tracerMaxDistance").value(config.tracerMaxDistance);
        out.name("outlineMaxDistance").value(config.outlineMaxDistance);

        out.endObject();
    }

    private Block deserializeBlock(JsonReader in) throws IOException {
        return Registries.BLOCKS.getValue(new ResourceLocation(in.nextString()));
    }

    private void serializeBlock(JsonWriter out, Block block) throws IOException {
        out.value(Registries.BLOCKS.getKey(block).toString());
    }

    private Double deserializeNullableDouble(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            return null;
        } else {
            return in.nextDouble();
        }
    }
}