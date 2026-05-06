package com.zergatul.cheatutils.neoforge;

import com.mojang.datafixers.util.Either;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.ModLoaderBridge;
import com.zergatul.cheatutils.common.ModLoaderBridgeInstance;
import com.zergatul.cheatutils.common.WrappedRegistry;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.*;
import com.zergatul.cheatutils.utils.DebugScreenExtensions;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.minecraft.core.DefaultedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeVersion;
import net.neoforged.neoforge.event.level.ChunkEvent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.zergatul.cheatutils.common.Events.*;

@Mod(Constants.MOD_ID)
public class ModMain {

    public ModMain(IEventBus bus, ModContainer container) {
        ModLoaderBridgeInstance.init(new Bridge());

        bus.addListener(this::onCommonSetup);
        bus.addListener(this::onLoadComplete);
        bus.addListener(this::onRegisterKeybindings);
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        Modules.register();
        NeoForge.EVENT_BUS.register(new NeoForgeEvents());
        DebugScreenExtensions.register();
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        SystemFonts.initAsync();
        Profiles.instance.init();
        ConfigHttpServer.instance.start();
    }

    private void onRegisterKeybindings(final RegisterKeyMappingsEvent event) {
        Modules.registerKeyBindings();
        Events.RegisterKeyBindings.trigger(event::register);
    }

    private static class NeoForgeEvents {

        @SubscribeEvent
        public void onPreRenderTooltip(RenderTooltipEvent.GatherComponents event) {
            List<Component> list = new ArrayList<>();
            GatherTooltipComponents.trigger(new GatherTooltipComponentsEvent(event.getItemStack(), list));
            list.forEach(c -> event.getTooltipElements().add(Either.left(c)));
        }

        @SubscribeEvent
        public void onChunkLoad(ChunkEvent.Load event) {
            if (event.getLevel().isClientSide()) {
                RawChunkLoaded.trigger(event.getChunk());
            }
        }

        @SubscribeEvent
        public void onChunkUnload(ChunkEvent.Unload event) {
            if (event.getLevel().isClientSide()) {
                RawChunkUnloaded.trigger(event.getChunk());
            }
        }
    }

    private static class Bridge implements ModLoaderBridge {

        @Override
        public WrappedRegistry<Block> getBlockRegistry() {
            return new BuiltInWrappedRegistry<>(BuiltInRegistries.BLOCK);
        }

        @Override
        public WrappedRegistry<Item> getItemRegistry() {
            return new BuiltInWrappedRegistry<>(BuiltInRegistries.ITEM);
        }

        @Override
        public WrappedRegistry<EntityType<?>> getEntityTypeRegistry() {
            return new BuiltInWrappedRegistry<>(BuiltInRegistries.ENTITY_TYPE);
        }

        @Override
        public WrappedRegistry<MobEffect> getMobEffectRegistry() {
            return new WrappedBaseRegistry<>(BuiltInRegistries.MOB_EFFECT);
        }

        @Override
        public boolean isProduction() {
            return FMLEnvironment.isProduction();
        }

        @Override
        public String getModLoaderName() {
            return "NeoForge";
        }

        @SuppressWarnings("UnstableApiUsage")
        @Override
        public String getModLoaderVersion() {
            return NeoForgeVersion.getVersion();
        }

        @Override
        public String getModVersion() {
            return ModList.get().getModFileById(Constants.MOD_ID).getMods().getFirst().getVersion().toString();
        }

        @Override
        public int getModCount() {
            return ModList.get().size();
        }
    }

    private record BuiltInWrappedRegistry<T>(DefaultedRegistry<T> registry) implements WrappedRegistry<T> {

        @Override
        public Identifier getKey(T value) {
            return registry.getKey(value);
        }

        @Override
        public T getValue(Identifier id) {
            return registry.getValue(id);
        }

        @Override
        public Collection<T> getValues() {
            return registry.keySet().stream().map(this::getValue).toList();
        }
    }

    private record WrappedBaseRegistry<T>(Registry<T> registry) implements WrappedRegistry<T> {

        @Override
        public Identifier getKey(T value) {
            return registry.getKey(value);
        }

        @Override
        public T getValue(Identifier id) {
            return registry.getValue(id);
        }

        @Override
        public Collection<T> getValues() {
            return registry.keySet().stream().map(this::getValue).toList();
        }
    }
}