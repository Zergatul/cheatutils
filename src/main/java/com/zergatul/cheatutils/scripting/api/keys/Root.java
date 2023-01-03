package com.zergatul.cheatutils.scripting.api.keys;

import com.zergatul.cheatutils.configs.AutoTotemConfig;
import com.zergatul.cheatutils.configs.ContainerButtonsConfig;

public class Root {

    public static MainApi main = new MainApi();

    //public static AutoDisconnectApi autoDisconnect = new AutoDisconnectApi();
    public static KillAuraApi killAura = new KillAuraApi();
    public static AutoDropApi autoDrop = new AutoDropApi();
    public static AutoTotemApi autoTotem = new AutoTotemApi();
    public static ContainerButtonsApi containerButtons = new ContainerButtonsApi();
    public static AutoEatApi autoEat = new AutoEatApi();

    public static BlocksApi blocks = new BlocksApi();
    public static EntitiesApi entities = new EntitiesApi();
    public static FreeCamApi freeCam = new FreeCamApi();

    public static FlyHackApi flyHack = new FlyHackApi();
    public static MovementApi movement = new MovementApi();
    public static ScaffoldApi scaffold = new ScaffoldApi();
    public static FakeLagApi fakeLag = new FakeLagApi();
    public static AutoCriticalsApi autoCriticals = new AutoCriticalsApi();
    public static NoFallApi noFall = new NoFallApi();
    public static ReachApi reach = new ReachApi();

    public static ArmorOverlayApi armorOverlay = new ArmorOverlayApi();
    public static ShulkerTooltipApi shulkerTooltip = new ShulkerTooltipApi();
    public static ExplorationMiniMapApi explorationMiniMap = new ExplorationMiniMapApi();
    public static FullBrightApi fullBright = new FullBrightApi();
    public static FogApi fog = new FogApi();

    public static LockInputsApi lockInputs = new LockInputsApi();
}