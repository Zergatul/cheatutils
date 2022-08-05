package com.zergatul.cheatutils.scripting.api;

public class Root {

    public static MainApi main = new MainApi();

    public static AutoDisconnectApi autoDisconnect = new AutoDisconnectApi();
    public static KillAuraApi killAura = new KillAuraApi();

    public static BlocksApi blocks = new BlocksApi();
    public static EntitiesApi entities = new EntitiesApi();
    public static FreeCamApi freeCam = new FreeCamApi();

    public static FlyHackApi flyHack = new FlyHackApi();
    public static ScaffoldApi scaffold = new ScaffoldApi();
    public static FakeLagApi fakeLag = new FakeLagApi();

    public static ArmorOverlayApi armorOverlay = new ArmorOverlayApi();
    public static ShulkerTooltipApi shulkerTooltip = new ShulkerTooltipApi();
    public static ExplorationMiniMapApi explorationMiniMap = new ExplorationMiniMapApi();
}