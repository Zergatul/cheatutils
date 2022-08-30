package com.zergatul.cheatutils.interfaces;

import io.netty.channel.Channel;

public interface ClientConnectionMixinInterface {
    Channel getChannel();
}