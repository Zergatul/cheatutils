package com.zergatul.cheatutils.fabric;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.common.ModLoaderBridge;
import com.zergatul.cheatutils.common.ModLoaderBridgeInstance;
import com.zergatul.cheatutils.common.WrappedRegistry;
import com.zergatul.cheatutils.font.SystemFonts;
import com.zergatul.cheatutils.modules.Modules;
import com.zergatul.cheatutils.modules.utilities.Profiles;
import com.zergatul.cheatutils.utils.DebugScreenExtensions;
import com.zergatul.cheatutils.utils.ColorUtils;
import com.zergatul.cheatutils.webui.ConfigHttpServer;
import com.zergatul.cheatutils.webui.BlockModelApi;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.renderer.v1.mesh.QuadView;
import net.fabricmc.fabric.api.client.renderer.v1.render.submit.ExtendedBlockModelSubmit;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.renderer.feature.submit.SubmitNode;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import org.joml.Vector3f;

import java.util.Collection;
import java.util.List;

public class ModMain implements ClientModInitializer {

    public ModMain() {
        ModLoaderBridgeInstance.init(new Bridge());
    }

    @Override
    public void onInitializeClient() {
        setupFabricEvents();

        SystemFonts.initAsync();
        Profiles.instance.init();
        ConfigHttpServer.instance.start();
        Modules.registerKeyBindings();
        Modules.register();
        Events.RegisterKeyBindings.trigger(KeyMappingHelper::registerKeyMapping);
        DebugScreenExtensions.register();
    }

    private void setupFabricEvents() {
        ClientChunkEvents.CHUNK_LOAD.register((_, chunk) -> {
            Events.RawChunkLoaded.trigger(chunk);
        });
        ClientChunkEvents.CHUNK_UNLOAD.register((_, chunk) -> {
            Events.RawChunkUnloaded.trigger(chunk);
        });
    }

    private static class Bridge implements ModLoaderBridge {

        @Override
        public WrappedRegistry<Block> getBlockRegistry() {
            return new VanillaWrapperRegistry<>(BuiltInRegistries.BLOCK);
        }

        @Override
        public WrappedRegistry<Item> getItemRegistry() {
            return new VanillaWrapperRegistry<>(BuiltInRegistries.ITEM);
        }

        @Override
        public WrappedRegistry<EntityType<?>> getEntityTypeRegistry() {
            return new VanillaWrapperRegistry<>(BuiltInRegistries.ENTITY_TYPE);
        }

        @Override
        public WrappedRegistry<MobEffect> getMobEffectRegistry() {
            return new VanillaWrapperRegistry<>(BuiltInRegistries.MOB_EFFECT);
        }

        @Override
        public boolean isProduction() {
            return !FabricLoader.getInstance().isDevelopmentEnvironment();
        }

        @Override
        public String getModLoaderName() {
            return "Fabric";
        }

        @Override
        public String getModLoaderVersion() {
            return FabricLoader.getInstance().getModContainer("fabricloader").orElseThrow().getMetadata().getVersion().getFriendlyString();
        }

        @Override
        public String getModVersion() {
            return FabricLoader.getInstance().getModContainer(Constants.MOD_ID).orElseThrow().getMetadata().getVersion().getFriendlyString();
        }

        @Override
        public int getModCount() {
            return FabricLoader.getInstance().getAllMods().size();
        }

        @Override
        public boolean hasMod(String modId) {
            return FabricLoader.getInstance().isModLoaded(modId);
        }

        @Override
        public void extractQuads(SubmitNode submission, List<BlockModelApi.Quad> output) {
            if (submission instanceof ExtendedBlockModelSubmit blockModel) {
                BlockModelApi.extractBlockModelQuads(
                        blockModel.modelParts(),
                        blockModel.tintLayers(),
                        blockModel.tintColor(),
                        output);
                if (blockModel.mesh() != null) {
                    blockModel.mesh().forEach(quad -> extractQuad(blockModel, quad, output));
                }
            }
        }

        private void extractQuad(ExtendedBlockModelSubmit submission, QuadView quad, List<BlockModelApi.Quad> output) {
            if (submission.renderTypeFunction().apply(quad.chunkLayer()) == null) {
                return;
            }

            String textureLocation = quad.atlas().getTextureLocation().toString();
            int color = BlockModelApi.getBlockModelQuadColor(quad.tintIndex(), submission.tintLayers(), submission.tintColor());
            output.add(new BlockModelApi.Quad(
                    textureLocation,
                    getVertex(submission, quad, 0, color),
                    getVertex(submission, quad, 1, color),
                    getVertex(submission, quad, 2, color),
                    getVertex(submission, quad, 3, color)));
        }

        private BlockModelApi.Vertex getVertex(ExtendedBlockModelSubmit submission, QuadView quad, int index, int color) {
            Vector3f pos = submission.pose().pose().transformPosition(quad.x(index), quad.y(index), quad.z(index), new Vector3f());
            int vertexColor = ColorUtils.Int.multiply(quad.color(index), color);
            return new BlockModelApi.Vertex(pos.x(), pos.y(), pos.z(), vertexColor, quad.u(index), quad.v(index));
        }
    }

    private record VanillaWrapperRegistry<T>(Registry<T> registry) implements WrappedRegistry<T> {

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