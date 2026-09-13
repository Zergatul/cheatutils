package com.zergatul.cheatutils.modules.scripting;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.concurrent.ClientTickEndExecutor;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.modules.Module;
import com.zergatul.cheatutils.modules.hacks.AimAssist;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

public class EventsScripting implements Module {

    public static final EventsScripting instance = new EventsScripting();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Runnable> onHandleKeys = new ArrayList<>();
    private final List<Runnable> onTickEnd = new ArrayList<>();
    private final List<Runnable> onMenuTickEnd = new ArrayList<>();
    private final List<AimAssist.TargetPredicate> aimAssistTargetPredicates = new ArrayList<>();

    private EventsScripting() {
        Events.BeforeHandleKeyBindings.add(() -> {
            if (canTrigger()) {
                for (Runnable handler : onHandleKeys) {
                    handler.run();
                }
            }
        });

        Events.ClientTickEnd.add(() -> {
            if (ConfigStore.instance.getConfig().eventsScriptingConfig.enabled &&
                    (mc == null || mc.level == null && mc.player == null)) {
                for (Runnable handler : onMenuTickEnd) {
                    handler.run();
                }
            }
        }, 1000); // let queued script replacements run before menu callbacks

        Events.ClientTickEnd.add(() -> {
            if (canTrigger()) {
                for (Runnable handler : onTickEnd) {
                    handler.run();
                }
            }
        }, 1000); // run after the other client-tick-end modules
    }

    public void setScript(Runnable runnable) {
        clear();
        if (runnable != null) {
            ClientTickEndExecutor.instance.execute(() -> {
                runnable.run();

                if (aimAssistTargetPredicates.isEmpty()) {
                    AimAssist.instance.clearTargetPredicate();
                } else if (aimAssistTargetPredicates.size() == 1) {
                    AimAssist.instance.setTargetPredicate(aimAssistTargetPredicates.get(0));
                } else {
                    List<AimAssist.TargetPredicate> predicates = List.copyOf(aimAssistTargetPredicates);
                    AimAssist.instance.setTargetPredicate(entityId -> {
                        for (AimAssist.TargetPredicate predicate : predicates) {
                            if (!predicate.test(entityId)) return false;
                        }
                        return true;
                    });
                }
            });
        }
    }

    public void clear() {
        ClientTickEndExecutor.instance.execute(() -> {
            onHandleKeys.clear();
            onTickEnd.clear();
            onMenuTickEnd.clear();
            aimAssistTargetPredicates.clear();
        });
    }

    public void addOnHandleKeys(Runnable action) {
        onHandleKeys.add(action);
    }

    public void addOnTickEnd(Runnable action) {
        onTickEnd.add(action);
    }

    public void addOnMenuTickEnd(Runnable action) {
        onMenuTickEnd.add(action);
    }

    public void addAimAssistTargetPredicate(AimAssist.TargetPredicate predicate) {
        aimAssistTargetPredicates.add(predicate);
    }

    private boolean canTrigger() {
        return mc != null && mc.player != null && ConfigStore.instance.getConfig().eventsScriptingConfig.enabled;
    }
}