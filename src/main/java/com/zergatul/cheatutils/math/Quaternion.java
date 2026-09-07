package com.zergatul.cheatutils.math;

public class Quaternion {

    public float i;
    public float j;
    public float k;
    public float r;

    public Quaternion(float p_i48100_1_, float p_i48100_2_, float p_i48100_3_, float p_i48100_4_) {
        this.i = p_i48100_1_;
        this.j = p_i48100_2_;
        this.k = p_i48100_3_;
        this.r = p_i48100_4_;
    }

    public Quaternion(Vector3f p_i48101_1_, float p_i48101_2_, boolean p_i48101_3_) {
        if (p_i48101_3_) {
            p_i48101_2_ *= ((float)Math.PI / 180F);
        }

        float f = sin(p_i48101_2_ / 2.0F);
        this.i = p_i48101_1_.x * f;
        this.j = p_i48101_1_.y * f;
        this.k = p_i48101_1_.z * f;
        this.r = cos(p_i48101_2_ / 2.0F);
    }

    public Quaternion(Quaternion p_i48103_1_) {
        this.i = p_i48103_1_.i;
        this.j = p_i48103_1_.j;
        this.k = p_i48103_1_.k;
        this.r = p_i48103_1_.r;
    }

    public void set(float p_227066_1_, float p_227066_2_, float p_227066_3_, float p_227066_4_) {
        this.i = p_227066_1_;
        this.j = p_227066_2_;
        this.k = p_227066_3_;
        this.r = p_227066_4_;
    }

    public void mul(Quaternion p_195890_1_) {
        float f = this.i;
        float f1 = this.j;
        float f2 = this.k;
        float f3 = this.r;
        float f4 = p_195890_1_.i;
        float f5 = p_195890_1_.j;
        float f6 = p_195890_1_.k;
        float f7 = p_195890_1_.r;
        this.i = f3 * f4 + f * f7 + f1 * f6 - f2 * f5;
        this.j = f3 * f5 - f * f6 + f1 * f7 + f2 * f4;
        this.k = f3 * f6 + f * f5 - f1 * f4 + f2 * f7;
        this.r = f3 * f7 - f * f4 - f1 * f5 - f2 * f6;
    }

    public void conj() {
        this.i = -this.i;
        this.j = -this.j;
        this.k = -this.k;
    }

    private static float cos(float p_214904_0_) {
        return (float)Math.cos((double)p_214904_0_);
    }

    private static float sin(float p_214903_0_) {
        return (float)Math.sin((double)p_214903_0_);
    }
}
