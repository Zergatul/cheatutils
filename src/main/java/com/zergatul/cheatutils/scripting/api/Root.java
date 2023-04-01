package com.zergatul.cheatutils.scripting.api;

import com.zergatul.cheatutils.scripting.api.modules.*;

public class Root {

    public static MainApi main = new MainApi();

    // automation
    public static AutoDisconnectApi autoDisconnect = new AutoDisconnectApi();
    public static AutoDropApi autoDrop = new AutoDropApi();
    public static AutoEatApi autoEat = new AutoEatApi();
    public static ContainerButtonsApi containerButtons = new ContainerButtonsApi();

    // ESP
    public static BlocksApi blocks = new BlocksApi();
    public static EntitiesApi entities = new EntitiesApi();
    public static FreeCamApi freeCam = new FreeCamApi();

    // hacks
    public static AutoCriticalsApi autoCriticals = new AutoCriticalsApi();
    public static FakeLagApi fakeLag = new FakeLagApi();
    public static FastBreakApi fastBreak = new FastBreakApi();
    public static FlyHackApi flyHack = new FlyHackApi();
    public static KillAuraApi killAura = new KillAuraApi();
    public static MovementApi movement = new MovementApi();
    public static NoFallApi noFall = new NoFallApi();
    public static ScaffoldApi scaffold = new ScaffoldApi();
    public static TeleportApi teleport = new TeleportApi();

    // visuals
    public static ArmorOverlayApi armorOverlay = new ArmorOverlayApi();
    public static ExplorationMiniMapApi explorationMiniMap = new ExplorationMiniMapApi();
    public static ShulkerTooltipApi shulkerTooltip = new ShulkerTooltipApi();
    public static ZoomApi zoom = new ZoomApi();

    // utility
    public static LockInputsApi lockInputs = new LockInputsApi();
    public static TpsApi tps = new TpsApi();

    // others
    public static GameApi game = new GameApi();
    public static InputApi input = new InputApi();
    public static KeysApi keys = new KeysApi();
    public static PlayerApi player = new PlayerApi();
    public static InventoryApi inventory = new InventoryApi();
    public static VariablesApi variables = new VariablesApi();
    public static MathApi math = new MathApi();
    public static ColorApi color = new ColorApi();
    public static TimeApi time = new TimeApi();
    public static ConvertApi convert = new ConvertApi();
}