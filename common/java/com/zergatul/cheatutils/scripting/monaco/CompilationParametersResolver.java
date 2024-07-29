package com.zergatul.cheatutils.scripting.monaco;

import com.zergatul.scripting.compiler.CompilationParameters;

public interface CompilationParametersResolver {
    CompilationParameters resolve(String type);
}