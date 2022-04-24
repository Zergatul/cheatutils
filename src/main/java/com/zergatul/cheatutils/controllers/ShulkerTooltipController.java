package com.zergatul.cheatutils.controllers;

import com.mojang.math.Matrix4f;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

// TODO: optimize
public class ShulkerTooltipController {

    public static ShulkerTooltipController instance = new ShulkerTooltipController();

    private static final ResourceLocation CONTAINER_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");
    private static final int ImageWidth = 176;
    private static final int ImageHeight = 166;

    private boolean active = true;
    private boolean locked;
    private ItemStack lockedStack;
    private Matrix4f lockedMatrix;
    private int lockedX, lockedY;
    private boolean allowTooltipOnce;

    private ShulkerTooltipController() {

    }

    public boolean isActive() {
        return active;
    }

    public void disable() {
        active = false;
    }

    public boolean isShulkerBox(ItemStack itemStack) {
        if (itemStack.getItem() instanceof BlockItem) {
            Block block = (((BlockItem)itemStack.getItem())).getBlock();
            if (block instanceof ShulkerBoxBlock) {
                return true;
            }
        }

        return false;
    }

    @SubscribeEvent
    public void onPreRenderTooltip(RenderTooltipEvent.Pre event) {
        if (locked && !allowTooltipOnce) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRenderTooltipPostText(RenderTooltipEvent.GatherComponents event) {

        if (locked && allowTooltipOnce) {
            allowTooltipOnce = false;
            return;
        }

        if (!isActive()) {
            clearLocked();
            return;
        }

        if (!isShulkerBox(event.getItemStack())) {
            clearLocked();
            return;
        }

        /*int x, y;
        x = event.getX() - ImageWidth - 24;
        y = event.getY() - 4;
        if (Screen.hasControlDown()) {
            locked = true;
            lockedMatrix = event.getMatrixStack().last().pose();
            lockedStack = event.getStack();
            lockedX = x;
            lockedY = y;
        }

        renderShulkerInventory(event.getStack(), event.getMatrixStack().last().pose(), x, y);*/
    }

    /*@SubscribeEvent
    public void onContainerDrawForeground(GuiContainerEvent.DrawForeground event) {
        if (locked) {
            if (Screen.hasControlDown()) {
                GL11.glPushMatrix();
                GL11.glLoadIdentity();
                GL11.glTranslatef(0, 0, -2000);
                renderShulkerInventory(lockedStack, lockedMatrix, lockedX, lockedY);
                renderTooltip(lockedX, lockedY, event.getMouseX(), event.getMouseY());
                GL11.glPopMatrix();
            } else {
                clearLocked();
            }
        }
    }

    private void renderShulkerInventory(ItemStack itemStack, Matrix4f matrix, int x, int y) {

        RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        Minecraft.getInstance().getTextureManager().bind(CONTAINER_TEXTURE);

        drawTexture(matrix, x, y, ImageWidth, 6, 100, 0, 0, ImageWidth, 6, 256, 256);
        drawTexture(matrix, x, y + 6, ImageWidth, 60, 100, 0, 14, ImageWidth, 60, 256, 256);
        drawTexture(matrix, x, y + 66, ImageWidth, 6, 100, 0, 160, ImageWidth, 6, 256, 256);

        CompoundNBT compoundnbt = itemStack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            if (compoundnbt.contains("Items", 9)) {
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                for (int i = 0; i < nonnulllist.size(); i++) {
                    ItemStack slot = nonnulllist.get(i);
                    int slotx = i % 9;
                    int sloty = i / 9;
                    if (!slot.isEmpty()) {
                        renderSlot(slot, x + 8 + 18 * slotx, y + 10 + 18 * sloty);
                    }
                }
            }
        }
    }

    private void renderTooltip(int x, int y, int mouseX, int mouseY) {

        Screen screen = Minecraft.getInstance().screen;

        CompoundNBT compoundnbt = lockedStack.getTagElement("BlockEntityTag");
        if (compoundnbt != null) {
            if (compoundnbt.contains("Items", 9)) {
                NonNullList<ItemStack> nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
                ItemStackHelper.loadAllItems(compoundnbt, nonnulllist);
                for (int i = 0; i < nonnulllist.size(); i++) {
                    ItemStack slot = nonnulllist.get(i);
                    int slotx = i % 9;
                    int sloty = i / 9;
                    if (!slot.isEmpty()) {
                        if (x + 8 + 18 * slotx <= mouseX && mouseX < x + 8 + 18 * slotx + 16) {
                            if (y + 10 + 18 * sloty <= mouseY && mouseY < y + 10 + 18 * sloty + 16) {
                                allowTooltipOnce = true;
                                FontRenderer font = slot.getItem().getFontRenderer(slot);
                                net.minecraftforge.fml.client.gui.GuiUtils.preItemToolTip(slot);
                                screen.renderWrappedToolTip(new MatrixStack(), screen.getTooltipFromItem(slot), mouseX, mouseY, (font == null ? Minecraft.getInstance().font : font));
                                net.minecraftforge.fml.client.gui.GuiUtils.postItemToolTip();
                            }
                        }
                    }
                }
            }
        }
    }

    private void drawTexture(Matrix4f matrix, int x, int y, int width, int height, int z, int texX, int texY, int texWidth, int texHeight, int texSizeX, int texSizeY) {
        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuilder();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.vertex(matrix, x, y, z).uv(1F * texX / texSizeX, 1F * texY / texSizeY).endVertex();
        bufferbuilder.vertex(matrix, x, y + height, z).uv(1F * texX / texSizeX, 1F * (texY + texHeight) / texSizeY).endVertex();
        bufferbuilder.vertex(matrix, x + width, y + height, z).uv(1F * (texX + texWidth) / texSizeX, 1F * (texY + texHeight) / texSizeY).endVertex();
        bufferbuilder.vertex(matrix, x + width, y, z).uv(1F * (texX + texWidth) / texSizeX, 1F * texY / texSizeY).endVertex();
        bufferbuilder.end();
        RenderSystem.enableAlphaTest();
        WorldVertexBufferUploader.end(bufferbuilder);
    }

    private void renderSlot(ItemStack itemStack, int x, int y) {
        ItemRenderer itemRenderer = Minecraft.getInstance().getItemRenderer();
        RenderSystem.enableDepthTest();
        itemRenderer.blitOffset += 150; // I have no idea why 150, found after a lot of experimentation
        itemRenderer.renderAndDecorateItem(Minecraft.getInstance().player, itemStack, x, y);
        itemRenderer.renderGuiItemDecorations(Minecraft.getInstance().font, itemStack, x, y, null);
        itemRenderer.blitOffset -= 150;
    }*/

    private void clearLocked() {
        locked = false;
        lockedMatrix = null;
        lockedStack = null;
    }

}
