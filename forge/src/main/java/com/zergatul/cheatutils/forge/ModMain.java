package com.zergatul.cheatutils.forge;

import com.mojang.datafixers.util.Either;
import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.ModLoaderBridge;
import com.zergatul.cheatutils.common.ModLoaderBridgeInstance;
import com.zergatul.cheatutils.common.WrappedRegistry;
import com.zergatul.cheatutils.common.events.GatherTooltipComponentsEvent;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.utils.DebugScreenExtensions;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RenderTooltipEvent;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.bus.BusGroup;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.versions.forge.ForgeVersion;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Mod(Constants.MOD_ID)
public final class ModMain {

    public static final Logger LOGGER = LogManager.getLogger(ModMain.class);

    public ModMain(final FMLJavaModLoadingContext context) {
        BusGroup modBusGroup = context.getModBusGroup();

        FMLCommonSetupEvent.getBus(modBusGroup).addListener(this::onCommonSetup);
        RegisterKeyMappingsEvent.BUS.addListener(this::onRegisterKeyMappings);
        FMLLoadCompleteEvent.getBus(modBusGroup).addListener(this::onLoadComplete);

        Modules.registerKeyBindings();
        Modules.register();
        DebugScreenExtensions.register();

        ModLoaderBridgeInstance.init(new Bridge());
    }

    private void onCommonSetup(final FMLCommonSetupEvent event) {
        RenderTooltipEvent.GatherComponents.BUS.addListener(ForgeEvents::onPreRenderTooltip);
        ChunkEvent.Load.BUS.addListener(ForgeEvents::onChunkLoad);
        ChunkEvent.Unload.BUS.addListener(ForgeEvents::onChunkUnload);
    }

    private void onLoadComplete(final FMLLoadCompleteEvent event) {
        SystemFonts.initAsync();
        Profiles.instance.init();
        ConfigHttpServer.instance.start();
    }

    private void onRegisterKeyMappings(final RegisterKeyMappingsEvent event) {
        Events.RegisterKeyBindings.trigger(event::register);
    }

    private static class ForgeEvents {

        private static void onPreRenderTooltip(RenderTooltipEvent.GatherComponents event) {
            List<Component> list = new ArrayList<>();
            Events.GatherTooltipComponents.trigger(new GatherTooltipComponentsEvent(event.getItemStack(), list));
            list.forEach(c -> event.getTooltipElements().add(Either.left(c)));
        }

        private static void onChunkLoad(ChunkEvent.Load event) {
            if (event.getLevel().isClientSide()) {
                Events.RawChunkLoaded.trigger((LevelChunk) event.getChunk());
            }
        }

        private static void onChunkUnload(ChunkEvent.Unload event) {
            if (event.getLevel().isClientSide()) {
                Events.RawChunkUnloaded.trigger((LevelChunk) event.getChunk());
            }
        }
    }

    private static class Bridge implements ModLoaderBridge {

        @Override
        public WrappedRegistry<Block> getBlockRegistry() {
            return new ForgeWrappedRegistry<>(ForgeRegistries.BLOCKS);
        }

        @Override
        public WrappedRegistry<Item> getItemRegistry() {
            return new ForgeWrappedRegistry<>(ForgeRegistries.ITEMS);
        }

        @Override
        public WrappedRegistry<EntityType<?>> getEntityTypeRegistry() {
            return new ForgeWrappedRegistry<>(ForgeRegistries.ENTITY_TYPES);
        }

        @Override
        public WrappedRegistry<MobEffect> getMobEffectRegistry() {
            return new ForgeWrappedRegistry<>(ForgeRegistries.MOB_EFFECTS);
        }

        @Override
        public boolean isProduction() {
            return FMLEnvironment.production;
        }

        @Override
        public String getModLoaderName() {
            return "Forge";
        }

        @Override
        public String getModLoaderVersion() {
            return ForgeVersion.getVersion();
        }

        @Override
        public String getModVersion() {
            return ModList.getModFileById(Constants.MOD_ID).getMods().getFirst().getVersion().toString();
        }

        @Override
        public int getModCount() {
            return ModList.getMods().size();
        }
    }

    private record ForgeWrappedRegistry<T>(IForgeRegistry<T> registry) implements WrappedRegistry<T> {

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
            return registry.getValues();
        }
    }
}