package com.zergatul.cheatutils.modules.esp;

import com.zergatul.cheatutils.common.Events;
import com.zergatul.cheatutils.configs.Config;
import com.zergatul.cheatutils.configs.ConfigStore;
import com.zergatul.cheatutils.controllers.BlockEventsProcessor;
import com.zergatul.cheatutils.common.events.RenderWorldLastEvent;
import com.zergatul.cheatutils.render.Position3dColorRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;

import java.awt.*;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class EndCityChunks {

    public static final EndCityChunks instance = new EndCityChunks();

    private final Minecraft mc = Minecraft.getInstance();

    private EndCityChunks() {
        Events.AfterRenderWorld.add(this::render);
    }

    private void render(RenderWorldLastEvent event) {
        Config config = ConfigStore.instance.getConfig();
        if (!EspGlobal.enabled || !config.endCityChunksConfig.enabled) {
            return;
        }

        if (mc.level == null || mc.level.dimension() != Level.END) {
            return;
        }

        Position3dColorRenderer renderer = Position3dColorRenderer.getInstance();
        renderer.begin();

        Vec3 cameraPos = event.getCameraPos();

        AtomicReferenceArray<LevelChunk> chunks = BlockEventsProcessor.instance.getRawChunks();
        for (int i = 0; i < chunks.length(); i++) {
            LevelChunk chunk = chunks.get(i);
            if (chunk == null) {
                continue;
            }

            int sx = Math.floorMod(chunk.getPos().x(), 20);
            int sz = Math.floorMod(chunk.getPos().z(), 20);
            boolean isEndCityChunk = 0 <= sx && sx <= 8 && 0 <= sz && sz <= 8;
            if (isEndCityChunk) {
                int x1 = chunk.getPos().x() * 16;
                int z1 = chunk.getPos().z() * 16;
                int x2 = x1 + 16;
                int z2 = z1 + 16;
                float r = sx == 4 || sz == 4 ? 1f : 0f;
                float g = 1f;
                float b = 0f;
                for (float y = 32.1f; y < 100; y += 32) {
                    renderer.quad(
                            (float) (x1 - cameraPos.x), (float) (y - cameraPos.y), (float) (z1 - cameraPos.z),
                            (float) (x1 - cameraPos.x), (float) (y - cameraPos.y), (float) (z2 - cameraPos.z),
                            (float) (x2 - cameraPos.x), (float) (y - cameraPos.y), (float) (z2 - cameraPos.z),
                            (float) (x2 - cameraPos.x), (float) (y - cameraPos.y), (float) (z1 - cameraPos.z),
                            new Color(r, g, b, 0.1f).getRGB());
                }
            }
        }

        renderer.end(event.getMvp());
    }
}