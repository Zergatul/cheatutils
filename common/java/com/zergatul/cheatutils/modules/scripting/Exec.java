package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.SendChatEvent;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.scripting.AsyncRunnable;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationResult;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Objects;

public class Exec implements Module {

    public static final Exec instance = new Exec();

    private Exec() {
        Events.SendChat.add(this::onSendChat);
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

        try {
            ScriptExecutionManager.instance.execute(result.<AsyncRunnable>getProgram())
                    .whenComplete((value, throwable) -> ScriptRuntimeFailureHandler.instance.report(
                            "Exec script failed.",
                            throwable));
        } catch (Throwable e) {
            ScriptRuntimeFailureHandler.instance.throwIfFailure("Exec script failed.", e);
            return;
        }

        systemMessage("OK", 0xFF80FF80);
    }

    private void systemMessage(String message, int color) {
        Minecraft.getInstance().gui.chatListener().handleSystemMessage(Component.literal(message).withStyle(Style.EMPTY.withColor(color)), false);
    }
}