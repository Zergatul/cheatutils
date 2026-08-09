package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.scripting.api.ApiType;
import com.zergatul.cheatutils.scripting.api.Root;
import com.zergatul.cheatutils.scripting.api.VisibilityCheck;
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
                    .setModuleName("Key Bindings")),

    OVERLAY(
            new Builder()
                    .setApis(ApiType.OVERLAY)
                    .setInterface(Runnable.class)
                    .setScriptClassName("StatusOverlayScript")
                    .setModuleName("Status Overlay")),

    BLOCK_AUTOMATION(
            new Builder()
                    .setApis(ApiType.CURRENT_BLOCK, ApiType.BLOCK_PLACER)
                    .setInterface(Runnable.class)
                    .setScriptClassName("BlockAutomationScript")
                    .setModuleName("Block Automation")),

    VILLAGER_ROLLER(
            new Builder()
                    .setApis(ApiType.VILLAGER_ROLLER, ApiType.LOGGING)
                    .setInterface(Runnable.class)
                    .setScriptClassName("VillagerRollerScript")
                    .setModuleName("Villager Roller"));

    private final ApiType[] apis;
    private final Class<?> functionalInterface;
    private final SType asyncReturnType;
    private final String scriptClassName;
    private final String moduleName;

    ScriptType(Builder builder) {
        builder.validate();
        this.apis = builder.apis;
        this.functionalInterface = builder.functionalInterface;
        this.asyncReturnType = builder.asyncReturnType;
        this.scriptClassName = builder.scriptClassName;
        this.moduleName = builder.moduleName;
    }

    public ApiType[] getApis() {
        return apis;
    }

    public String getModuleName() {
        return moduleName;
    }

    public CompilationParameters createParameters() {
        CompilationParametersBuilder builder = new CompilationParametersBuilder()
                .setRoot(Root.class)
                .setInterface(functionalInterface)
                .setPolicy(new JavaInteropPolicy() {
                    @Override
                    public boolean isMethodVisible(Method method) {
                        return VisibilityCheck.isOk(method, apis);
                    }

                    @Override
                    public boolean isJavaTypeUsageAllowed() {
                        return false;
                    }

                    @Override
                    public String getJavaTypeUsageError() {
                        return "Java<...> types are not permitted.";
                    }

                    @Override
                    public ClassLoader getClassLoader() {
                        return Root.class.getClassLoader();
                    }
                })
                .setMainClassName(scriptClassName)
                .setSourceFile("<" + scriptClassName + ">")
                .emitLineNumbers(true)
                .emitVariableNames(true);

        if (asyncReturnType != null) {
            builder.setAsyncReturnType(asyncReturnType);
        }

        return builder.build();
    }

    private static class Builder {

        private ApiType[] apis = new ApiType[0];
        private Class<?> functionalInterface = Runnable.class;
        private SType asyncReturnType;
        private String scriptClassName;
        private String moduleName;

        public Builder setApis(ApiType... apis) {
            this.apis = Objects.requireNonNull(apis);
            return this;
        }

        public Builder setInterface(Class<?> functionalInterface) {
            this.functionalInterface = Objects.requireNonNull(functionalInterface);
            this.asyncReturnType = null;
            return this;
        }

        public Builder setInterface(Class<?> functionalInterface, SType asyncReturnType) {
            this.functionalInterface = Objects.requireNonNull(functionalInterface);
            this.asyncReturnType = Objects.requireNonNull(asyncReturnType);
            return this;
        }

        public Builder setScriptClassName(String scriptClassName) {
            this.scriptClassName = Objects.requireNonNull(scriptClassName);
            return this;
        }

        public Builder setModuleName(String moduleName) {
            this.moduleName = Objects.requireNonNull(moduleName);
            return this;
        }

        private void validate() {
            Objects.requireNonNull(functionalInterface);
            Objects.requireNonNull(scriptClassName);
            Objects.requireNonNull(moduleName);
        }
    }
}