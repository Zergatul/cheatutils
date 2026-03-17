package com.zergatul.cheatutils.render.gl;

import com.zergatul.cheatutils.ModMain;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL45C;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

public class GlBufferUtils {

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