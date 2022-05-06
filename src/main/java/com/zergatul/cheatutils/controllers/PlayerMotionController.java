package com.zergatul.cheatutils.controllers;

import java.util.ArrayList;
import java.util.List;

public class PlayerMotionController {

    public static final PlayerMotionController instance = new PlayerMotionController();

    private final List<Runnable> onBeforeHandlers = new ArrayList<>();
    private final List<Runnable> onAfterHandlers = new ArrayList<>();

    private PlayerMotionController() {

    }

    public void addOnBeforeSendPosition(Runnable handler) {
        synchronized (onBeforeHandlers) {
            onBeforeHandlers.add(handler);
        }
    }

    public void addOnAfterSendPosition(Runnable handler) {
        synchronized (onAfterHandlers) {
            onAfterHandlers.add(handler);
        }
    }

    public void triggerOnBeforeSendPosition() {
        synchronized (onBeforeHandlers) {
            for (Runnable handler: onBeforeHandlers) {
                handler.run();
            }
        }
    }

    public void triggerOnAfterSendPosition() {
        synchronized (onAfterHandlers) {
            for (Runnable handler: onAfterHandlers) {
                handler.run();
            }
        }
    }

}
