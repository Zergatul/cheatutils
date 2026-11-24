package com.zergatul.cheatutils.utils;

import com.zergatul.cheatutils.Constants;
import com.zergatul.cheatutils.concurrent.ProfilerSingleThreadExecutor;
import com.zergatul.cheatutils.controllers.BlockEventsProcessor;
import net.minecraft.client.gui.components.debug.DebugGroup;
import net.minecraft.client.gui.components.debug.DebugScreenDisplayer;
import net.minecraft.client.gui.components.debug.DebugScreenEntry;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jspecify.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class DebugScreenExtensions {

    private static final Identifier COMMON = Identifier.fromNamespaceAndPath(Constants.MOD_ID, Constants.MOD_ID);
    private static final DebugGroup COMMON_GROUP = DebugGroup.Builder.titleless().build();

    /** Takes the loader's registration, so it can run before the debug profile is loaded. */
    public static void register(BiConsumer<Identifier, DebugScreenEntry> registry) {
        registry.accept(COMMON, new CommonDebugScreenEntry());
    }

    private static class CommonDebugScreenEntry implements DebugScreenEntry {

        private final DecimalFormat format = new DecimalFormat("0.00");

        @Override
        public void display(DebugScreenDisplayer displayer, @Nullable Level level, @Nullable LevelChunk chunk1, @Nullable LevelChunk chunk2) {
            ProfilerSingleThreadExecutor executor = BlockEventsProcessor.INSTANCE.getExecutor();

            List<String> list = new ArrayList<>();
            list.add(String.format("%s BlockEvents thread: queue size=%s; successful=%d; failed=%d; rejected=%d; busy=%s;",
                    Constants.MOD_NAME,
                    executor.getQueueSize(),
                    executor.getSuccessful(),
                    executor.getFailed(),
                    executor.getRejected(),
                    format.format(executor.getBusyPercentage()) + "%"));

            displayer.addToGroup(COMMON_GROUP, list);
        }
    }
}