package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.events.BlockEspConsumer;
import com.zergatul.cheatutils.scripting.events.BlockPosConsumer;
import com.zergatul.cheatutils.scripting.events.EntityEspConsumer;
import com.zergatul.cheatutils.scripting.modules.PacketEvent;
import com.zergatul.cheatutils.scripting.types.*;
import com.zergatul.cheatutils.scripting.types.nbt.*;
import com.zergatul.scripting.compiler.CompilationParameters;
import com.zergatul.scripting.compiler.CompilationParametersBuilder;
import com.zergatul.scripting.compiler.JavaInteropPolicy;
import com.zergatul.scripting.type.SType;
import com.zergatul.scripting.type.SVoidType;

import java.lang.reflect.Method;
import java.util.List;

public enum ScriptType {
    KEYBINDING(
            new ApiType[] { ApiType.ACTION, ApiType.UPDATE },
            AsyncRunnable.class,
            "KeyBindingScript",
            SVoidType.instance),

    OVERLAY(
            new ApiType[] { ApiType.OVERLAY },
            Runnable.class,
            "StatusOverlayScript"),

    BLOCK_AUTOMATION(
            new ApiType[] { ApiType.BLOCK_AUTOMATION },
            BlockPosConsumer.class,
            "BlockAutomationScript"),

    VILLAGER_ROLLER(
            new ApiType[] { ApiType.VILLAGER_ROLLER, ApiType.LOGGING },
            Runnable.class,
            "VillagerRollerScript"),

    EVENTS(
            new ApiType[] { ApiType.ACTION, ApiType.UPDATE, ApiType.EVENTS },
            Runnable.class,
            "EventsScripting"),

    BLOCK_ESP(
            new ApiType[] { ApiType.CURRENT_BLOCK_ESP },
            BlockEspConsumer.class,
            "BlockEspScript"),

    ENTITY_ESP(
            new ApiType[] { ApiType.CURRENT_ENTITY_ESP },
            EntityEspConsumer.class,
            "EntityEspScript"),

    KILL_AURA(
            new ApiType[0],
            KillAuraFunction.class,
            "KillAuraScript"),

    HITBOX_SIZE(
            new ApiType[0],
            HitboxSizeFunction.class,
            "HitboxSizeScript");

    private final ApiType[] apis;
    private final Class<?> funcInterface;
    private final String name;
    private final SType asyncReturnType;

    ScriptType(ApiType[] apis, Class<?> funcInterface, String name) {
        this(apis, funcInterface, name, null);
    }

    ScriptType(ApiType[] apis, Class<?> funcInterface, String name, SType asyncReturnType) {
        this.apis = apis;
        this.funcInterface = funcInterface;
        this.name = name;
        this.asyncReturnType = asyncReturnType;
    }

    public ApiType[] getApis() {
        return apis;
    }

    public CompilationParameters createParameters() {
        return new CompilationParametersBuilder()
                .setRoot(Root.class)
                .addCustomType(EnchantmentWrapper.class)
                .addCustomType(ItemStackWrapper.class)
                .addCustomType(ItemWrapper.class)
                .addCustomType(Position3d.class)
                .addCustomType(BlockPosWrapper.class)
                .addCustomType(HttpRequestWrapper.class)
                .addCustomType(HttpRequestBuilderWrapper.class)
                .addCustomType(HttpResponseWrapper.class)
                .addCustomType(HttpHeader.class)
                .addCustomType(Regex.class)
                .addCustomType(Match.class)
                .addCustomType(MatchGroup.class)
                .addCustomType(MatchGroups.class)
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
                        LongArrayTagWrapper.class
                ))
                .addCustomTypes(List.of(UUIDWrapper.class))
                .addCustomType(PacketEvent.class)
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
                })
                .setClassNamePrefix(name)
                .setSourceFile("<" + name + ">")
                .setLineNumbers(true)
                .build();
    }
}