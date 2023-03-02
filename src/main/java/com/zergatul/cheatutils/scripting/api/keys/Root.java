package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.scripting.api.Variables;

public class Root {

    public static MainApi main = new MainApi();
    public static InputApi input = new InputApi();

    public static AutoDisconnectApi autoDisconnect = new AutoDisconnectApi();
    public static KillAuraApi killAura = new KillAuraApi();
    public static AutoDropApi autoDrop = new AutoDropApi();
    public static ContainerButtonsApi containerButtons = new ContainerButtonsApi();

    public static BlocksApi blocks = new BlocksApi();
    public static EntitiesApi entities = new EntitiesApi();
    public static FreeCamApi freeCam = new FreeCamApi();

    public static FlyHackApi flyHack = new FlyHackApi();
    public static MovementApi movement = new MovementApi();
    public static ScaffoldApi scaffold = new ScaffoldApi();
    public static FakeLagApi fakeLag = new FakeLagApi();
    public static NoFallApi noFall = new NoFallApi();
    public static AutoCriticalsApi autoCriticals = new AutoCriticalsApi();

    public static ArmorOverlayApi armorOverlay = new ArmorOverlayApi();
    public static ShulkerTooltipApi shulkerTooltip = new ShulkerTooltipApi();
    public static ExplorationMiniMapApi explorationMiniMap = new ExplorationMiniMapApi();
    public static ZoomApi zoom = new ZoomApi();
    public static LockInputsApi lockInputs = new LockInputsApi();

    public static Variables variables = Variables.instance;
    public static GameApi game = new GameApi();
    public static PlayerApi player = new PlayerApi();
    public static KeysApi keys = new KeysApi();
}