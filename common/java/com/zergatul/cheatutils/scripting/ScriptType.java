package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.events.BlockEspConsumer;
import com.zergatul.cheatutils.scripting.events.BlockPosConsumer;
import com.zergatul.cheatutils.scripting.events.EntityEspConsumer;
import com.zergatul.cheatutils.scripting.events.ServerInformation;
import com.zergatul.cheatutils.scripting.modules.BlockEspEvent;
import com.zergatul.cheatutils.scripting.modules.EntityEspEvent;
import com.zergatul.cheatutils.scripting.modules.PacketEvent;
import com.zergatul.cheatutils.scripting.modules.PlayerMessageSendingEvent;
import com.zergatul.cheatutils.scripting.types.*;
import com.zergatul.cheatutils.scripting.types.json.*;
import com.zergatul.cheatutils.scripting.types.nbt.*;
import com.zergatul.cheatutils.utils.ModEnvironmentCommon;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.JavaInteropPolicy;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SVoidType;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

@NullMarked
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
                    .setApis(ApiType.BLOCK_AUTOMATION)
                    .setInterface(BlockPosConsumer.class)
                    .setScriptClassName("BlockAutomationScript")
                    .setModuleName("Block Automation")),

    VILLAGER_ROLLER(
            new Builder()
                    .setApis(ApiType.VILLAGER_ROLLER, ApiType.LOGGING)
                    .setInterface(Runnable.class)
                    .setScriptClassName("VillagerRollerScript")
                    .setModuleName("Villager Roller")),

    EVENTS(
            new Builder()
                    .setApis(ApiType.ACTION, ApiType.UPDATE, ApiType.EVENTS)
                    .setInterface(Runnable.class)
                    .setScriptClassName("EventsScripting")
                    .setModuleName("Events Scripting")),

    BLOCK_ESP(
            new Builder()
                    .setApis(ApiType.CURRENT_BLOCK_ESP)
                    .setInterface(BlockEspConsumer.class)
                    .setScriptClassName("BlockEspScript")
                    .setModuleName("Block ESP")),

    ENTITY_ESP(
            new Builder()
                    .setApis(ApiType.CURRENT_ENTITY_ESP)
                    .setInterface(EntityEspConsumer.class)
                    .setScriptClassName("EntityEspScript")
                    .setModuleName("Entity ESP")),

    EXPR_EVAL(
            new Builder()
                    .setApis(ApiType.ACTION, ApiType.UPDATE)
                    .setScriptClassName("EvalScript")
                    .setModuleName("Eval")),

    EXEC_CODE(
            new Builder()
                    .setApis(ApiType.ACTION, ApiType.UPDATE, ApiType.EXEC_LOGGING)
                    .setInterface(Runnable.class)
                    .setScriptClassName("ExecCode")
                    .setModuleName("Exec"));

    private final ApiType[] apis;
    private final Class<?> funcInterface;
    private final @Nullable SType asyncReturnType;
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
        CompilationParametersBuilder builder = new CompilationParametersBuilder()
                .setRoot(Root.class)
                .addCustomType(EnchantmentWrapper.class)
                .addCustomTypes(List.of(ItemStackWrapper.class, AttributeModifier.class))
                .addCustomType(ItemWrapper.class)
                .addCustomType(Position3d.class)
                .addCustomType(BlockPosWrapper.class)
                .addCustomTypes(List.of(HitResultWrapper.class, RayCastEntityResult.class))
                .addCustomType(HttpRequestWrapper.class)
                .addCustomType(HttpRequestBuilderWrapper.class)
                .addCustomType(HttpResponseWrapper.class)
                .addCustomType(HttpHeader.class)
                .addCustomType(Regex.class)
                .addCustomType(Match.class)
                .addCustomType(MatchGroup.class)
                .addCustomType(MatchGroups.class)
                .addCustomType(BoundingBox.class)
                .addCustomType(ComponentWrapper.class)
                .addCustomType(FormattedTextComponent.class)
                .addCustomType(StyleWrapper.class)
                .addCustomType(PlayerInfoWrapper.class)
                .addCustomTypes(List.of(BlockWrapper.class, BlockStateWrapper.class))
                .addCustomTypes(List.of(
                        TagWrapper.class,
                        MissingTagWrapper.class,
                        ByteTagWrapper.class,
                        ShortTagWrapper.class,
                        IntTagWrapper.class,
                        LongTagWrapper.class,
                        FloatTagWrapper.class,
                        DoubleTagWrapper.class,
                        ByteArrayTagWrapper.class,
                        StringTagWrapper.class,
                        ListTagWrapper.class,
                        CompoundTagWrapper.class,
                        IntArrayTagWrapper.class,
                        LongArrayTagWrapper.class))
                .addCustomTypes(List.of(
                        JsonElementWrapper.class,
                        JsonInvalidWrapper.class,
                        JsonNullWrapper.class,
                        JsonBooleanWrapper.class,
                        JsonNumberWrapper.class,
                        JsonStringWrapper.class,
                        JsonArrayWrapper.class,
                        JsonObjectWrapper.class))
                .addCustomTypes(List.of(UUIDWrapper.class))
                .addCustomTypes(List.of(
                        BlockEspEvent.class,
                        EntityEspEvent.class,
                        PacketEvent.class,
                        ServerInformation.class,
                        PlayerMessageSendingEvent.class))
                .setInterface(funcInterface)
                .setAsyncReturnType(asyncReturnType)
                .setPolicy(new JavaInteropPolicy() {
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
                        return Constants.class.getClassLoader();
                    }
                })
                .setMainClassName(scriptClassName)
                .setSourceFile("<" + scriptClassName + ">")
                .emitLineNumbers(true)
                .emitVariableNames(true);

        if (ModEnvironmentCommon.IS_CURSEFORGE_RESTRICTED) {
            builder.setPolicy(new CurseForgeMethodUsagePolicy());
        }

        return builder.build();
    }

    private static class Builder {

        private ApiType[] apis = new ApiType[0];
        private @Nullable Class<?> funcInterface;
        private @Nullable SType asyncReturnType;
        private @Nullable String scriptClassName;
        private @Nullable String moduleName;

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