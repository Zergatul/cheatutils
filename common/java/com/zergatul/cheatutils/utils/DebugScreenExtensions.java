package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.ModMain;
import com.zergatul.cheatutils.concurrent.ProfilerSingleThreadExecutor;
import com.zergatul.cheatutils.controllers.BlockEventsProcessor;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntries;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class DebugScreenExtensions {

    private static final ResourceLocation COMMON = ResourceLocation.fromNamespaceAndPath(ModMain.MODID, ModMain.MODID);

    public static void register() {
        DebugScreenEntries.register(COMMON, new CommonDebugScreenEntry());
    }

    private static class CommonDebugScreenEntry implements DebugScreenEntry {

        private final DecimalFormat format = new DecimalFormat("0.00");

        @Override
        public void display(DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk chunk1, @Nullable LevelChunk chunk2) {
            ProfilerSingleThreadExecutor executor = BlockEventsProcessor.instance.getExecutor();

            List<String> list = new ArrayList<>();
            list.add(String.format("CheatUtils BlockEvents thread: queue size=%s; successful=%d; failed=%d; rejected=%d; busy=%s;",
                    executor.getQueueSize(),
                    executor.getSuccessful(),
                    executor.getFailed(),
                    executor.getRejected(),
                    format.format(executor.getBusyPercentage()) + "%"));

            displayer.addToGroup(COMMON, list);
        }
    }
}