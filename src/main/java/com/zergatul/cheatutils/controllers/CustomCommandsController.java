package com.zergatul.cheatutils.controllers;

import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CustomCommandsController {

    public static final CustomCommandsController instance = new CustomCommandsController();

    @SubscribeEvent
    public void onRegisterCommandsEvent(RegisterCommandsEvent event) {
        //MoveToCommand.register(event.getDispatcher());
        //LookAtCommand.register(event.getDispatcher());
        //LavaCastCommand.register(event.getDispatcher());
    }
}
