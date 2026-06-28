package com.zergatul.cheatutils.scripting;

public enum ApiType {

    OVERLAY,

    // update module configs or some UI tweaks
    UPDATE,

    // in-game action leading to interaction with server
    ACTION,

    BLOCK_AUTOMATION,

    VILLAGER_ROLLER,

    LOGGING,

    EVENTS,

    CURRENT_BLOCK_ESP,

    CURRENT_ENTITY_ESP,

    EXEC_LOGGING,
}