package com.zergatul.cheatutils.font.ttf;

import java.io.IOException;
import java.util.Optional;

public class FontDirectoryTable {

    public OffsetSubtable offset;
    public TableDirectory[] directory;

    public FontDirectoryTable(OffsetSubtable offset, TableDirectory[] directory) {
        this.offset = offset;
        this.directory = directory;
    }

    public static FontDirectoryTable read(TtfBinaryReader reader) throws IOException {
        OffsetSubtable offset = OffsetSubtable.read(reader);
        TableDirectory[] directory = new TableDirectory[offset.numTables];
        for (int i = 0; i < directory.length; i++) {
            directory[i] = TableDirectory.read(reader);
        }
        return new FontDirectoryTable(offset, directory);
    }

    public Optional<TableDirectory> getDirectory(String name) {
        int tag = SfntTag.toInt32(name);
        for (TableDirectory directory : directory) {
            if (directory.tag == tag) {
                return Optional.of(directory);
            }
        }
        return Optional.empty();
    }
}