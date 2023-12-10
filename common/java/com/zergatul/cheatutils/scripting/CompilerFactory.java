package com.zergatul.cheatutils.scripting;

import com.zergatul.scripting.compiler.MethodVisibilityChecker;
import com.zergatul.scripting.compiler.ScriptingLanguageCompiler;

import java.lang.reflect.Method;

public class CompilerFactory {
    public static ScriptingLanguageCompiler create(ApiType[] types) {
        return new ScriptingLanguageCompiler(Root.class, new MethodVisibilityChecker() {
            @Override
            public boolean isVisible(Method method) {
                return VisibilityCheck.isOk(method, types);
            }
        });
    }
}