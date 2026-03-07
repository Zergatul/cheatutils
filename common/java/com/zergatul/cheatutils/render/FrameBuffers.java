package com.zergatul.cheatutils.render;

import com.zergatul.cheatutils.render.gl.FrameBuffer;

public class FrameBuffers {

    private static FrameBuffer fb1;
    private static FrameBuffer fb2;

    public static FrameBuffer get1() {
        if (fb1 == null) {
            fb1 = new FrameBuffer();
        }

        return fb1;
    }

    public static FrameBuffer get2() {
        if (fb2 == null) {
            fb2 = new FrameBuffer();
        }

        return fb2;
    }

    public static void onResize() {
        if (fb1 != null) {
            fb1.delete();
            fb1 = null;
        }
        if (fb2 != null) {
            fb2.delete();
            fb2 = null;
        }
    }
}