package com.zergatul.cheatutils.scripting.monaco.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;
import com.zergatul.scripting.parser.NodeType;

import java.io.IOException;

public class NodeTypeAdapter extends TypeAdapter<NodeType> {

    @Override
    public void write(JsonWriter out, NodeType type) throws IOException {
        if (type == null) {
            out.nullValue();
        } else {
            out.value(type.ordinal());
        }
    }

    @Override
    public NodeType read(JsonReader in) {
        throw new RuntimeException();
    }
}
