package com.zergatul.cheatutils.modules.visuals;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.events.GetFieldOfViewEvent;
import com.zergatul.cheatutils.modules.Module;

public class Zoom implements Module {

    public static final Zoom instance = new Zoom();

    private State state;
    private long begin;
    private long end;
    private double finalFov;
    private double fovFactor;

    private Zoom() {
        Events.GetFieldOfView.add(this::onGetFov);
        state = State.NONE;
    }

    public boolean isActive() {
        return state != State.NONE;
    }

    public void startZooming(double fov, double seconds) {
        switch (state) {
            case NONE -> {
                state = State.ZOOM_IN;
                begin = System.nanoTime();
                end = begin + (long) (seconds * 1e9);
                finalFov = fov;
            }
            case ZOOM_STATIC -> {
                if (finalFov != fov) {
                    finalFov = fov;
                }
            }
            case ZOOM_OUT -> {
                state = State.ZOOM_IN;
                long duration = end - begin;
                long now = System.nanoTime();
                long elapsed = now - begin;
                end = now + elapsed;
                begin = end - duration;
                finalFov = fov;
            }
        }
    }

    public void stopZooming() {
        switch (state) {
            case ZOOM_IN -> {
                state = State.ZOOM_OUT;
                long duration = end - begin;
                long now = System.nanoTime();
                long elapsed = now - begin;
                end = now + elapsed;
                begin = end - duration;
            }
            case ZOOM_STATIC -> {
                state = State.ZOOM_OUT;
                long duration = end - begin;
                begin = System.nanoTime();
                end = begin + duration;
            }
        }
    }

    public double getFovFactor() {
        return fovFactor;
    }

    private void onGetFov(GetFieldOfViewEvent event) {
        long now = System.nanoTime();
        double originalFov = event.get();
        switch (state) {
            case ZOOM_IN:
                if (now >= end) {
                    state = State.ZOOM_STATIC;
                    onGetFov(event);
                    return;
                }
                event.set(perceptual(originalFov, finalFov, 1d * (now - begin) / (end - begin)));
                break;

            case ZOOM_STATIC:
                event.set(finalFov);
                break;

            case ZOOM_OUT:
                if (now >= end) {
                    state = State.NONE;
                    return;
                }
                event.set(perceptual(finalFov, originalFov, 1d * (now - begin) / (end - begin)));
                break;
        }

        fovFactor = event.get() / originalFov;
    }

    private double perceptual(double fov1, double fov2, double t) {
        assert 0 <= t && t <= 1;

        double scale1 = 1 / Math.tan(Math.toRadians(fov1 / 2));
        double scale2 = 1 / Math.tan(Math.toRadians(fov2 / 2));
        double scale = lerp(scale1, scale2, smoothstep(t));
        return Math.toDegrees(2 * Math.atan(1 / scale));
    }

    private double smoothstep(double t) {
        return t * t * (3 - 2 * t);
    }

    public static double lerp(double v1, double v2, double t) {
        return v1 + t * (v2 - v1);
    }

    private enum State {
        NONE,
        ZOOM_IN,
        ZOOM_STATIC,
        ZOOM_OUT
    }
}