package com.zergatul.cheatutils.render.gl;

import static org.lwjgl.opengl.GL30.*;

public class GlStateTracker {

    public static final int PROGRAM = 0x01;
    public static final int TEXTURE = 0x02;
    public static final int FRAMEBUFFER = 0x04;

    private static int FBO;
    private static int VAO;
    private static int VBO;
    //private static boolean blend;
    //private static boolean depth;
    //private static boolean cull;
    private static int texture;
    private static int program;

    private static int binding0;
    private static int binding1;

    public static void save() {
        VAO = glGetInteger(GL_VERTEX_ARRAY_BINDING);
        VBO = glGetInteger(GL_ARRAY_BUFFER_BINDING);
        //blend = glIsEnabled(GL_BLEND);
        //depth = glIsEnabled(GL_DEPTH_TEST);
        //cull = glIsEnabled(GL_CULL_FACE);
        texture = glGetInteger(GL_ACTIVE_TEXTURE);

        program = glGetInteger(GL_CURRENT_PROGRAM);

        glActiveTexture(GL_TEXTURE0);
        binding0 = glGetInteger(GL_TEXTURE_BINDING_2D);
        glActiveTexture(GL_TEXTURE1);
        binding1 = glGetInteger(GL_TEXTURE_BINDING_2D);
    }

    public static void restore() {
        glBindVertexArray(VAO);
        glBindBuffer(GL_ARRAY_BUFFER, VBO);

//        if (blend) {
//            glEnable(GL_BLEND);
//        } else {
//            glDisable(GL_BLEND);
//        }

//        if (depth) {
//            glEnable(GL_DEPTH_TEST);
//        } else {
//            glDisable(GL_DEPTH_TEST);
//        }

//        if (cull) {
//            glEnable(GL_CULL_FACE);
//        } else {
//            glDisable(GL_CULL_FACE);
//        }

        glUseProgram(program);

        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_2D, binding0);
        glActiveTexture(GL_TEXTURE1);
        glBindTexture(GL_TEXTURE_2D, binding1);

        glActiveTexture(texture);
    }

    public static void save(int flags) {
        if ((flags & PROGRAM) != 0) {
            program = glGetInteger(GL_CURRENT_PROGRAM);
        }
        if ((flags & TEXTURE) != 0) {
            texture = glGetInteger(GL_ACTIVE_TEXTURE);

            glActiveTexture(GL_TEXTURE0);
            binding0 = glGetInteger(GL_TEXTURE_BINDING_2D);
            glActiveTexture(GL_TEXTURE1);
            binding1 = glGetInteger(GL_TEXTURE_BINDING_2D);
        }
        if ((flags & FRAMEBUFFER) != 0) {
            FBO = glGetInteger(GL_DRAW_FRAMEBUFFER_BINDING);
        }
    }

    public static void restore(int flags) {
        if ((flags & PROGRAM) != 0) {
            glUseProgram(program);
        }
        if ((flags & TEXTURE) != 0) {
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, binding0);
            glActiveTexture(GL_TEXTURE1);
            glBindTexture(GL_TEXTURE_2D, binding1);

            glActiveTexture(texture);
        }
        if ((flags & FRAMEBUFFER) != 0) {
            glBindFramebuffer(GL_FRAMEBUFFER, FBO);
        }
    }
}