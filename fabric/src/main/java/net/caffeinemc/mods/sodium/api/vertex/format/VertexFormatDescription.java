package net.caffeinemc.mods.sodium.api.vertex.format;

import net.caffeinemc.mods.sodium.api.vertex.attributes.CommonVertexAttribute;

public interface VertexFormatDescription {
    boolean containsElement(CommonVertexAttribute element);
    int getElementOffset(CommonVertexAttribute element);
    int id();
    int stride();
}