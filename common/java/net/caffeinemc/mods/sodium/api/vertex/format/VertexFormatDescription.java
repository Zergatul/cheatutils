package net.caffeinemc.mods.sodium.api.vertex.format;

import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;

/**
 * Compile-time stub for optional Sodium integration. This interface is excluded from produced jars.
 */
public interface VertexFormatDescription {
    int getElementOffset(CommonVertexAttribute element);
    int stride();
}