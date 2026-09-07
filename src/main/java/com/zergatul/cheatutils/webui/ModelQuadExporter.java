package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.mixins.accessors.ModelBoxAccessor;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

public class ModelQuadExporter {

    public static List<BlockModelApi.Quad> export(String texture, UnaryOperator<Vec3d> transform, ModelRenderer... parts) {
        List<BlockModelApi.Quad> quads = new ArrayList<>();
        for (ModelRenderer part : parts) {
            append(quads, texture, transform, part);
        }
        return quads;
    }

    private static void append(List<BlockModelApi.Quad> quads, String texture,
                               UnaryOperator<Vec3d> parentTransform, ModelRenderer part) {
        if (part.isHidden || !part.showModel) {
            return;
        }
        UnaryOperator<Vec3d> transform = v -> parentTransform.apply(transformPart(v, part));
        for (ModelBox box : part.cubeList) {
            for (TexturedQuad quad : ((ModelBoxAccessor) box).getQuadList_CU()) {
                BlockModelApi.Vertex[] vertices = new BlockModelApi.Vertex[4];
                for (int i = 0; i < 4; i++) {
                    PositionTextureVertex source = quad.vertexPositions[i];
                    Vec3d position = transform.apply(source.vector3D.scale(1.0 / 16));
                    BlockModelApi.Vertex vertex = vertices[i] = new BlockModelApi.Vertex();
                    vertex.x = (float) position.x;
                    vertex.y = (float) position.y;
                    vertex.z = (float) position.z;
                    vertex.u = source.texturePositionX;
                    vertex.v = source.texturePositionY;
                    vertex.r = vertex.g = vertex.b = vertex.a = 255;
                }
                quads.add(new BlockModelApi.Quad(texture, vertices[0], vertices[1], vertices[2], vertices[3]));
            }
        }
        if (part.childModels != null) {
            for (ModelRenderer child : part.childModels) {
                append(quads, texture, transform, child);
            }
        }
    }

    private static Vec3d transformPart(Vec3d v, ModelRenderer part) {
        // ModelRenderer.render applies T(offset) * T(pivot / 16) * Rz * Ry * Rx.
        double sin = Math.sin(part.rotateAngleX);
        double cos = Math.cos(part.rotateAngleX);
        v = new Vec3d(v.x, v.y * cos - v.z * sin, v.y * sin + v.z * cos);
        sin = Math.sin(part.rotateAngleY);
        cos = Math.cos(part.rotateAngleY);
        v = new Vec3d(v.x * cos + v.z * sin, v.y, -v.x * sin + v.z * cos);
        sin = Math.sin(part.rotateAngleZ);
        cos = Math.cos(part.rotateAngleZ);
        v = new Vec3d(v.x * cos - v.y * sin, v.x * sin + v.y * cos, v.z);
        return new Vec3d(v.x + part.offsetX + part.rotationPointX / 16.0,
                v.y + part.offsetY + part.rotationPointY / 16.0,
                v.z + part.offsetZ + part.rotationPointZ / 16.0);
    }
}