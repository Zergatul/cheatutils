package com.zergatul.cheatutils.mcp.utility;

import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.zergatul.scripting.DiagnosticMessage;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;

@NullMarked
public class Serializer {

    public static JsonElement serialize(@Nullable List<DiagnosticMessage> diagnostics) {
        if (diagnostics == null) {
            return JsonNull.INSTANCE;
        }

        return new JsonArrayBuilder()
                .withItems(diagnostics.stream().map(diagnostic -> new JsonObjectBuilder()
                        .withProperty("message", diagnostic.message)
                        .withProperty("range", new JsonObjectBuilder()
                                .withProperty("line1", diagnostic.range.getLine1())
                                .withProperty("column1", diagnostic.range.getColumn1())
                                .withProperty("line2", diagnostic.range.getLine2())
                                .withProperty("column2", diagnostic.range.getColumn2())
                                .build())
                        .build()))
                .build();
    }
}