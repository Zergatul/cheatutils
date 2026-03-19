package com.zergatul.cheatutils.render.gl;

import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;

public class GlBufferUtils {

    public static float[] toFloats(int handle, int size) {
        ByteBuffer bytes = BufferUtils.createByteBuffer(size * 4);
        GL45C.glGetNamedBufferSubData(handle, 0, bytes);

        float[] result = new float[size];
        bytes.asFloatBuffer().get(result);
        return result;
    }

    public static Matrix4f toMatrix(int handle) {
        ByteBuffer bytes = BufferUtils.createByteBuffer(64);
        GL45C.glGetNamedBufferSubData(handle, 0, bytes);

        FloatBuffer floats = bytes.asFloatBuffer();
//        float[] result = new float[floats.remaining()];
//        floats.get(result);
//        List<Float> list = new ArrayList<>();
//        for (int i = 0; i < 16; i++)
//            list.add(result[i]);
//        ModMain.LOGGER.info(String.join(",", list.stream().map(Object::toString).toList()));
        return new Matrix4f(floats).transpose();
    }
}