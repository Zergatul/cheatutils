package com.zergatul.cheatutils.math;

public class Vector3f {

    public static Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);
    public static Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
    public static Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);
    public static Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
    public static Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);
    public static Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);

    public float x;
    public float y;
    public float z;

    public Vector3f(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void set(float x, float y, float z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Quaternion rotationDegrees(float p_229187_1_) {
        return new Quaternion(this, p_229187_1_, true);
    }

    public void transform(Quaternion p_214905_1_) {
        Quaternion quaternion = new Quaternion(p_214905_1_);
        quaternion.mul(new Quaternion(this.x, this.y, this.z, 0.0F));
        Quaternion quaternion1 = new Quaternion(p_214905_1_);
        quaternion1.conj();
        quaternion.mul(quaternion1);
        this.set(quaternion.i, quaternion.j, quaternion.k);
    }
}