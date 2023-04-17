package com.zergatul.cheatutils.schematics;

import java.util.ArrayList;
import java.util.List;

public class PlacingConverter {

    private int width;
    private int height;
    private int length;
    private final List<Processor> processors = new ArrayList<>();

    public PlacingConverter(PlacingSettings settings, int width, int height, int length) {
        this.width = width;
        this.height = height;
        this.length = length;

        if (settings.flipX) {
            processors.add(new FlipXProcessor());
        }

        if (settings.flipY) {
            processors.add(new FlipYProcessor());
        }

        switch (settings.rotateY) {
            case -90 -> processors.add(new RotateYRightProcessor());
            case 90 -> processors.add(new RotateYLeftProcessor());
            case 180 -> processors.add(new RotateY180Processor());
        }
    }

    public Vec3iMutable convert(int x, int y, int z) {
        Vec3iMutable vec = new Vec3iMutable(x, y, z);
        for (Processor processor: processors) {
            processor.convert(vec);
        }
        return vec;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getLength() {
        return length;
    }

    private static abstract class Processor {
        public abstract void convert(Vec3iMutable vec);
    }

    private class FlipXProcessor extends Processor {
        private final int width;

        public FlipXProcessor() {
            this.width = PlacingConverter.this.width;
        }

        @Override
        public void convert(Vec3iMutable vec) {
            vec.x = width - vec.x;
        }
    }

    private class FlipYProcessor extends Processor {
        private final int height;

        public FlipYProcessor() {
            this.height = PlacingConverter.this.height;
        }

        @Override
        public void convert(Vec3iMutable vec) {
            vec.y = height - vec.y;
        }
    }

    private class RotateYLeftProcessor extends Processor {
        private final int width;

        public RotateYLeftProcessor() {
            this.width = PlacingConverter.this.length;
            int length = PlacingConverter.this.width;
            PlacingConverter.this.width = width;
            PlacingConverter.this.length = length;
        }

        @Override
        public void convert(Vec3iMutable vec) {
            int z = vec.z;
            vec.z = vec.x;
            vec.x = width - z;
        }
    }

    private class RotateYRightProcessor extends Processor {
        private final int length;

        public RotateYRightProcessor() {
            int width = PlacingConverter.this.length;
            this.length = PlacingConverter.this.width;
            PlacingConverter.this.width = width;
            PlacingConverter.this.length = length;
        }

        @Override
        public void convert(Vec3iMutable vec) {
            int x = vec.x;
            vec.x = vec.z;
            vec.z = length - x;
        }
    }

    private class RotateY180Processor extends Processor {
        private final int width;
        private final int length;

        public RotateY180Processor() {
            this.width = PlacingConverter.this.width;
            this.length = PlacingConverter.this.length;
        }

        @Override
        public void convert(Vec3iMutable vec) {
            vec.x = width - vec.x;
            vec.z = length - vec.z;
        }
    }

    public static class Vec3iMutable {
        public int x;
        public int y;
        public int z;

        public Vec3iMutable(int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}