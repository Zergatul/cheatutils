package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.JavaInteropPolicy;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SVoidType;

import java.lang.reflect.Method;
import java.util.Objects;

public enum ScriptType {

    KEYBINDING(
            new Builder()
                    .setApis(ApiType.ACTION, ApiType.UPDATE)
                    .setInterface(AsyncRunnable.class, SVoidType.instance)
                    .setScriptClassName("KeyBindingScript")
                    .setModuleName("Key Bindings"));

    private final ApiType[] apis;
    private final Class<?> funcInterface;
    private final SType asyncReturnType;
    private final String scriptClassName;
    private final String moduleName;

    ScriptType(Builder builder) {
        builder.build();
        this.apis = builder.apis;
        this.funcInterface = Objects.requireNonNull(builder.funcInterface);
        this.asyncReturnType = builder.asyncReturnType;
        this.scriptClassName = Objects.requireNonNull(builder.scriptClassName);
        this.moduleName = Objects.requireNonNull(builder.moduleName);
    }

    public ApiType[] getApis() {
        return apis;
    }

    public String getModuleName() {
        return moduleName;
    }

    public CompilationParameters createParameters() {
        return new CompilationParametersBuilder()
                .setRoot(Root.class)
                .setInterface(funcInterface)
                .setAsyncReturnType(asyncReturnType)
                .setMainClassName(scriptClassName)
                .setSourceFile("<" + scriptClassName + ">")
                .emitLineNumbers(true)
                .emitVariableNames(true)
                .setPolicy(new ScriptInteropPolicy(apis))
                .build();
    }

    private static class ScriptInteropPolicy extends JavaInteropPolicy {

        private final ApiType[] apis;

        public ScriptInteropPolicy(ApiType[] apis) {
            this.apis = apis;
        }

        @Override
        public boolean isMethodVisible(Method method) {
            return VisibilityCheck.isOk(method, apis);
        }

        @Override
        public boolean isJavaTypeUsageAllowed() {
            return ConfigStore.instance.getConfig().coreConfig.advancedScripting;
        }

        @Override
        public String getJavaTypeUsageError() {
            return "Java<…> types are not permitted. Enable Advanced Scripting to use Java interop";
        }

        @Override
        public ClassLoader getClassLoader() {
            return Root.class.getClassLoader();
        }
    }

    private static class Builder {

        private ApiType[] apis = new ApiType[0];
        private Class<?> funcInterface;
        private SType asyncReturnType;
        private String scriptClassName;
        private String moduleName;

        public Builder() {
            this.funcInterface = Runnable.class;
        }

        public void build() {
            if (funcInterface == null) {
                throw new IllegalStateException();
            }
            if (scriptClassName == null) {
                throw new IllegalStateException();
            }
            if (moduleName == null) {
                throw new IllegalStateException();
            }
        }

        public Builder setApis(ApiType... apis) {
            this.apis = Objects.requireNonNull(apis);
            return this;
        }

        public Builder setInterface(Class<?> funcInterface) {
            this.funcInterface = funcInterface;
            this.asyncReturnType = null;
            return this;
        }

        public Builder setInterface(Class<?> funcInterface, SType asyncReturnType) {
            this.funcInterface = funcInterface;
            this.asyncReturnType = asyncReturnType;
            return this;
        }

        public Builder setScriptClassName(String name) {
            this.scriptClassName = name;
            return this;
        }

        public Builder setModuleName(String name) {
            this.moduleName = name;
            return this;
        }
    }
}