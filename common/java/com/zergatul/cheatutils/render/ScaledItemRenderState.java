package com.zergatul.cheatutils.render;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.state.GuiItemRenderState;
import net.minecraft.client.gui.render.state.pip.PictureInPictureRenderState;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record ScaledItemRenderState(GuiItemRenderState item, int itemScale) implements PictureInPictureRenderState {

    public ScreenRectangle textureBounds() {
        ScreenRectangle oversized = item.oversizedItemBounds();
        return oversized != null ? oversized : new ScreenRectangle(item.x(), item.y(), 16, 16);
    }

    @Override
    public int x0() {
        return textureBounds().left();
    }

    @Override
    public int y0() {
        return textureBounds().top();
    }

    @Override
    public int x1() {
        return textureBounds().right();
    }

    @Override
    public int y1() {
        return textureBounds().bottom();
    }

    @Override
    public float scale() {
        return 16;
    }

    @Override
    public Matrix3x2f pose() {
        return item.pose();
    }

    @Override
    public @Nullable ScreenRectangle scissorArea() {
        return item.scissorArea();
    }

    @Override
    public @Nullable ScreenRectangle bounds() {
        return item.bounds();
    }
}