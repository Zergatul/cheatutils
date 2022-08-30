package com.zergatul.cheatutils.mixins;

import com.zergatul.cheatutils.interfaces.ClientConnectionMixinInterface;
import io.netty.channel.Channel;
import net.minecraft.network.ClientConnection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientConnection.class)
public abstract class MixinClientConnection implements ClientConnectionMixinInterface {

    @Shadow
    private io.netty.channel.Channel channel;

    @Override
    public Channel getChannel() {
        return this.channel;
    }
}