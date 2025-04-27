package com.zergatul.cheatutils.font.ttf;

import java.io.IOException;

public class OffsetSubtable {

    public int scalerType;
    public int numTables;
    public int searchRange;
    public int entrySelector;
    public int rangeShift;

    public static OffsetSubtable read(TtfBinaryReader reader) throws IOException {
        OffsetSubtable table = new OffsetSubtable();
        table.scalerType = reader.readUInt32();
        table.numTables = reader.readUInt16();
        table.searchRange = reader.readUInt16();
        table.entrySelector = reader.readUInt16();
        table.rangeShift = reader.readUInt16();
        return table;
    }
}