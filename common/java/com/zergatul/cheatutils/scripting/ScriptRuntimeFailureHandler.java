package com.zergatul.cheatutils.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.LevelUnloadedException;
import com.zergatul.cheatutils.scripting.workspace.ScriptRef;
import com.zergatul.cheatutils.ui.CustomToast;
import com.zergatul.scripting.DiagnosticMessage;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;

public class ScriptRuntimeFailureHandler {

    public static final ScriptRuntimeFailureHandler instance = new ScriptRuntimeFailureHandler(true);

    private static final int RUNTIME_FAILURE_PRIORITY = Integer.MAX_VALUE;
    private static final int MAX_NOTIFICATION_HISTORY = 100;

    private static final Logger LOGGER = LogManager.getLogger(ScriptRuntimeFailureHandler.class);
    private final ConcurrentLinkedQueue<Throwable> fatalFailures = new ConcurrentLinkedQueue<>();
    private final ConcurrentLinkedQueue<FailureNotification> notifications = new ConcurrentLinkedQueue<>();
    private final ArrayDeque<ScriptNotification> notificationHistory = new ArrayDeque<>();
    private long latestNotificationId;

    ScriptRuntimeFailureHandler() {
        this(false);
    }

    private ScriptRuntimeFailureHandler(boolean registerLifecycleHandler) {
        if (registerLifecycleHandler) {
            Events.ClientTickStart.add(this::showPendingNotifications, RUNTIME_FAILURE_PRIORITY);
            Events.ClientTickEnd.add(this::throwPendingFatalFailure, RUNTIME_FAILURE_PRIORITY);
        }
    }

    public void report(ScriptRef ref, String context, @Nullable Throwable throwable) {
        Throwable failure = unwrapFailure(throwable);
        if (failure == null) {
            return;
        }
        if (isFatal(failure)) {
            // Completion callbacks cannot propagate errors back to the game loop.
            fatalFailures.add(failure);
            return;
        }

        String name = getDisplayName(ref);
        String message = "Script '" + name + "' failed during " + context + ".";
        LOGGER.error(message, failure);
        addToHistory("Script crashed", message, getStackTrace(failure));
        notifications.add(new FailureNotification(
                "Script crashed",
                name + "\n" + summarize(failure) + "\nCheck web UI or Minecraft logs for full details." +
                        (ref.type() == ScriptType.EXEC_CODE ? "" : "\nSave the script to re-enable it.")));
    }

    public void reportCompilationFailure(ScriptRef ref, List<DiagnosticMessage> diagnostics) {
        String name = getDisplayName(ref);
        LOGGER.error("Script '{}' failed to compile during startup.", name);
        diagnostics.forEach(diagnostic -> LOGGER.error("{}", diagnostic.message));
        String summary = diagnostics.isEmpty() ? "Compilation failed." : truncate(singleLine(diagnostics.getFirst().message), 120);
        addToHistory(
                "Script compilation failed",
                "Script '" + name + "' failed to compile during startup.",
                diagnostics.isEmpty() ? "Compilation failed." : String.join("\n", diagnostics.stream().map(ScriptRuntimeFailureHandler::formatDiagnostic).toList()));
        notifications.add(new FailureNotification(
                "Script compilation failed",
                name + "\n" + summary + "\nCheck web UI or Minecraft logs for full details."));
    }

    public void reportInitializationFailure(ScriptRef ref, Throwable failure) {
        Throwable normalized = unwrapFailure(failure);
        if (normalized == null) {
            return;
        }
        rethrowIfFatal(normalized);
        String name = getDisplayName(ref);
        String message = "Script '" + name + "' failed to initialize during startup.";
        LOGGER.error(message, normalized);
        addToHistory("Script initialization failed", message, getStackTrace(normalized));
        notifications.add(new FailureNotification(
                "Script initialization failed",
                name + "\n" + summarize(normalized) + "\nCheck web UI or Minecraft logs for full details."));
    }

    public void report(String message, @Nullable Throwable throwable) {
        Throwable failure = unwrapFailure(throwable);
        if (failure != null) {
            if (isFatal(failure)) {
                // Completion callbacks cannot propagate errors back to the game loop.
                fatalFailures.add(failure);
            } else {
                LOGGER.error(message, failure);
            }
        }
    }

    public void reportSynchronous(String message, @Nullable Throwable throwable) {
        Throwable failure = unwrapFailure(throwable);
        rethrowIfFatal(failure);
        report(message, failure);
    }

    void throwPendingFatalFailure() {
        rethrowIfFatal(fatalFailures.poll());
    }

    void showPendingNotifications() {
        Minecraft mc = Minecraft.getInstance();

        FailureNotification notification;
        while ((notification = notifications.poll()) != null) {
            CustomToast toast = new CustomToast(
                    Duration.ofSeconds(10),
                    Component.literal(notification.title),
                    Component.literal(notification.message));
            if (toast.occcupiedSlotCount() > 5) {
                toast = new CustomToast(
                        Duration.ofSeconds(10),
                        Component.literal(notification.title),
                        Component.literal("Check web UI or Minecraft logs for full details."));
            }
            mc.gui.toastManager().addToast(toast);
        }
    }

    @Nullable FailureNotification pollNotification() {
        return notifications.poll();
    }

    public synchronized NotificationHistory getNotificationHistory() {
        List<ScriptNotification> items = new ArrayList<>(notificationHistory);
        Collections.reverse(items);
        return new NotificationHistory(latestNotificationId, List.copyOf(items));
    }

    synchronized void addToHistory(String title, String message, String details) {
        notificationHistory.addLast(new ScriptNotification(
                ++latestNotificationId,
                System.currentTimeMillis(),
                title,
                message,
                details));
        while (notificationHistory.size() > MAX_NOTIFICATION_HISTORY) {
            notificationHistory.removeFirst();
        }
    }

    static String getDisplayName(ScriptRef ref) {
        return ref.identifier() == null ? ref.type().getModuleName() : ref.type().getModuleName() + " / " + ref.identifier();
    }

    private static String summarize(Throwable failure) {
        String type = failure.getClass().getSimpleName();
        String message = failure.getMessage();
        return message == null || message.isBlank() ? type : type + ": " + truncate(singleLine(message), 120);
    }

    private static String singleLine(String value) {
        return value.replace('\r', ' ').replace('\n', ' ');
    }

    private static String truncate(String value, int maxLength) {
        return value.length() <= maxLength ? value : value.substring(0, maxLength - 3) + "...";
    }

    private static String getStackTrace(Throwable failure) {
        StringWriter writer = new StringWriter();
        failure.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    private static String formatDiagnostic(DiagnosticMessage diagnostic) {
        String location = "";
        try {
            location = "Line " + diagnostic.range.getLine1() + ", column " + diagnostic.range.getColumn1() + ": ";
        } catch (UnsupportedOperationException _) {
        }
        return "[" + diagnostic.code + "] " + location + diagnostic.message;
    }

    static @Nullable Throwable unwrapFailure(@Nullable Throwable throwable) {
        Throwable failure = throwable;
        while ((failure instanceof CompletionException || failure instanceof ExecutionException) && failure.getCause() != null) {
            failure = failure.getCause();
        }
        if (failure == null ||
                failure instanceof CancellationException ||
                failure instanceof LevelUnloadedException ||
                failure instanceof ControllableStopException
        ) {
            return null;
        }
        return failure;
    }

    static boolean isFatal(@Nullable Throwable failure) {
        return failure instanceof VirtualMachineError || failure instanceof ThreadDeath || failure instanceof LinkageError;
    }

    static void rethrowIfFatal(@Nullable Throwable failure) {
        if (isFatal(failure)) {
            throw (Error) failure;
        }
    }

    record FailureNotification(String title, String message) {}

    public record ScriptNotification(long id, long createdAt, String title, String message, String details) {}

    public record NotificationHistory(long latestId, List<ScriptNotification> notifications) {}
}