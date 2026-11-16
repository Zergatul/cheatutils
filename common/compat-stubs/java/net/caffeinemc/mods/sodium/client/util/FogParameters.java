package net.caffeinemc.mods.sodium.client.util;

public record FogParameters(
        float red, float green, float blue, float alpha,
        float environmentalStart, float environmentalEnd,
        float renderStart, float renderEnd,
        float cullDistance
) {
    public FogParameters(float red, float green, float blue, float alpha, float environmentalStart, float environmentalEnd, float renderStart, float renderEnd) {
        this(red, green, blue, alpha, environmentalStart, environmentalEnd, renderStart, renderEnd, Float.isNaN(environmentalEnd) ? renderEnd : Math.min(renderEnd, environmentalEnd));
    }
}