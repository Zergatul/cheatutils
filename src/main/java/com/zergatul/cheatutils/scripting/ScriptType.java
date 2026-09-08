package com.zergatul.cheatutils.scripting;

import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.JavaInteropPolicy;

import java.lang.reflect.Method;

public enum ScriptType {
    KEYBINDING("KeyBindingScript"),
    EXEC_CODE("ExecCode");

    private final String scriptClassName;

    ScriptType(String scriptClassName) {
        this.scriptClassName = scriptClassName;
    }

    public CompilationParameters createParameters() {
        return new CompilationParametersBuilder()
                .setRoot(Root.class)
                .setInterface(Runnable.class)
                .setMainClassName(scriptClassName)
                .setSourceFile("<" + scriptClassName + ">")
                .emitLineNumbers(true)
                .emitVariableNames(true)
                .setPolicy(new JavaInteropPolicy() {
                    @Override
                    public boolean isMethodVisible(Method method) {
                        return method.getDeclaringClass() != Object.class;
                    }

                    @Override
                    public boolean isJavaTypeUsageAllowed() {
                        return false;
                    }

                    @Override
                    public String getJavaTypeUsageError() {
                        return "Java interop is not available in the minimal scripting API.";
                    }

                    @Override
                    public ClassLoader getClassLoader() {
                        return Root.class.getClassLoader();
                    }
                })
                .build();
    }
}
