package com.zergatul.cheatutils.configs;

import com.zergatul.cheatutils.utils.MathUtils;

public class SchematicaConfig implements ValidatableConfig {
    public boolean enabled;
    public boolean showMissingBlockGhosts;
    public double missingBlockGhostsMaxDistance;
    public boolean showMissingBlockTracers;
    public double missingBlockTracersMaxDistance;
    public boolean showMissingBlockCubes;
    public double missingBlockCubesMaxDistance;
    //public boolean showWrongBlocks;
    public boolean autoBuild;
    public double autoBuildDistance;

    public SchematicaConfig() {
        showMissingBlockGhosts = true;
        missingBlockGhostsMaxDistance = 10;

        showMissingBlockTracers = false;
        missingBlockTracersMaxDistance = 30;

        showMissingBlockCubes = true;
        missingBlockCubesMaxDistance = 100;

        autoBuildDistance = 5;
    }

    @Override
    public void validate() {
        missingBlockGhostsMaxDistance = MathUtils.clamp(missingBlockGhostsMaxDistance, 1, 1000);
        missingBlockTracersMaxDistance = MathUtils.clamp(missingBlockTracersMaxDistance, 1, 1000);
        missingBlockCubesMaxDistance = MathUtils.clamp(missingBlockCubesMaxDistance, 1, 1000);
        autoBuildDistance = MathUtils.clamp(autoBuildDistance, 1, 10);
    }
}