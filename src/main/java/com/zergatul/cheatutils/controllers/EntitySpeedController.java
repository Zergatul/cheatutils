package com.zergatul.cheatutils.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class EntitySpeedController {

    public static final EntitySpeedController instance = new EntitySpeedController();

    private Minecraft mc = Minecraft.getInstance();

    private EntitySpeedController() {

    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (mc.player == null) {
            return;
        }

        if (event.phase == TickEvent.Phase.START) {
            Entity vehicle = mc.player.getVehicle();
            if (vehicle == null) {
                return;
            }
            if (vehicle instanceof Boat)
            {
                Boat boat = (Boat) vehicle;
                //boat.getDeltaMovement()
                //boat.hasImpulse
            }
        }

        if (event.phase == TickEvent.Phase.END) {

        }
    }

}
