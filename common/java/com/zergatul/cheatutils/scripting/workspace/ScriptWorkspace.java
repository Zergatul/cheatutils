package com.zergatul.cheatutils.scripting.workspace;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.ClientThread;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.slots.*;
import com.zergatul.scripting.compiler.InstanceCompilationResult;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.LongSupplier;

/**
 * The only entry point for changing scripts. Script documents and module runtimes belong to the client thread:
 * saves are compiled on the calling thread and applied on the client thread, profile loads reload every slot there.
 */
@NullMarked
public class ScriptWorkspace {

    public static final ScriptWorkspace INSTANCE = new ScriptWorkspace();

    private final Map<ScriptType, ScriptSlot> slots;
    private final LongSupplier profileGeneration;

    private ScriptWorkspace() {
        Map<ScriptType, ScriptSlot> slots = new Object2ObjectArrayMap<>();
        slots.put(ScriptType.KEYBINDING, new KeyBindingScriptSlot());
        slots.put(ScriptType.OVERLAY, new StatusOverlayScriptSlot());
        slots.put(ScriptType.BLOCK_AUTOMATION, new BlockAutomationScriptSlot());
        slots.put(ScriptType.VILLAGER_ROLLER, new VillagerRollerScriptSlot());
        slots.put(ScriptType.EVENTS, new EventsScriptingScriptSlot());
        slots.put(ScriptType.BLOCK_ESP, new BlockEspScriptSlot());
        slots.put(ScriptType.ENTITY_ESP, new EntityEspScriptSlot());
        this.slots = slots;
        this.profileGeneration = ConfigStore.INSTANCE::getProfileGeneration;

        Events.ProfileLoaded.add(this::reloadAll);
    }

    // for tests
    ScriptWorkspace(Map<ScriptType, ScriptSlot> slots, LongSupplier profileGeneration) {
        this.slots = slots;
        this.profileGeneration = profileGeneration;
    }

    public ScriptSlot get(ScriptType type) {
        return Objects.requireNonNull(slots.get(type));
    }

    public List<ScriptDocument> getAllInstances() {
        return slots.values().stream().flatMap(descriptor -> descriptor.getInstances().stream()).toList();
    }

    public List<ScriptType> getSupportedTypes() {
        return List.copyOf(slots.keySet());
    }

    /**
     * Compiles on the calling thread, then applies document, settings and runtime together on the client thread.
     * Throws {@link ScriptSaveRejectedException} without changing anything when the profile was switched
     * in the meantime, or when the script target no longer exists.
     */
    public ScriptSaveResult save(ScriptRef ref, @Nullable String code) {
        ScriptSlot slot = get(ref.type());
        long generation = profileGeneration.getAsLong();
        InstanceCompilationResult compilation = code == null || code.isEmpty() ? null : slot.compileScript(code);
        return ClientThread.call(() -> {
            if (profileGeneration.getAsLong() != generation) {
                throw new ScriptSaveRejectedException("Profile was changed while saving the script. The script was not saved.");
            }
            return slot.commit(ref.identifier(), code, compilation);
        });
    }

    void reloadAll() {
        ClientThread.assertCurrent();
        for (ScriptSlot slot : slots.values()) {
            try {
                slot.reload();
            } catch (Throwable e) {
                // one broken slot must not keep scripts of the previous profile in the others
                ScriptRuntimeFailureHandler.INSTANCE.reportInitializationFailure(new ScriptRef(slot.getType()), e);
            }
        }
    }
}
