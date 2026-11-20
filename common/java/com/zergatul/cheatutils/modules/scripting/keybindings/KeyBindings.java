package com.zergatul.cheatutils.modules.scripting.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.collections.*;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.IKeyBindingRegistry;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.configs.modules.KeyBindingEntry;
import com.zergatul.cheatutils.configs.modules.KeyBindingsConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.scripting.keybindings.web.CodeWebApi;
import com.zergatul.cheatutils.modules.scripting.keybindings.web.ConfigWebApi;
import com.zergatul.cheatutils.scripting.*;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveResult;
import com.zergatul.cheatutils.scripting.workspace.ScriptWorkspace;
import com.zergatul.cheatutils.scripting.workspace.slots.KeyBindingScriptSlot;
import com.zergatul.cheatutils.web.WebApiRegistry;
import com.zergatul.scripting.DiagnosticMessage;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.*;

@NullMarked
public class KeyBindings implements Module {

    public static final KeyBindings INSTANCE = new KeyBindings();

    private final SettingHandle<KeyBindingsConfig> settings = Settings.get(KeyBindingsConfig.class);
    private final Minecraft mc = Minecraft.getInstance();
    private final KeyBindingScriptSlot scriptSlot = (KeyBindingScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.KEYBINDING);
    private final KeyMapping[] keys;
    private final Map<String, ScriptActivation<AsyncRunnable>> scripts;
    private final @Nullable ScriptActivation<AsyncRunnable>[] bindings;

    private KeyBindings() {
        Events.RegisterKeyBindings.add(this::onRegisterKeyBindings);
        Events.AfterHandleKeyBindings.add(this::onHandleKeyBindings);
        settings.observe(this::onConfigUpdated);

        WebApiRegistry.INSTANCE.register(new ConfigWebApi());
        WebApiRegistry.INSTANCE.register(new CodeWebApi());

        KeyMapping.Category category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.MOD_ID, "common"));
        this.keys = new KeyMapping[KeyBindingEntry.KEYS_COUNT];
        for (int i = 0; i < keys.length; i++) {
            this.keys[i] = new KeyMapping("key.zergatul.cheatutils.reserved" + i, InputConstants.UNKNOWN.getValue(), category);
        }

        this.scripts = new HashMap<>();
        this.bindings = new ScriptActivation[KeyBindingEntry.KEYS_COUNT];
    }

    public SettingHandle<KeyBindingsConfig> getSettings() {
        return settings;
    }

    public OneOf2<SuccessfulResult, ErrorResult> add(@Nullable String name, int keyIndex) {
        if (name == null || name.isBlank()) {
            return OneOf2.from2(new ErrorResult("Name is required"));
        }
        if (!KeyBindingEntry.isValidKeyIndex(keyIndex)) {
            return OneOf2.from2(new ErrorResult("Invalid key index"));
        }

        return settings.updateWithResult(config -> {
            if (config.entries().stream().anyMatch(e -> e.name().equals(name))) {
                return new SettingUpdateResult<>(
                        config,
                        OneOf2.from2(new ErrorResult("Script with the same name already exists.")));
            }

            KeyBindingEntry entry = KeyBindingEntry.Builder.from(KeyBindingEntry.defaults())
                    .name(name)
                    .keyIndex(keyIndex)
                    .build();

            ImmutableList<KeyBindingEntry> entries = config.entries();
            if (keyIndex != KeyBindingEntry.UNASSIGNED) {
                entries = entries.replaceAll(e -> e.keyIndex() == keyIndex ? e.withKeyIndex(KeyBindingEntry.UNASSIGNED): e);
            }
            entries = entries.add(entry);

            return new SettingUpdateResult<>(
                    config.withEntries(entries),
                    OneOf2.from1(SuccessfulResult.INSTANCE));
        });
    }

    public OneOf2<SuccessfulResult, ErrorResult> update(
            @Nullable String oldName,
            @Nullable String newName,
            int keyIndex
    ) {
        if (oldName == null) {
            return OneOf2.from2(new ErrorResult("Old name is required"));
        }
        if (newName == null || newName.isBlank()) {
            return OneOf2.from2(new ErrorResult("New name is required"));
        }
        if (!KeyBindingEntry.isValidKeyIndex(keyIndex)) {
            return OneOf2.from2(new ErrorResult("Invalid key index"));
        }

        return settings.updateWithResult(config -> {
            KeyBindingEntry entry = config.entries().stream()
                    .filter(e -> e.name().equals(oldName))
                    .findFirst()
                    .orElse(null);
            if (entry == null) {
                return new SettingUpdateResult<>(
                        config,
                        OneOf2.from2(new ErrorResult("Cannot find original script by name " + oldName + ".")));
            }

            if (!oldName.equals(newName)) {
                if (config.entries().stream().anyMatch(e -> e.name().equals(newName))) {
                    return new SettingUpdateResult<>(
                            config,
                            OneOf2.from2(new ErrorResult("Script with the same name already exists.")));
                }
            }

            ImmutableList<KeyBindingEntry> entries = config.entries().replaceAll(e -> {
                if (e == entry) {
                    return e.withName(newName).withKeyIndex(keyIndex);
                }
                if (keyIndex == KeyBindingEntry.UNASSIGNED) {
                    return e;
                }
                if (e.keyIndex() == keyIndex) {
                    return e.withKeyIndex(KeyBindingEntry.UNASSIGNED);
                }
                return e;
            });

            return new SettingUpdateResult<>(
                    config.withEntries(entries),
                    OneOf2.from1(SuccessfulResult.INSTANCE));
        });
    }

    public OneOf2<SuccessfulResult, ErrorResult> delete(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return OneOf2.from2(new ErrorResult("Name is required"));
        }

        return settings.updateWithResult(config -> {
            KeyBindingEntry entry = config.entries().stream()
                    .filter(e -> e.name().equals(name))
                    .findFirst()
                    .orElse(null);
            if (entry == null) {
                return new SettingUpdateResult<>(
                        config,
                        OneOf2.from2(new ErrorResult("Cannot find keybinding entry by name.")));
            }

            return new SettingUpdateResult<>(
                    config.withEntries(config.entries().remove(entry)),
                    OneOf2.from1(SuccessfulResult.INSTANCE));
        });
    }

    public OneOf3<SuccessfulResult, List<DiagnosticMessage>, ErrorResult> saveCode(
            @Nullable String name,
            @Nullable String code
    ) throws Exception {
        if (RenderSystem.isOnRenderThread()) {
            return saveCodeInternal(name, code);
        } else {
            return ClientTickEndExecutor.INSTANCE.submit(() -> saveCodeInternal(name, code)).get();
        }
    }

    public void setScript(String name, @Nullable AsyncRunnable script) {
        if (RenderSystem.isOnRenderThread()) {
            setScriptInternal(name, script);
        } else {
            ClientTickEndExecutor.INSTANCE.execute(() -> setScriptInternal(name, script));
        }
    }

    private void onConfigUpdated(SettingNotificationReason reason, KeyBindingsConfig prev, KeyBindingsConfig curr) {
        ClientTickEndExecutor.INSTANCE.execute(() -> {
            switch (reason) {
                case INITIAL, PROFILE_LOAD -> resetRuntime(curr);
                case UPDATE -> updateRuntime(prev, curr);
            }
        });
    }

    private void resetRuntime(KeyBindingsConfig config) {
        // TODO: how to setup F6 keybind on very first load?
        initializeEmptyState();
        setupScripts(config);
    }

    private void updateRuntime(KeyBindingsConfig prev, KeyBindingsConfig curr) {
        // we don't allow all possible combinations of different operations
        // expecting the user does things one by one from UI
        synchronizeScriptIdentities(prev, curr);
        rebuildAssignments(curr);
    }

    private void synchronizeScriptIdentities(KeyBindingsConfig prev, KeyBindingsConfig curr) {
        if (prev.entries().size() > curr.entries().size()) {
            List<KeyBindingEntry> removed = prev.entries().stream()
                    .filter(e1 -> curr.entries().stream().noneMatch(e2 -> e1.name().equals(e2.name())))
                    .toList();
            for (KeyBindingEntry entry : removed) {
                scriptSlot.remove(entry.name());
            }
            return;
        }

        if (prev.entries().size() < curr.entries().size()) {
            return;
        }

        int size = curr.entries().size();
        for (int i = 0; i < size; i++) {
            KeyBindingEntry prevEntry = prev.entries().get(i);
            KeyBindingEntry currEntry = curr.entries().get(i);

            if (!prevEntry.name().equals(currEntry.name())) {
                scriptSlot.move(prevEntry.name(), currEntry.name());
                ScriptActivation<AsyncRunnable> activation = scripts.remove(prevEntry.name());
                if (activation != null) {
                    activation = activation.withRef(new ScriptRef(ScriptType.KEYBINDING, currEntry.name()));
                    scripts.put(currEntry.name(), activation);
                }
            }
        }
    }

    private void rebuildAssignments(KeyBindingsConfig config) {
        Arrays.fill(bindings, null);

        for (KeyBindingEntry entry : config.entries()) {
            if (entry.keyIndex() == KeyBindingEntry.UNASSIGNED) {
                continue;
            }

            bindings[entry.keyIndex()] = scripts.get(entry.name());
        }
    }

    private OneOf3<SuccessfulResult, List<DiagnosticMessage>, ErrorResult> saveCodeInternal(
            @Nullable String name,
            @Nullable String code
    ) {
        if (name == null || name.isBlank()) {
            return OneOf3.from3(new ErrorResult("Name is required"));
        }

        ScriptSaveResult result = scriptSlot.save(name, code);
        if (result.isSuccess()) {
            return OneOf3.from1(SuccessfulResult.INSTANCE);
        } else {
            return OneOf3.from2(result.getDiagnostics());
        }
    }

    private void setScriptInternal(String name, @Nullable AsyncRunnable script) {
        ScriptActivation<AsyncRunnable> previous = scripts.remove(name);
        if (previous != null) {
            previous.deactivate();
        }
        if (script != null) {
            ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, name);
            ScriptActivation<AsyncRunnable> activation = new ScriptActivation<>(ref, script);
            scripts.put(name, activation);
        }
        rebuildAssignments(settings.get());
    }

    private void onRegisterKeyBindings(IKeyBindingRegistry registry) {
        for (KeyMapping key : keys) {
            registry.register(key);
        }
    }

    private void onHandleKeyBindings() {
        if (mc.player == null) {
            return;
        }

        for (int i = 0; i < keys.length; i++) {
            KeyMapping key = keys[i];
            ScriptActivation<AsyncRunnable> activation = bindings[i];
            while (key.consumeClick()) {
                if (activation != null) {
                    try {
                        ScriptExecutionManager.INSTANCE.execute(
                                activation.getRef(),
                                () -> activation.execute(activation.program));
                    } catch (Throwable e) {
                        ScriptRuntimeFailureHandler.INSTANCE.reportSynchronous("Keybinding script '" + activation.getRef().identifier() + "' failed.", e);
                        break;
                    }
                }
            }
        }
    }

    private void initializeEmptyState() {
        scripts.values().forEach(ScriptActivation::deactivate);
        scripts.clear();
        Arrays.fill(bindings, null);
        scriptSlot.clear();
    }

    private void setupScripts(KeyBindingsConfig config) {
        for (KeyBindingEntry entry : config.entries()) {
            scriptSlot.initAndReport(entry.name(), entry.code());
        }

        rebuildAssignments(config);
    }
}