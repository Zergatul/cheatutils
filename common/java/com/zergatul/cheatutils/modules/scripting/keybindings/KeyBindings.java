package com.zergatul.cheatutils.modules.scripting.keybindings;

import com.mojang.blaze3d.platform.InputConstants;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.collections.*;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.IKeyBindingRegistry;
import com.zergatul.cheatutils.concurrent.ClientThread;
import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.configs.modules.KeyBindingEntry;
import com.zergatul.cheatutils.configs.modules.KeyBindingsConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.scripting.keybindings.web.CodeWebApi;
import com.zergatul.cheatutils.modules.scripting.keybindings.web.ConfigWebApi;
import com.zergatul.cheatutils.scripting.*;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.scripting.workspace.ScriptSaveRejectedException;
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

/**
 * Scripts and key assignments belong to the client thread. Entry changes that affect scripts
 * (add, rename, delete) run there as one step together with the config update.
 */
@NullMarked
public class KeyBindings implements Module {

    public static final KeyBindings INSTANCE = new KeyBindings();

    private final SettingHandle<KeyBindingsConfig> settings = Settings.get(KeyBindingsConfig.class);
    private final Minecraft mc = Minecraft.getInstance();
    private final KeyMapping[] keys;
    private final Map<String, ScriptActivation<AsyncRunnable>> scripts;
    private final @Nullable ScriptActivation<AsyncRunnable>[] bindings;

    private KeyBindings() {
        Events.RegisterKeyBindings.add(this::onRegisterKeyBindings);
        Events.AfterHandleKeyBindings.add(this::onHandleKeyBindings);

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

        return ClientThread.call(() -> {
            OneOf2<SuccessfulResult, ErrorResult> result = settings.updateWithResult(config -> {
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

            rebuildAssignments(settings.get());
            return result;
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

        return ClientThread.call(() -> {
            OneOf2<SuccessfulResult, ErrorResult> result = settings.updateWithResult(config -> {
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

            if (isSuccess(result) && !oldName.equals(newName)) {
                getScriptSlot().move(oldName, newName);
            }

            rebuildAssignments(settings.get());
            return result;
        });
    }

    public OneOf2<SuccessfulResult, ErrorResult> delete(@Nullable String name) {
        if (name == null || name.isBlank()) {
            return OneOf2.from2(new ErrorResult("Name is required"));
        }

        return ClientThread.call(() -> {
            OneOf2<SuccessfulResult, ErrorResult> result = settings.updateWithResult(config -> {
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

            if (isSuccess(result)) {
                getScriptSlot().remove(name);
            }

            rebuildAssignments(settings.get());
            return result;
        });
    }

    public OneOf3<SuccessfulResult, List<DiagnosticMessage>, ErrorResult> saveCode(
            @Nullable String name,
            @Nullable String code
    ) {
        if (name == null || name.isBlank()) {
            return OneOf3.from3(new ErrorResult("Name is required"));
        }

        ScriptSaveResult result;
        try {
            result = ScriptWorkspace.INSTANCE.save(new ScriptRef(ScriptType.KEYBINDING, name), code);
        } catch (ScriptSaveRejectedException e) {
            return OneOf3.from3(new ErrorResult(e.getMessage()));
        }

        if (result.isSuccess()) {
            return OneOf3.from1(SuccessfulResult.INSTANCE);
        } else {
            return OneOf3.from2(result.getDiagnostics());
        }
    }

    /** Client thread. */
    public void setScript(String name, @Nullable AsyncRunnable script) {
        ClientThread.assertCurrent();

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

    /** Client thread. Keeps the installed program, and its running execution, under the new name. */
    public void renameScript(String oldName, String newName) {
        ClientThread.assertCurrent();

        ScriptActivation<AsyncRunnable> activation = scripts.remove(oldName);
        if (activation != null) {
            scripts.put(newName, activation.withRef(new ScriptRef(ScriptType.KEYBINDING, newName)));
        }
        rebuildAssignments(settings.get());
    }

    // TODO: how to setup F6 keybind on very first load?
    /** Client thread. */
    public void clearScripts() {
        ClientThread.assertCurrent();

        scripts.values().forEach(ScriptActivation::deactivate);
        scripts.clear();
        Arrays.fill(bindings, null);
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

    private static KeyBindingScriptSlot getScriptSlot() {
        return (KeyBindingScriptSlot) ScriptWorkspace.INSTANCE.get(ScriptType.KEYBINDING);
    }

    private static boolean isSuccess(OneOf2<SuccessfulResult, ErrorResult> result) {
        return result.match(_ -> true, _ -> false);
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
}
