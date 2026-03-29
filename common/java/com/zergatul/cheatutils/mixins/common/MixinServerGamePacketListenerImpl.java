package com.zergatul.cheatutils.mixins.common;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.network.protocol.game.*;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.storage.TagValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerGamePacketListenerImpl.class)
public abstract class MixinServerGamePacketListenerImpl {

    @Shadow
    public ServerPlayer player;

    @Inject(at = @At("HEAD"), method = "handleChat")
    private void onHandleChat(ServerboundChatPacket packet, CallbackInfo ci) {
        if (packet.message().equals("sign")) {
            BlockPos pos = this.player.blockPosition().above(3);

            SignText text = new SignText()
                    .setMessage(0, MutableComponent.create(new TranslatableContents("button.take.all", "Fall-Back", new Object[0])))
                    .setMessage(1, MutableComponent.create(new KeybindContents("key.zergatul.cheatutils.reserved0")))
                    .setMessage(2, Component.empty())
                    .setMessage(3, Component.empty());

            TagValueOutput output = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, player.level().registryAccess());
            output.store("front_text", SignText.DIRECT_CODEC, text);
            output.store("back_text", SignText.DIRECT_CODEC, text);
            output.putBoolean("is_waxed", false);
            CompoundTag compound = output.buildResult();

            this.player.connection.send(new ClientboundBlockUpdatePacket(pos, Blocks.ACACIA_SIGN.defaultBlockState()));
//            this.player.connection.send(new ClientboundBlockEntityDataPacket(
//                    pos,
//                    BlockEntityType.SIGN,
//                    compound));
            this.player.connection.send(new ClientboundOpenSignEditorPacket(pos, true));
            this.player.connection.send(new ClientboundContainerClosePacket(1));
        }
    }
}