package com.zergatul.cheatutils.scripting.api.overlay;

import com.zergatul.cheatutils.scripting.api.Variables;

public class Root {
    public static MainApi main = new MainApi();
    public static Variables variables = Variables.instance;
    public static GameApi game = new GameApi();
    public static BlocksApi blocks = new BlocksApi();
    public static EntitiesApi entities = new EntitiesApi();
    public static FreeCamApi freeCam = new FreeCamApi();
    public static ScaffoldApi scaffold = new ScaffoldApi();
    public static FlyHackApi flyHack = new FlyHackApi();
    public static KillAuraApi killAura = new KillAuraApi();
    public static MovementApi movement = new MovementApi();
    public static NoFallApi noFall = new NoFallApi();
    public static AutoCriticalsApi autoCriticals = new AutoCriticalsApi();
}