package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.ResourceHelper;
import net.minecraft.client.Minecraft;

import java.io.*;
import java.nio.file.Path;

public class LocalFilesHandler extends StaticFilesHandler {

    private final Path directory;

    public LocalFilesHandler() {
        this.directory = new File(Minecraft.getMinecraft().gameDir, "mods").toPath();
    }

    @Override
    protected InputStream open(String path) throws IOException {
        return ResourceHelper.openLocal(directory, path.substring("/local/".length()));
    }
}