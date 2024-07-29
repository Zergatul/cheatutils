package com.zergatul.cheatutils.scripting;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.Compiler;
import com.zergatul.scripting.compiler.VisibilityChecker;

import java.lang.reflect.Method;

public class CompilerFactory {

    public static Compiler create(ApiType[] types) {
        return new Compiler(createParameters(types));
    }

    public static CompilationParameters createParameters(ApiType[] types) {
        return new CompilationParametersBuilder()
                .setRoot(Root.class)
                .setVisibilityChecker(new VisibilityChecker() {
                    @Override
                    public boolean isVisible(Method method) {
                        return VisibilityCheck.isOk(method, types);
                    }
                })
                .build();
    }
}