package com.zergatul.cheatutils.modules.scripting.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
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
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
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
    private final KeyMapping[] keys;
    private final Map<String, ScriptActivation<AsyncRunnable>> scripts;
    private final Object2IntMap<String> revAssignmentsMap;
    private final @Nullable String[] assignments;
    private final @Nullable AsyncRunnable[] actions;

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
        this.revAssignmentsMap = new Object2IntOpenHashMap<>();
        this.assignments = new String[KeyBindingEntry.KEYS_COUNT];
        this.actions = new AsyncRunnable[KeyBindingEntry.KEYS_COUNT];
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

        OneOf2<SuccessfulResult, ErrorResult>[] resultHolder = new OneOf2[1];

        settings.update(config -> {
            if (config.entries().stream().anyMatch(e -> e.name().equals(name))) {
                resultHolder[0] = OneOf2.from2(new ErrorResult("Script with the same name already exists."));
                return config;
            }

            resultHolder[0] = OneOf2.from1(SuccessfulResult.INSTANCE);
            KeyBindingEntry entry = KeyBindingEntry.Builder.from(KeyBindingEntry.defaults())
                    .name(name)
                    .keyIndex(keyIndex)
                    .build();

            ImmutableList<KeyBindingEntry> entries = config.entries();
            if (keyIndex != KeyBindingEntry.UNASSIGNED) {
                entries = entries.replaceAll(e -> e.keyIndex() == keyIndex ? e.withKeyIndex(KeyBindingEntry.UNASSIGNED): e);
            }
            entries = entries.add(entry);

            return config.withEntries(entries);
        });

        return resultHolder[0];
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

        OneOf2<SuccessfulResult, ErrorResult>[] resultHolder = new OneOf2[1];

        settings.update(config -> {
            KeyBindingEntry entry = config.entries().stream()
                    .filter(e -> e.name().equals(oldName))
                    .findFirst()
                    .orElse(null);
            if (entry == null) {
                resultHolder[0] = OneOf2.from2(new ErrorResult("Cannot find original script by name " + oldName + "."));
                return config;
            }

            if (!oldName.equals(newName)) {
                if (config.entries().stream().anyMatch(e -> e.name().equals(newName))) {
                    resultHolder[0] = OneOf2.from2(new ErrorResult("Script with the same name already exists."));
                    return config;
                }
            }

            resultHolder[0] = OneOf2.from1(SuccessfulResult.INSTANCE);

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

            return config.withEntries(entries);
        });

        return resultHolder[0];
    }

    public OneOf2<SuccessfulResult, ErrorResult> delete(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return OneOf2.from2(new ErrorResult("Name is required"));
        }

        OneOf2<SuccessfulResult, ErrorResult>[] resultHolder = new OneOf2[1];

        settings.update(config -> {
            KeyBindingEntry entry = config.entries().stream()
                    .filter(e -> e.name().equals(name))
                    .findFirst()
                    .orElse(null);
            if (entry == null) {
                resultHolder[0] = OneOf2.from2(new ErrorResult("Cannot find keybinding entry by name."));
                return config;
            }

            resultHolder[0] = OneOf2.from1(SuccessfulResult.INSTANCE);
            return config.withEntries(config.entries().remove(entry));
        });

        return resultHolder[0];
    }

    public OneOf3<SuccessfulResult, List<DiagnosticMessage>, ErrorResult> saveCode(
            @Nullable String name,
            @Nullable String code
    ) {
        if (name == null || name.isBlank()) {
            return OneOf3.from3(new ErrorResult("Name is required"));
        }

        ScriptSaveResult result = getScriptSlot().save(name, code);
        if (result.isSuccess()) {
            return OneOf3.from1(SuccessfulResult.INSTANCE);
        } else {
            return OneOf3.from2(result.getDiagnostics());
        }
    }

    public void setScript(String name, @Nullable AsyncRunnable script) {
        ClientTickEndExecutor.INSTANCE.execute(() -> {
            ScriptActivation<AsyncRunnable> previous = scripts.remove(name);
            if (previous != null) {
                previous.deactivate();
            }
            if (script != null) {
                ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, name);
                scripts.put(name, new ScriptActivation<>(ref, script));

                int keyIndex = revAssignmentsMap.containsKey(name) ? revAssignmentsMap.getInt(name) : KeyBindingEntry.UNASSIGNED;
                if (keyIndex >= 0) {
                    actions[keyIndex] = script;
                }
            } else {
                int keyIndex = revAssignmentsMap.containsKey(name) ? revAssignmentsMap.getInt(name) : KeyBindingEntry.UNASSIGNED;
                if (keyIndex >= 0) {
                    revAssignmentsMap.removeInt(name);
                    assignments[keyIndex] = null;
                    actions[keyIndex] = null;
                }
            }
        });
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

        if (prev.entries().size() > curr.entries().size()) {
            List<KeyBindingEntry> removed = prev.entries().stream()
                    .filter(e1 -> curr.entries().stream().noneMatch(e2 -> e1.name().equals(e2.name())))
                    .toList();
            for (KeyBindingEntry entry : removed) {
                getScriptSlot().remove(entry.name());
            }
            return;
        }

        if (prev.entries().size() < curr.entries().size()) {
            List<KeyBindingEntry> added = curr.entries().stream()
                    .filter(e1 -> prev.entries().stream().noneMatch(e2 -> e1.name().equals(e2.name())))
                    .toList();
            for (KeyBindingEntry entry : added) {
                ScriptActivation<AsyncRunnable> activation = scripts.get(entry.name());
                if (activation != null && entry.keyIndex() != KeyBindingEntry.UNASSIGNED) {
                    revAssignmentsMap.put(entry.name(), entry.keyIndex());
                    assignments[entry.keyIndex()] = entry.name();
                    actions[entry.keyIndex()] = activation.program;
                }
            }
            return;
        }

        int size = curr.entries().size();
        for (int i = 0; i < size; i++) {
            KeyBindingEntry prevEntry = prev.entries().get(i);
            KeyBindingEntry currEntry = curr.entries().get(i);

            if (!prevEntry.name().equals(currEntry.name())) {
                getScriptSlot().move(prevEntry.name(), currEntry.name());
                ScriptActivation<AsyncRunnable> activation = scripts.remove(prevEntry.name());
                if (activation != null) {
                    activation = activation.withRef(new ScriptRef(ScriptType.KEYBINDING, currEntry.name()));
                    scripts.put(currEntry.name(), activation);
                    if (prevEntry.keyIndex() != KeyBindingEntry.UNASSIGNED) {
                        revAssignmentsMap.removeInt(prevEntry.name());
                        assignments[prevEntry.keyIndex()] = null;
                        actions[prevEntry.keyIndex()] = null;
                    }
                    if (currEntry.keyIndex() != KeyBindingEntry.UNASSIGNED) {
                        revAssignmentsMap.put(currEntry.name(), currEntry.keyIndex());
                        assignments[currEntry.keyIndex()] = currEntry.name();
                        actions[currEntry.keyIndex()] = activation.program;
                    }
                }
            }

            if (prevEntry.keyIndex() != currEntry.keyIndex()) {
                AsyncRunnable action = prevEntry.keyIndex() != KeyBindingEntry.UNASSIGNED ? actions[prevEntry.keyIndex()] : null;
                if (prevEntry.keyIndex() != KeyBindingEntry.UNASSIGNED) {
                    revAssignmentsMap.removeInt(prevEntry.name());
                    assignments[prevEntry.keyIndex()] = null;
                    actions[prevEntry.keyIndex()] = null;
                }
                if (currEntry.keyIndex() != KeyBindingEntry.UNASSIGNED) {
                    revAssignmentsMap.put(currEntry.name(), currEntry.keyIndex());
                    assignments[currEntry.keyIndex()] = currEntry.name();
                    actions[currEntry.keyIndex()] = action;
                }
            }
        }
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
            AsyncRunnable action = actions[i];
            while (key.consumeClick()) {
                if (action != null) {
                    String name = this.assignments[i];
                    if (name == null) {
                        throw new IllegalStateException();
                    }

                    ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, name);
                    ScriptExecutionManager.INSTANCE.execute(ref, action);
                }
            }
        }
    }

    private void initializeEmptyState() {
        scripts.values().forEach(ScriptActivation::deactivate);
        scripts.clear();
        revAssignmentsMap.clear();
        Arrays.fill(assignments, null);
        Arrays.fill(actions, null);
        getScriptSlot().clear();
    }

    private void setupScripts(KeyBindingsConfig config) {
        for (KeyBindingEntry entry : config.entries()) {
            try {
                ScriptSaveResult result = getScriptSlot().init(entry.name(), entry.code());
                if (!result.isSuccess()) {
                    ScriptRuntimeFailureHandler.INSTANCE.reportCompilationFailure(
                            new ScriptRef(ScriptType.KEYBINDING, entry.name()),
                            result.getDiagnostics());
                }
            } catch (Throwable e) {
                ScriptRuntimeFailureHandler.INSTANCE.reportInitializationFailure(
                        new ScriptRef(ScriptType.KEYBINDING, entry.name()),
                        e);
            }

            if (entry.keyIndex() != KeyBindingEntry.UNASSIGNED) {
                revAssignmentsMap.put(entry.name(), entry.keyIndex());
                assignments[entry.keyIndex()] = entry.name();
                ScriptActivation<AsyncRunnable> activation = scripts.get(entry.name());
                if (activation != null) {
                    actions[entry.keyIndex()] = activation.program;
                }
            }
        }
    }

    private KeyBindingScriptSlot getScriptSlot() {
        return (KeyBindingScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.KEYBINDING);
    }
}