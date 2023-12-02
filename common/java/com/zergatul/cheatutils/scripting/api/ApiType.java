package com.zergatul.cheatutils.scripting.api;

public enum ApiType {

    OVERLAY,

    // update module configs or some UI tweaks
    UPDATE,

    // in-game action leading to interaction with server
    ACTION,

    CURRENT_BLOCK,

    BLOCK_PLACER,

    DISCONNECT,

    VILLAGER_ROLLER,

    LOGGING,

    EVENTS
}