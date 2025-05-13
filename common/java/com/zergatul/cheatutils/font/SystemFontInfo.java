package com.zergatul.cheatutils.font;

import com.zergatul.cheatutils.font.ttf.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBTTFontinfo;
import org.lwjgl.stb.STBTruetype;

import java.awt.*;
import java.awt.Font;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class SystemFontInfo {

    protected final String path;
    protected final String name;
    protected final boolean isMonospace;

    protected SystemFontInfo(String path, String name, boolean isMonospace) {
        this.name = name;
        this.path = path;
        this.isMonospace = isMonospace;
    }

    public abstract String getInfo();

    public String getName() {
        return name;
    }

    public abstract StbFont load();

    public abstract java.awt.Font createAwtFont() throws IOException, FontFormatException;

    @Override
    public String toString() {
        return name;
    }

    public static List<SystemFontInfo> fromFile(String path) throws IOException {
        if (path.endsWith(".ttc")) {
            return fromContainerFile(path);
        }
        if (path.endsWith(".ttf") || path.endsWith(".otf")) {
            return List.of(fromFontFile(path));
        }
        throw new IllegalStateException();
    }

    private static List<SystemFontInfo> fromContainerFile(String path) throws IOException {
        try (TtfBinaryReader reader = new TtfBinaryReader(path)) {
            TtcHeader header = TtcHeader.read(reader);
            List<SystemFontInfo> fonts = new ArrayList<>();
            int[] tableDirectoryOffsets = header.tableDirectoryOffsets;
            for (int i = 0; i < tableDirectoryOffsets.length; i++) {
                int offset = tableDirectoryOffsets[i];
                reader.seek(offset);
                TtfShortInfo info = readShortInfo(reader);
                fonts.add(new ContainedSystemFontInfo(path, i, offset, info));
            }
            return fonts;
        }
    }

    private static SystemFontInfo fromFontFile(String path) throws IOException {
        try (TtfBinaryReader reader = new TtfBinaryReader(path)) {
            TtfShortInfo info = readShortInfo(reader);
            return new TtfSystemFontInfo(path, info);
        }
    }

    private static TtfShortInfo readShortInfo(TtfBinaryReader reader) throws IOException {
        FontDirectoryTable directoryTable = FontDirectoryTable.read(reader);

        Optional<String> name = Optional.empty();

        TableDirectory directory = directoryTable.getDirectory("name").orElseThrow();
        reader.seek(directory.offset);
        NameTable nameTable = NameTable.read(reader);
        for (NameRecord record : nameTable.records) {
            if (record.nameId == NameIdentifiers.FULL_NAME) {
                name = Optional.of(record.getValue(reader, directory.offset + nameTable.stringOffset));
                break;
            }
        }

        directory = directoryTable.getDirectory("post").orElseThrow();
        reader.seek(directory.offset);
        PostTable postTable = PostTable.read(reader);

        return new TtfShortInfo(name.orElse("<BLANK>"), postTable.isMonospace());
    }

    private static class TtfSystemFontInfo extends SystemFontInfo {

        public TtfSystemFontInfo(String path, TtfShortInfo info) {
            super(path, info.name, info.isMonospace);
        }

        @Override
        public String getInfo() {
            return String.format("%s [%s]", name, path);
        }

        @Override
        public java.awt.Font createAwtFont() throws IOException, FontFormatException {
            try (InputStream stream = Files.newInputStream(Path.of(path))) {
                return java.awt.Font.createFont(Font.TRUETYPE_FONT, stream);
            }
        }

        @Override
        public StbFont load() {
            STBTTFontinfo fontInfo = STBTTFontinfo.calloc();
            byte[] data;
            try {
                data = Files.readAllBytes(Paths.get(path));
            } catch (IOException ex) {
                throw new CannotLoadFontException(ex);
            }
            ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
            buffer.put(data);
            buffer.flip();

            if (!STBTruetype.stbtt_InitFont(fontInfo, buffer)) {
                throw new CannotLoadFontException("Failed to init font.");
            }

            return new StbFont(fontInfo, buffer);
        }
    }

    private static class ContainedSystemFontInfo extends SystemFontInfo {

        private final int index;
        private final int offset;

        public ContainedSystemFontInfo(String path, int index, int offset, TtfShortInfo info) {
            super(path, info.name, info.isMonospace);
            this.index = index;
            this.offset = offset;
        }

        @Override
        public String getInfo() {
            return String.format("%s [%s #%s]", name, path, index);
        }

        @Override
        public Font createAwtFont() throws IOException, FontFormatException {
            try (InputStream stream = Files.newInputStream(Path.of(path))) {
                Font[] fonts = java.awt.Font.createFonts(stream);
                return fonts[index];
            }
        }

        @Override
        public StbFont load() {
            STBTTFontinfo fontInfo = STBTTFontinfo.calloc();
            byte[] data;
            try {
                data = Files.readAllBytes(Paths.get(path));
            } catch (IOException ex) {
                throw new CannotLoadFontException(ex);
            }
            ByteBuffer buffer = BufferUtils.createByteBuffer(data.length);
            buffer.put(data);
            buffer.flip();

            if (!STBTruetype.stbtt_InitFont(fontInfo, buffer, offset)) {
                throw new CannotLoadFontException("Failed to init font.");
            }

            return new StbFont(fontInfo, buffer);
        }
    }

    private record TtfShortInfo(String name, boolean isMonospace) {}
}