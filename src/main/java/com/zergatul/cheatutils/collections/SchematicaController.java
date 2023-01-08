package com.zergatul.cheatutils.collections;

import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.configs.SchematicaConfig;
import com.zergatul.cheatutils.schematics.PlacingSettings;
import com.zergatul.cheatutils.schematics.SchematicFile;
import com.zergatul.cheatutils.utils.JavaRandom;
import com.zergatul.cheatutils.wrappers.ModApiWrapper;
import com.zergatul.cheatutils.wrappers.events.RenderWorldLastEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SchematicaController {

    public static final SchematicaController instance = new SchematicaController();

    private final Minecraft mc = Minecraft.getInstance();
    private final List<Entry> entries = new ArrayList<>();
    private final RandomSource random = new JavaRandom(0);

    private SchematicaController() {
        ModApiWrapper.RenderWorldLast.add(this::render);
    }

    public synchronized void place(SchematicFile file, PlacingSettings placing) {
        entries.add(new Entry(file, placing));
    }

    private synchronized void render(RenderWorldLastEvent event) {
        SchematicaConfig config = ConfigStore.instance.getConfig().schematicaConfig;
        if (!config.enabled) {
            return;
        }

        if (mc.level == null) {
            return;
        }

        for (Entry entry: entries) {
            for (var mapEntry: entry.blocks.entrySet()) {
                BlockPos pos = mapEntry.getKey();
                Block block = mapEntry.getValue();
                BlockState state = mc.level.getBlockState(pos);
                if (config.showMissingBlocks) {
                    if (state.isAir()) {
                        BakedModel model = mc.getBlockRenderer().getBlockModel(block.defaultBlockState());
                        List<BakedQuad> quads = model.getQuads(null, null, random, ModelData.EMPTY, null);
                        for (BakedQuad quad: quads) {

                        }
                    }
                }

            }
        }
    }

    private static class Entry {

        public final int x1, x2, y1, y2, z1, z2;
        public final Map<BlockPos, Block> blocks;

        public Entry(SchematicFile file, PlacingSettings placing) {
            x1 = placing.x;
            x2 = x1 + file.getWidth();
            y1 = placing.y;
            y2 = y1 + file.getHeight();
            z1 = placing.z;
            z2 = z1 + file.getLength();

            blocks = new HashMap<>();
            for (int x = 0; x < file.getWidth(); x++) {
                for (int y = 0; y < file.getHeight(); y++) {
                    for (int z = 0; z < file.getLength(); z++) {
                        Block block = file.getBlock(x, y, z);
                        if (block != Blocks.AIR) {
                            blocks.put(new BlockPos(x1 + x, y1 + y, z1 + z), block);
                        }
                    }
                }
            }
        }
    }
}