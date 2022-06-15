package com.zergatul.cheatutils.webui;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.animal.horse.*;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.decoration.*;
import net.minecraft.world.entity.item.*;
import net.minecraft.world.entity.monster.*;
import net.minecraft.world.entity.monster.hoglin.*;
import net.minecraft.world.entity.monster.piglin.*;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.npc.*;
import net.minecraft.world.entity.player.*;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.entity.vehicle.*;
import org.apache.http.MethodNotSupportedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class EntityInfoApi extends ApiBase {

    private static final Class[] classes = new Class[] {
        // base classes
        LivingEntity.class,
        Animal.class,
        PathfinderMob.class,
        AgeableMob.class,
        Mob.class,
        Monster.class,
        Entity.class,
        FlyingMob.class,
        AbstractMinecart.class,
        AbstractVillager.class,
        AbstractChestedHorse.class,
        AbstractHorse.class,
        AbstractMinecartContainer.class,
        AbstractIllager.class,
        AbstractPiglin.class,
        TamableAnimal.class,
        Raider.class,
        Player.class,

        // final classes
        ArmorStand.class,
        Bee.class,
        Blaze.class,
        Boat.class,
        Cat.class,
        CaveSpider.class,
        Chicken.class,
        Cow.class,
        Creeper.class,
        Donkey.class,
        Drowned.class,
        ElderGuardian.class,
        EnderMan.class,
        Endermite.class,
        Evoker.class,
        Fox.class,
        Ghast.class,
        Guardian.class,
        Hoglin.class,
        Horse.class,
        Husk.class,
        IronGolem.class,
        ItemEntity.class,
        Llama.class,
        MagmaCube.class,
        Minecart.class,
        MinecartChest.class,
        MinecartTNT.class,
        Mule.class,
        Ocelot.class,
        Panda.class,
        Parrot.class,
        Phantom.class,
        Pig.class,
        Piglin.class,
        PiglinBrute.class,
        Pillager.class,
        PolarBear.class,
        Rabbit.class,
        Ravager.class,
        Sheep.class,
        Shulker.class,
        Skeleton.class,
        SkeletonHorse.class,
        Slime.class,
        SnowGolem.class,
        Spider.class,
        Squid.class,
        Stray.class,
        Vex.class,
        Villager.class,
        Vindicator.class,
        WanderingTrader.class,
        Witch.class,
        WitherBoss.class,
        WitherSkeleton.class,
        Wolf.class,
        Zoglin.class,
        Zombie.class,
        ZombieHorse.class,
        ZombieVillager.class,
        ZombifiedPiglin.class,
        Warden.class
    };

    @Override
    public String getRoute() {
        return "entity-info";
    }

    @Override
    public String get() throws MethodNotSupportedException {

        Object[] result = Arrays.stream(classes).map(c -> {
            try {
                return new EntityInfo(c);
            } catch (Exception e) {
                return null;
            }
        }).filter(Objects::nonNull).toArray();

        return gson.toJson(result);
    }

    private static class EntityInfo {

        public Class clazz;
        public String simpleName;
        public List<String> baseClasses;

        public EntityInfo(Class clazz) throws Exception {

            if (!Entity.class.isAssignableFrom(clazz)) {
                throw new Exception("Not supported");
            }

            this.clazz = clazz;
            this.simpleName = clazz.getSimpleName();

            this.baseClasses = new ArrayList<>();
            while (clazz != Entity.class) {
                clazz = clazz.getSuperclass();
                this.baseClasses.add(clazz.getSimpleName());
            }

        }

    }
}
