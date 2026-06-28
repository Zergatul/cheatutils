package com.zergatul.cheatutils.mcp.protocol;

import com.google.gson.JsonObject;
import org.jspecify.annotations.Nullable;

public record CallToolRequest(String name, @Nullable JsonObject arguments) {}