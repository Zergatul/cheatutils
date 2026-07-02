package com.zergatul.cheatutils.scripting.services;

import com.zergatul.cheatutils.scripting.ScriptType;
import org.jspecify.annotations.Nullable;

public record ScriptLocator(ScriptType type, @Nullable String identifier) {}