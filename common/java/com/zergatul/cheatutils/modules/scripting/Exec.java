package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.SendChatEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.scripting.AsyncRunnable;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationResult;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import org.jspecify.annotations.Nullable;

import java.util.Objects;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicReference;

public class Exec implements Module {

    public static final Exec instance = new Exec();

    private static final int RUNTIME_FAILURE_PRIORITY = Integer.MAX_VALUE;

    private final AtomicReference<RuntimeException> runtimeFailure = new AtomicReference<>();
    private long nextExecutionId;

    private Exec() {
        Events.SendChat.add(this::onSendChat);
        Events.ClientTickEnd.add(this::onClientTickEnd, RUNTIME_FAILURE_PRIORITY);
    }

    private void onSendChat(SendChatEvent event) {
        if (!ConfigStore.instance.getConfig().execConfig.enabled) {
            return;
        }

        if (!event.getMessage().startsWith(".")) {
            return;
        }

        event.cancel();

        String code = event.getMessage().substring(1);
        CompilationResult result = ScriptCompilerRegistry.INSTANCE.compile(ScriptType.KEYBINDING, code);
        if (result.getProgram() == null) {
            for (DiagnosticMessage message : Objects.requireNonNull(result.getDiagnostics())) {
                systemMessage(message.message, 0xFFFF8080);
            }
            return;
        }

        CompletableFuture<?> future;
        try {
            ScriptRef ref = new ScriptRef(ScriptType.KEYBINDING, "Exec " + (++nextExecutionId));
            future = ScriptExecutionManager.instance.execute(ref, result.<AsyncRunnable>getProgram());
        } catch (Throwable e) {
            RuntimeException crash = createRuntimeFailure(e);
            if (crash != null) {
                throw crash;
            }
            return;
        }

        future.whenComplete((value, throwable) -> crashOnRuntimeFailure(throwable));
        systemMessage("OK", 0xFF80FF80);
    }

    private void crashOnRuntimeFailure(@Nullable Throwable throwable) {
        RuntimeException crash = createRuntimeFailure(throwable);
        if (crash != null) {
            runtimeFailure.compareAndSet(null, crash);
        }
    }

    private void onClientTickEnd() {
        RuntimeException crash = runtimeFailure.getAndSet(null);
        if (crash != null) {
            throw crash;
        }
    }

    private @Nullable RuntimeException createRuntimeFailure(@Nullable Throwable throwable) {
        Throwable failure = throwable;
        while (failure instanceof CompletionException && failure.getCause() != null) {
            failure = failure.getCause();
        }
        if (failure == null || failure instanceof CancellationException) {
            return null;
        }
        return new RuntimeException("Exec script failed.", failure);
    }

    private void systemMessage(String message, int color) {
        Minecraft.getInstance().getChatListener().handleSystemMessage(Component.literal(message).withStyle(Style.EMPTY.withColor(color)), false);
    }
}