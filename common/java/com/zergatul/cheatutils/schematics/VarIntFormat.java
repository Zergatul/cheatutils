package com.zergatul.cheatutils.schematics;

import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

public class VarIntFormat {

    public static byte[] encode(int[] values) {
        ByteArrayList list = new ByteArrayList();
        for (int value : values) {
            while (true) {
                if ((value >>> 7) == 0) {
                    list.add((byte) (value & 0x7F));
                    break;
                } else {
                    list.add((byte) ((value & 0x7F) | 0x80));
                    value >>>= 7;
                }
            }
        }
        return list.toByteArray();
    }

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