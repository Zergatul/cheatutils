package com.zergatul.cheatutils.scripting;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.VisibilityChecker;

import java.lang.reflect.Method;

public class CompilerFactory {

    public static CompilationParameters createParameters(String type) {
        return createParameters(VisibilityCheck.getTypes(type), switch (type) {
            case "block-automation" -> BlockPosConsumer.class;
            default -> Runnable.class;
        });
    }

    private static CompilationParameters createParameters(ApiType[] types, Class<?> func) {
        return new CompilationParametersBuilder()
                .setRoot(Root.class)
                .setInterface(func)
                .setVisibilityChecker(new VisibilityChecker() {
                    @Override
                    public boolean isVisible(Method method) {
                        return VisibilityCheck.isOk(method, types);
                    }
                })
                .build();
    }
}