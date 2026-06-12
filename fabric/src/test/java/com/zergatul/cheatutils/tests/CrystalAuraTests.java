package com.zergatul.cheatutils.tests;

import com.zergatul.cheatutils.configs.CrystalAuraConfig;
import com.zergatul.cheatutils.tests.utility.MockLevel;
import com.zergatul.cheatutils.tests.utility.MockPlayer;
import com.zergatul.cheatutils.utils.CrystalAuraDamageCalculator;
import com.zergatul.cheatutils.utils.FastCrystalAuraDamageCalculator;
import com.zergatul.cheatutils.utils.VanillaCrystalAuraDamageCalculator;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class CrystalAuraTests {

    @BeforeAll
    public static void init() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    public void zeroHitsTest1() {
        MockLevel level = MockLevel.create();
        level.platform(-64, -50, -50, 50, 50, Blocks.BEDROCK);
        level.block(20, -63, 20, Blocks.DIRT);

        MockPlayer player = new MockPlayer(level, "Tester");
        player.setPos(19.5, -63, 20.5);
        level.addFreshEntity(player);

        CrystalAuraConfig config = new CrystalAuraConfig();
        config.placeRange = 4;
        config.breakRange = 4;

        CrystalAuraDamageCalculator calculator1 = new VanillaCrystalAuraDamageCalculator();
        CrystalAuraDamageCalculator calculator2 = new FastCrystalAuraDamageCalculator();
        calculator1.begin(level, config, player.getEyePosition());
        calculator2.begin(level, config, player.getEyePosition());
        float dmg1 = calculator1.calculateEndCrystalDamage(new Vec3(22.5, -63, 20.5), player);
        float dmg2 = calculator1.calculateEndCrystalDamage(new Vec3(22.5, -63, 20.5), player);

        Assertions.assertEquals(1.5f, dmg1);
        Assertions.assertEquals(1.5f, dmg2);
    }

    @Test
    public void fewHitsTest1() {
        MockLevel level = MockLevel.create();
        level.platform(-64, -50, -50, 50, 50, Blocks.BEDROCK);
        level.block(20, -63, 20, Blocks.DIRT);

        MockPlayer player = new MockPlayer(level, "Tester");
        player.setPos(19.5, -63, 20.5);
        level.addFreshEntity(player);

        CrystalAuraConfig config = new CrystalAuraConfig();
        config.placeRange = 4;
        config.breakRange = 4;

        CrystalAuraDamageCalculator calculator1 = new VanillaCrystalAuraDamageCalculator();
        CrystalAuraDamageCalculator calculator2 = new FastCrystalAuraDamageCalculator();
        calculator1.begin(level, config, player.getEyePosition());
        calculator2.begin(level, config, player.getEyePosition());
        float dmg1 = calculator1.calculateEndCrystalDamage(new Vec3(23.5, -63, 20.5), player);
        float dmg2 = calculator1.calculateEndCrystalDamage(new Vec3(23.5, -63, 20.5), player);

        Assertions.assertEquals(4.4244447f, dmg1);
        Assertions.assertEquals(4.4244447f, dmg2);
    }

    @Test
    public void slabTest1() {
        MockLevel level = MockLevel.create();
        level.platform(-64, -50, -50, 50, 50, Blocks.BEDROCK);
        level.block(20, -63, 20, Blocks.STONE_SLAB.defaultBlockState());

        MockPlayer player = new MockPlayer(level, "Tester");
        player.setPos(19.5, -63, 20.5);
        level.addFreshEntity(player);

        CrystalAuraConfig config = new CrystalAuraConfig();
        config.placeRange = 4;
        config.breakRange = 4;

        CrystalAuraDamageCalculator calculator1 = new VanillaCrystalAuraDamageCalculator();
        CrystalAuraDamageCalculator calculator2 = new FastCrystalAuraDamageCalculator();
        calculator1.begin(level, config, player.getEyePosition());
        calculator2.begin(level, config, player.getEyePosition());
        float dmg1 = calculator1.calculateEndCrystalDamage(new Vec3(21.5, -63, 20.5), player);
        float dmg2 = calculator1.calculateEndCrystalDamage(new Vec3(21.5, -63, 20.5), player);

        Assertions.assertEquals(1.5f, dmg1);
        Assertions.assertEquals(1.5f, dmg2);
    }

    @Test
    public void slabTest2() {
        MockLevel level = MockLevel.create();
        level.platform(-64, -50, -50, 50, 50, Blocks.BEDROCK);
        level.block(20, -63, 20, Blocks.STONE_SLAB.defaultBlockState());

        MockPlayer player = new MockPlayer(level, "Tester");
        player.setPos(19.5, -63, 20.5);
        level.addFreshEntity(player);

        CrystalAuraConfig config = new CrystalAuraConfig();
        config.placeRange = 4;
        config.breakRange = 4;

        CrystalAuraDamageCalculator calculator1 = new VanillaCrystalAuraDamageCalculator();
        CrystalAuraDamageCalculator calculator2 = new FastCrystalAuraDamageCalculator();
        calculator1.begin(level, config, player.getEyePosition());
        calculator2.begin(level, config, player.getEyePosition());
        float dmg1 = calculator1.calculateEndCrystalDamage(new Vec3(22.5, -63, 20.5), player);
        float dmg2 = calculator1.calculateEndCrystalDamage(new Vec3(22.5, -63, 20.5), player);

        Assertions.assertEquals(26.070002f, dmg1);
        Assertions.assertEquals(26.070002f, dmg2);
    }
}