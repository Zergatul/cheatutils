package com.zergatul.cheatutils.schematics;

import it.unimi.dsi.fastutil.ints.IntArrayList;

public class VarIntDecoder {

    public static int[] decode(byte[] data) throws InvalidFormatException {
        IntArrayList list = new IntArrayList();

        int i = 0;
        while (i < data.length) {
            int value = 0;
            int length = 0;
            while (true) {
                value |= (data[i] & 0x7F) << (length++ * 7);
                if (length > 5) {
                    throw new InvalidFormatException("VarInt too big (probably corrupted data).");
                }
                if ((data[i] & 0x80) != 0x80) {
                    i++;
                    break;
                }
                i++;
            }
            list.add(value);
        }

        return list.toIntArray();
    }
}