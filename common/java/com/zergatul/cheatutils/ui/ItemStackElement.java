package com.zergatul.cheatutils.ui;

import com.zergatul.cheatutils.render.CacheItemRenderer;
import com.zergatul.cheatutils.render.Position2dTextureColorRenderer;
import com.zergatul.cheatutils.render.CustomizableVanillaFontRenderer;
import com.zergatul.cheatutils.render.buffers.RenderBuffers;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

public class ItemStackElement implements Element {

    private final @Nullable ItemOwner owner;
    private final ItemStack itemStack;
    private int measuredWidth, measuredHeight;
    private int x, y;

    public ItemStackElement(@Nullable ItemOwner owner, ItemStack itemStack) {
        this.owner = owner;
        this.itemStack = itemStack;
    }

    @Override
    public void measure(RenderingContext context) {
        this.measuredWidth = 16 * context.getItemScale();
        this.measuredHeight = 16 * context.getItemScale();
    }

    @Override
    public int getMeasuredWidth() {
        return measuredWidth;
    }

    @Override
    public int getMeasuredHeight() {
        return measuredHeight;
    }

    @Override
    public void layout(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
    }

    @Override
    public void render(RenderingContext context) {
        CacheItemRenderer.AtlasSlot slot = CacheItemRenderer.instance.getTexture(owner, itemStack);
        if (slot == null) {
            return;
        }

        Position2dTextureColorRenderer.BufferBuilder buffer = context.getBuffers().getTexColor2d(RenderBuffers.ITEMS, slot.textureView());
        int w = measuredWidth;
        int h = measuredHeight;
        buffer.quad(
                x, y, slot.u0(), slot.v0(),
                x, y + h, slot.u0(), slot.v1(),
                x + w, y + h, slot.u1(), slot.v1(),
                x + w, y, slot.u1(), slot.v0(),
                -1);

        // copied from GuiGraphics.renderItemCount
        if (itemStack.getCount() != 1) {
            String amount = String.valueOf(itemStack.getCount());
            Font font = Minecraft.getInstance().font;
            CustomizableVanillaFontRenderer.instance.render(
                    font,
                    context.getItemScale(),
                    context.getBuffers(),
                    amount,
                    x + context.getItemScale() * (19 - 2 - font.width(amount)),
                    y + context.getItemScale() * (6 + 3));
        }
    }
}