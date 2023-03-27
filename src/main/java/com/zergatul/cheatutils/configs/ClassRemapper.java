package com.zergatul.cheatutils.configs;

import net.fabricmc.loader.api.FabricLoader;

import java.util.HashMap;
import java.util.Map;

public class ClassRemapper {

    private static final boolean enabled = !FabricLoader.getInstance().getMappingResolver().getCurrentRuntimeNamespace().equals("named");
    private static final Map<String, String> obfToNorm = new HashMap<>();
    private static final Map<String, String> normToObf = new HashMap<>();

    private static String[] mappings = new String[] {
            "net.minecraft.class_1915:net.minecraft.village.Merchant",
            "net.minecraft.class_2615:net.minecraft.block.entity.Hopper",
            "net.minecraft.class_5718$class_5719:net.minecraft.world.event.listener.VibrationListener$Callback",
            "net.minecraft.class_1265:net.minecraft.inventory.InventoryChangedListener",
            "net.minecraft.class_1296:net.minecraft.entity.passive.PassiveEntity",
            "net.minecraft.class_1295:net.minecraft.entity.AreaEffectCloudEntity",
            "net.minecraft.class_8113:net.minecraft.entity.decoration.DisplayEntity",
            "net.minecraft.class_8113$class_8115:net.minecraft.entity.decoration.DisplayEntity$BlockDisplayEntity",
            "net.minecraft.class_8113$class_8122:net.minecraft.entity.decoration.DisplayEntity$ItemDisplayEntity",
            "net.minecraft.class_8113$class_8123:net.minecraft.entity.decoration.DisplayEntity$TextDisplayEntity",
            "net.minecraft.class_8149:net.minecraft.entity.Attackable",
            "net.minecraft.class_1297:net.minecraft.entity.Entity",
            "net.minecraft.class_1307:net.minecraft.entity.mob.FlyingEntity",
            "net.minecraft.class_1303:net.minecraft.entity.ExperienceOrbEntity",
            "net.minecraft.class_7248:net.minecraft.entity.RideableInventory",
            "net.minecraft.class_5776:net.minecraft.entity.passive.GlowSquidEntity",
            "net.minecraft.class_8150:net.minecraft.entity.decoration.InteractionEntity",
            "net.minecraft.class_4981:net.minecraft.entity.ItemSteerable",
            "net.minecraft.class_1538:net.minecraft.entity.LightningEntity",
            "net.minecraft.class_6375:net.minecraft.entity.AngledModelEntity",
            "net.minecraft.class_6335:net.minecraft.entity.MarkerEntity",
            "net.minecraft.class_1309:net.minecraft.entity.LivingEntity",
            "net.minecraft.class_1308:net.minecraft.entity.mob.MobEntity",
            "net.minecraft.class_6025:net.minecraft.entity.Tameable",
            "net.minecraft.class_5354:net.minecraft.entity.mob.Angerable",
            "net.minecraft.class_1314:net.minecraft.entity.mob.PathAwareEntity",
            "net.minecraft.class_1316:net.minecraft.entity.JumpingMount",
            "net.minecraft.class_4582:net.minecraft.client.render.entity.feature.SkinOverlayOwner",
            "net.minecraft.class_4094:net.minecraft.entity.InteractionObserver",
            "net.minecraft.class_5147:net.minecraft.entity.Shearable",
            "net.minecraft.class_5146:net.minecraft.entity.Saddleable",
            "net.minecraft.class_1321:net.minecraft.entity.passive.TameableEntity",
            "net.minecraft.class_8046:net.minecraft.entity.Ownable",
            "net.minecraft.class_8152:net.minecraft.entity.Targeter",
            "net.minecraft.class_7988:net.minecraft.entity.VariantHolder",
            "net.minecraft.class_1420:net.minecraft.entity.passive.BatEntity",
            "net.minecraft.class_1421:net.minecraft.entity.mob.AmbientEntity",
            "net.minecraft.class_1422:net.minecraft.entity.passive.FishEntity",
            "net.minecraft.class_1425:net.minecraft.entity.passive.SchoolingFishEntity",
            "net.minecraft.class_1427:net.minecraft.entity.passive.GolemEntity",
            "net.minecraft.class_4466:net.minecraft.entity.passive.BeeEntity",
            "net.minecraft.class_1429:net.minecraft.entity.passive.AnimalEntity",
            "net.minecraft.class_1451:net.minecraft.entity.passive.CatEntity",
            "net.minecraft.class_5761:net.minecraft.entity.Bucketable",
            "net.minecraft.class_1428:net.minecraft.entity.passive.ChickenEntity",
            "net.minecraft.class_1430:net.minecraft.entity.passive.CowEntity",
            "net.minecraft.class_1431:net.minecraft.entity.passive.CodEntity",
            "net.minecraft.class_1432:net.minecraft.entity.Flutterer",
            "net.minecraft.class_1433:net.minecraft.entity.passive.DolphinEntity",
            "net.minecraft.class_4019:net.minecraft.entity.passive.FoxEntity",
            "net.minecraft.class_1438:net.minecraft.entity.passive.MooshroomEntity",
            "net.minecraft.class_1439:net.minecraft.entity.passive.IronGolemEntity",
            "net.minecraft.class_1440:net.minecraft.entity.passive.PandaEntity",
            "net.minecraft.class_3701:net.minecraft.entity.passive.OcelotEntity",
            "net.minecraft.class_1453:net.minecraft.entity.passive.ParrotEntity",
            "net.minecraft.class_1456:net.minecraft.entity.passive.PolarBearEntity",
            "net.minecraft.class_1452:net.minecraft.entity.passive.PigEntity",
            "net.minecraft.class_1463:net.minecraft.entity.passive.RabbitEntity",
            "net.minecraft.class_1454:net.minecraft.entity.passive.PufferfishEntity",
            "net.minecraft.class_1472:net.minecraft.entity.passive.SheepEntity",
            "net.minecraft.class_1462:net.minecraft.entity.passive.SalmonEntity",
            "net.minecraft.class_1473:net.minecraft.entity.passive.SnowGolemEntity",
            "net.minecraft.class_1471:net.minecraft.entity.passive.TameableShoulderEntity",
            "net.minecraft.class_1474:net.minecraft.entity.passive.TropicalFishEntity",
            "net.minecraft.class_1477:net.minecraft.entity.passive.SquidEntity",
            "net.minecraft.class_1480:net.minecraft.entity.mob.WaterCreatureEntity",
            "net.minecraft.class_1481:net.minecraft.entity.passive.TurtleEntity",
            "net.minecraft.class_7298:net.minecraft.entity.passive.AllayEntity",
            "net.minecraft.class_1493:net.minecraft.entity.passive.WolfEntity",
            "net.minecraft.class_5762:net.minecraft.entity.passive.AxolotlEntity",
            "net.minecraft.class_7102:net.minecraft.entity.passive.FrogEntity",
            "net.minecraft.class_7110:net.minecraft.entity.passive.TadpoleEntity",
            "net.minecraft.class_6053:net.minecraft.entity.passive.GoatEntity",
            "net.minecraft.class_1496:net.minecraft.entity.passive.AbstractHorseEntity",
            "net.minecraft.class_1492:net.minecraft.entity.passive.AbstractDonkeyEntity",
            "net.minecraft.class_1498:net.minecraft.entity.passive.HorseEntity",
            "net.minecraft.class_1495:net.minecraft.entity.passive.DonkeyEntity",
            "net.minecraft.class_1501:net.minecraft.entity.passive.LlamaEntity",
            "net.minecraft.class_1506:net.minecraft.entity.mob.SkeletonHorseEntity",
            "net.minecraft.class_1500:net.minecraft.entity.passive.MuleEntity",
            "net.minecraft.class_3986:net.minecraft.entity.passive.TraderLlamaEntity",
            "net.minecraft.class_1507:net.minecraft.entity.mob.ZombieHorseEntity",
            "net.minecraft.class_1511:net.minecraft.entity.decoration.EndCrystalEntity",
            "net.minecraft.class_1510:net.minecraft.entity.boss.dragon.EnderDragonEntity",
            "net.minecraft.class_1528:net.minecraft.entity.boss.WitherEntity",
            "net.minecraft.class_1531:net.minecraft.entity.decoration.ArmorStandEntity",
            "net.minecraft.class_1530:net.minecraft.entity.decoration.AbstractDecorationEntity",
            "net.minecraft.class_5915:net.minecraft.entity.decoration.GlowItemFrameEntity",
            "net.minecraft.class_1532:net.minecraft.entity.decoration.LeashKnotEntity",
            "net.minecraft.class_1533:net.minecraft.entity.decoration.ItemFrameEntity",
            "net.minecraft.class_1534:net.minecraft.entity.decoration.painting.PaintingEntity",
            "net.minecraft.class_1540:net.minecraft.entity.FallingBlockEntity",
            "net.minecraft.class_1541:net.minecraft.entity.TntEntity",
            "net.minecraft.class_1542:net.minecraft.entity.ItemEntity",
            "net.minecraft.class_1543:net.minecraft.entity.mob.IllagerEntity",
            "net.minecraft.class_1545:net.minecraft.entity.mob.BlazeEntity",
            "net.minecraft.class_1547:net.minecraft.entity.mob.AbstractSkeletonEntity",
            "net.minecraft.class_1548:net.minecraft.entity.mob.CreeperEntity",
            "net.minecraft.class_1549:net.minecraft.entity.mob.CaveSpiderEntity",
            "net.minecraft.class_1551:net.minecraft.entity.mob.DrownedEntity",
            "net.minecraft.class_3745:net.minecraft.entity.CrossbowUser",
            "net.minecraft.class_1560:net.minecraft.entity.mob.EndermanEntity",
            "net.minecraft.class_1550:net.minecraft.entity.mob.ElderGuardianEntity",
            "net.minecraft.class_1569:net.minecraft.entity.mob.Monster",
            "net.minecraft.class_1559:net.minecraft.entity.mob.EndermiteEntity",
            "net.minecraft.class_1571:net.minecraft.entity.mob.GhastEntity",
            "net.minecraft.class_1564:net.minecraft.entity.mob.EvokerEntity",
            "net.minecraft.class_1577:net.minecraft.entity.mob.GuardianEntity",
            "net.minecraft.class_1570:net.minecraft.entity.mob.GiantEntity",
            "net.minecraft.class_1581:net.minecraft.entity.mob.IllusionerEntity",
            "net.minecraft.class_1576:net.minecraft.entity.mob.HuskEntity",
            "net.minecraft.class_1589:net.minecraft.entity.mob.MagmaCubeEntity",
            "net.minecraft.class_3732:net.minecraft.entity.mob.PatrolEntity",
            "net.minecraft.class_1588:net.minecraft.entity.mob.HostileEntity",
            "net.minecraft.class_1604:net.minecraft.entity.mob.PillagerEntity",
            "net.minecraft.class_1593:net.minecraft.entity.mob.PhantomEntity",
            "net.minecraft.class_1584:net.minecraft.entity.mob.RavagerEntity",
            "net.minecraft.class_1603:net.minecraft.entity.ai.RangedAttackMob",
            "net.minecraft.class_1614:net.minecraft.entity.mob.SilverfishEntity",
            "net.minecraft.class_1606:net.minecraft.entity.mob.ShulkerEntity",
            "net.minecraft.class_1621:net.minecraft.entity.mob.SlimeEntity",
            "net.minecraft.class_1613:net.minecraft.entity.mob.SkeletonEntity",
            "net.minecraft.class_1628:net.minecraft.entity.mob.SpiderEntity",
            "net.minecraft.class_1617:net.minecraft.entity.mob.SpellcastingIllagerEntity",
            "net.minecraft.class_4985:net.minecraft.entity.passive.StriderEntity",
            "net.minecraft.class_1627:net.minecraft.entity.mob.StrayEntity",
            "net.minecraft.class_1632:net.minecraft.entity.mob.VindicatorEntity",
            "net.minecraft.class_1634:net.minecraft.entity.mob.VexEntity",
            "net.minecraft.class_1639:net.minecraft.entity.mob.WitherSkeletonEntity",
            "net.minecraft.class_1640:net.minecraft.entity.mob.WitchEntity",
            "net.minecraft.class_1642:net.minecraft.entity.mob.ZombieEntity",
            "net.minecraft.class_5136:net.minecraft.entity.mob.ZoglinEntity",
            "net.minecraft.class_1590:net.minecraft.entity.mob.ZombifiedPiglinEntity",
            "net.minecraft.class_1641:net.minecraft.entity.mob.ZombieVillagerEntity",
            "net.minecraft.class_4760:net.minecraft.entity.mob.HoglinEntity",
            "net.minecraft.class_5137:net.minecraft.entity.mob.Hoglin",
            "net.minecraft.class_4836:net.minecraft.entity.mob.PiglinEntity",
            "net.minecraft.class_5418:net.minecraft.entity.mob.AbstractPiglinEntity",
            "net.minecraft.class_5419:net.minecraft.entity.mob.PiglinBruteEntity",
            "net.minecraft.class_7260:net.minecraft.entity.mob.WardenEntity",
            "net.minecraft.class_3988:net.minecraft.entity.passive.MerchantEntity",
            "net.minecraft.class_1655:net.minecraft.entity.Npc",
            "net.minecraft.class_6067:net.minecraft.entity.InventoryOwner",
            "net.minecraft.class_1646:net.minecraft.entity.passive.VillagerEntity",
            "net.minecraft.class_3851:net.minecraft.village.VillagerDataContainer",
            "net.minecraft.class_3989:net.minecraft.entity.passive.WanderingTraderEntity",
            "net.minecraft.class_1657:net.minecraft.entity.player.PlayerEntity",
            "net.minecraft.class_1668:net.minecraft.entity.projectile.ExplosiveProjectileEntity",
            "net.minecraft.class_1665:net.minecraft.entity.projectile.PersistentProjectileEntity",
            "net.minecraft.class_1670:net.minecraft.entity.projectile.DragonFireballEntity",
            "net.minecraft.class_1667:net.minecraft.entity.projectile.ArrowEntity",
            "net.minecraft.class_1672:net.minecraft.entity.EyeOfEnderEntity",
            "net.minecraft.class_1669:net.minecraft.entity.mob.EvokerFangsEntity",
            "net.minecraft.class_1671:net.minecraft.entity.projectile.FireworkRocketEntity",
            "net.minecraft.class_3855:net.minecraft.entity.projectile.AbstractFireballEntity",
            "net.minecraft.class_1536:net.minecraft.entity.projectile.FishingBobberEntity",
            "net.minecraft.class_1674:net.minecraft.entity.projectile.FireballEntity",
            "net.minecraft.class_3856:net.minecraft.entity.FlyingItemEntity",
            "net.minecraft.class_1676:net.minecraft.entity.projectile.ProjectileEntity",
            "net.minecraft.class_1673:net.minecraft.entity.projectile.LlamaSpitEntity",
            "net.minecraft.class_1678:net.minecraft.entity.projectile.ShulkerBulletEntity",
            "net.minecraft.class_1680:net.minecraft.entity.projectile.thrown.SnowballEntity",
            "net.minecraft.class_1677:net.minecraft.entity.projectile.SmallFireballEntity",
            "net.minecraft.class_3857:net.minecraft.entity.projectile.thrown.ThrownItemEntity",
            "net.minecraft.class_1679:net.minecraft.entity.projectile.SpectralArrowEntity",
            "net.minecraft.class_1681:net.minecraft.entity.projectile.thrown.EggEntity",
            "net.minecraft.class_1682:net.minecraft.entity.projectile.thrown.ThrownEntity",
            "net.minecraft.class_1683:net.minecraft.entity.projectile.thrown.ExperienceBottleEntity",
            "net.minecraft.class_1684:net.minecraft.entity.projectile.thrown.EnderPearlEntity",
            "net.minecraft.class_1685:net.minecraft.entity.projectile.TridentEntity",
            "net.minecraft.class_1686:net.minecraft.entity.projectile.thrown.PotionEntity",
            "net.minecraft.class_1687:net.minecraft.entity.projectile.WitherSkullEntity",
            "net.minecraft.class_3763:net.minecraft.entity.raid.RaiderEntity",
            "net.minecraft.class_1693:net.minecraft.entity.vehicle.StorageMinecartEntity",
            "net.minecraft.class_1688:net.minecraft.entity.vehicle.AbstractMinecartEntity",
            "net.minecraft.class_7264:net.minecraft.entity.vehicle.ChestBoatEntity",
            "net.minecraft.class_1690:net.minecraft.entity.vehicle.BoatEntity",
            "net.minecraft.class_7265:net.minecraft.entity.vehicle.VehicleInventory",
            "net.minecraft.class_1694:net.minecraft.entity.vehicle.ChestMinecartEntity",
            "net.minecraft.class_1695:net.minecraft.entity.vehicle.MinecartEntity",
            "net.minecraft.class_1696:net.minecraft.entity.vehicle.FurnaceMinecartEntity",
            "net.minecraft.class_1697:net.minecraft.entity.vehicle.CommandBlockMinecartEntity",
            "net.minecraft.class_1699:net.minecraft.entity.vehicle.SpawnerMinecartEntity",
            "net.minecraft.class_1700:net.minecraft.entity.vehicle.HopperMinecartEntity",
            "net.minecraft.class_1701:net.minecraft.entity.vehicle.TntMinecartEntity"
    };

    public static boolean isEnabled() {
        return enabled;
    }

    public static String fromObf(String className) {
        if (!enabled) {
            return className;
        }
        return obfToNorm.getOrDefault(className, className);
    }

    public static String toObf(String className) {
        if (!enabled) {
            return className;
        }
        return normToObf.getOrDefault(className, className);
    }

    static {
        for (String c: mappings) {
            String[] parts = c.split(":");
            String obf = parts[0];
            String norm = parts[1];
            obfToNorm.put(obf, norm);
            normToObf.put(norm, obf);
        }
    }
}