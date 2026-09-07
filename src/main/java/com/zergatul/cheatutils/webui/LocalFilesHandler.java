package com.zergatul.cheatutils.webui;

import com.zergatul.cheatutils.utils.ResourceHelper;
import java.io.*;
import java.nio.file.Path;

public class LocalFilesHandler extends StaticFilesHandler {
    private final Path directory;

    public LocalFilesHandler(Path directory) {
        this.directory = directory;
    }

    @Override
    protected InputStream open(String path) throws IOException {
        return ResourceHelper.openLocal(directory, path.substring("/local/".length()));
    }
}
