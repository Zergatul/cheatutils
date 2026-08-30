package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class SchematicaConfig extends BlockPlacerConfig implements ValidatableConfig {

    public boolean renderBlocks;
    public boolean shadeBlocks;
    public boolean showMissingBlockTracers;
    public double missingBlockTracersMaxDistance;
    public boolean showMissingBlockCubes;
    public double missingBlockCubesMaxDistance;
    public boolean showWrongBlockTracers;
    public double wrongBlockTracersMaxDistance;
    public boolean showWrongBlockCubes;
    public double wrongBlockCubesMaxDistance;
    public boolean replaceableAsAir;
    public boolean airAlwaysValid;
    public boolean autoBuild;
    public double placementRate;
    public Create create;

    public SchematicaConfig() {
        super();

        renderBlocks = true;
        shadeBlocks = true;

        showMissingBlockTracers = false;
        missingBlockTracersMaxDistance = 30;

        showMissingBlockCubes = true;
        missingBlockCubesMaxDistance = 100;

        showWrongBlockTracers = false;
        wrongBlockTracersMaxDistance = 10;

        showWrongBlockCubes = false;
        wrongBlockCubesMaxDistance = 10;

        placementRate = 1;
        create = new Create();
    }

    @Override
    public void validate() {
        missingBlockTracersMaxDistance = MathUtils.clamp(missingBlockTracersMaxDistance, 1, 1000);
        missingBlockCubesMaxDistance = MathUtils.clamp(missingBlockCubesMaxDistance, 1, 1000);
        wrongBlockTracersMaxDistance = MathUtils.clamp(wrongBlockTracersMaxDistance, 1, 1000);
        wrongBlockCubesMaxDistance = MathUtils.clamp(wrongBlockCubesMaxDistance, 1, 1000);
        placementRate = MathUtils.clamp(placementRate, 1, 100);
        super.validate();
    }

    public static class Create {

        public int x1, y1, z1, x2, y2, z2;
        public boolean enabled;

        public int getX1() {
            return Math.min(x1, x2);
        }

        public int getX2() {
            return Math.max(x1, x2) + 1;
        }

        public int getY1() {
            return Math.min(y1, y2);
        }

        public int getY2() {
            return Math.max(y1, y2) + 1;
        }

        public int getZ1() {
            return Math.min(z1, z2);
        }

        public int getZ2() {
            return Math.max(z1, z2) + 1;
        }
    }
}