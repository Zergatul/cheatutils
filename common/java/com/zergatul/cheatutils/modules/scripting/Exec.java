package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.SendChatEvent;
import com.zergatul.cheatutils.configs.*;
import com.zergatul.cheatutils.configs.modules.ExecConfig;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.scripting.AsyncRunnable;
import com.zergatul.cheatutils.scripting.ScriptActivation;
import com.zergatul.cheatutils.scripting.ScriptExecutionManager;
import com.zergatul.cheatutils.scripting.ScriptRuntimeFailureHandler;
import com.zergatul.cheatutils.scripting.ScriptType;
import com.zergatul.cheatutils.scripting.ScriptCompilerRegistry;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.web.WebApiRegistry;
import com.zergatul.scripting.DiagnosticMessage;
import com.zergatul.scripting.compiler.CompilationResult;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

import java.util.Objects;

public class Exec implements Module {

    public static final Exec INSTANCE = new Exec();

    private final SettingHandle2<ExecConfig> settings = Settings.get(ExecConfig.class);

    private Exec() {
        Events.SendChat.add(this::onSendChat);

        WebApiRegistry.INSTANCE.register("exec", settings, ExecConfig.class);
    }

    public SettingHandle2<ExecConfig> getSettings() {
        return settings;
    }

    private void onSendChat(SendChatEvent event) {
        if (!settings.get().enabled()) {
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
            ScriptActivation<AsyncRunnable> activation = new ScriptActivation<>(new ScriptRef(ScriptType.EXEC_CODE), result.getProgram());
            ScriptExecutionManager.instance.execute(() -> activation.execute(activation.program));
        } catch (Throwable e) {
            ScriptRuntimeFailureHandler.instance.reportSynchronous("Exec script failed.", e);
            return;
        }

        systemMessage("OK", 0xFF80FF80);
    }

    private void systemMessage(String message, int color) {
        Minecraft.getInstance().gui.chatListener().handleSystemMessage(Component.literal(message).withStyle(Style.EMPTY.withColor(color)), false);
    }
}