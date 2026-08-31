package com.zergatul.cheatutils.schematics;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.zergatul.cheatutils.configs.adapters.BlockStateTypeAdapter;
import com.zergatul.cheatutils.modules.automation.Schematica;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.function.Function;

public class SchematicaFormatSmokeTest {

    private SchematicaFormatSmokeTest() {}

    public static void main(String[] args) throws Exception {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();

        verifyRoundTrip("test.litematic", LitematicaOutputFile::create);
        verifyRoundTrip("test.schem", SpongeSchematicaVersion1OutputFile::create);
        verifyLegacySchematic();
        verifyTransforms();
        verifyBlockStateJson();
        verifyRenderingHelpers();
        verifyMalformedInput();
    }

    private static void verifyRoundTrip(
            String filename,
            Function<SchematicaOutputData, Schematica.DownloadInfo> writer
    ) throws Exception {
        List<BlockState> palette = List.of(
                Blocks.AIR.defaultBlockState(),
                Blocks.STONE.defaultBlockState(),
                Blocks.OAK_STAIRS.defaultBlockState());
        int[] blocks = { 0, 1, 2, 1 };
        SchematicaOutputData source = new SchematicaOutputData(2, 1, 2, palette, blocks.clone());

        Schematica.DownloadInfo result = writer.apply(source);
        if (result.error() != null || result.data() == null) {
            throw new IllegalStateException("Failed to write " + filename + ": " + result.error());
        }

        SchemaFile parsed = SchemaFormatFactory.parse(result.data(), filename);
        if (parsed.getWidth() != 2 || parsed.getHeight() != 1 || parsed.getLength() != 2) {
            throw new IllegalStateException("Dimensions changed after round-trip: " + filename);
        }
        for (int z = 0; z < 2; z++) {
            for (int x = 0; x < 2; x++) {
                int index = z * 2 + x;
                BlockState expected = palette.get(blocks[index]);
                if (parsed.getBlockState(x, 0, z) != expected) {
                    throw new IllegalStateException("Block state changed after round-trip: " + filename);
                }
            }
        }
    }

    private static void verifyLegacySchematic() throws Exception {
        CompoundTag root = new CompoundTag();
        root.putShort("Width", (short) 1);
        root.putShort("Height", (short) 1);
        root.putShort("Length", (short) 1);
        root.putByteArray("Blocks", new byte[] { 1 });
        root.putByteArray("Data", new byte[] { 0 });
        root.putString("Materials", "Alpha");

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        NbtIo.writeCompressed(root, stream);
        SchemaFile parsed = SchemaFormatFactory.parse(stream.toByteArray(), "legacy.schematic");
        if (parsed.getBlockState(0, 0, 0) != VanillaMapping.get()[16]) {
            throw new IllegalStateException("Legacy Alpha block mapping changed.");
        }
    }

    private static void verifyTransforms() {
        PlacingSettings settings = new PlacingSettings();
        settings.transforms = new String[] { "Rotate Y +90deg", "Flip X" };
        PlacingConverter converter = new PlacingConverter(settings, 2, 3, 4);
        if (converter.getWidth() != 4 || converter.getHeight() != 3 || converter.getLength() != 2) {
            throw new IllegalStateException("Schematica transformed dimensions are incorrect.");
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(0, 0, 0);
        converter.convert(pos);
        if (pos.getX() < 0 || pos.getX() >= converter.getWidth() ||
                pos.getY() < 0 || pos.getY() >= converter.getHeight() ||
                pos.getZ() < 0 || pos.getZ() >= converter.getLength()) {
            throw new IllegalStateException("Schematica transform produced an out-of-bounds position.");
        }
    }

    private static void verifyBlockStateJson() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(BlockState.class, new BlockStateTypeAdapter())
                .create();

        BlockState stairs = Blocks.OAK_STAIRS.defaultBlockState();
        String json = gson.toJson(stairs, BlockState.class);
        if (gson.fromJson(json, BlockState.class) != stairs) {
            throw new IllegalStateException("Block-state properties changed after JSON round-trip.");
        }
        if (gson.fromJson("\"minecraft:stone\"", BlockState.class) != Blocks.STONE.defaultBlockState()) {
            throw new IllegalStateException("Legacy block-state JSON string is no longer supported.");
        }
    }

    private static void verifyRenderingHelpers() {
        boolean[] indexes = new boolean[WrapperRenderChunkRegion.SIZE * WrapperRenderChunkRegion.SIZE * WrapperRenderChunkRegion.SIZE];
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    int index = WrapperRenderChunkRegion.index(-1, -1, -1, x, y, z);
                    if (index < 0 || index >= indexes.length || indexes[index]) {
                        throw new IllegalStateException("Schematica render-region index is invalid.");
                    }
                    indexes[index] = true;
                }
            }
        }

        RecordingVertexConsumer recording = new RecordingVertexConsumer();
        new ShadedVertexConsumerWrapper(recording).color(200, 100, 50, 250);
        if (recording.r != 100 || recording.g != 80 || recording.b != 50 || recording.a != 150) {
            throw new IllegalStateException("Schematica block shading color is incorrect.");
        }
    }

    private static void verifyMalformedInput() throws Exception {
        try {
            SchemaFormatFactory.parse(new byte[0], "missing-extension");
            throw new IllegalStateException("Filename without an extension was accepted.");
        } catch (InvalidFormatException expected) {
            // expected
        }

        try {
            VarIntFormat.decode(new byte[] { (byte) 0x80 });
            throw new IllegalStateException("Truncated VarInt was accepted.");
        } catch (InvalidFormatException expected) {
            // expected
        }
    }

    private static class RecordingVertexConsumer implements VertexConsumer {

        private int r;
        private int g;
        private int b;
        private int a;

        @Override
        public VertexConsumer vertex(double x, double y, double z) { return this; }

        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            this.r = r;
            this.g = g;
            this.b = b;
            this.a = a;
            return this;
        }

        @Override
        public VertexConsumer uv(float u, float v) { return this; }

        @Override
        public VertexConsumer overlayCoords(int u, int v) { return this; }

        @Override
        public VertexConsumer uv2(int u, int v) { return this; }

        @Override
        public VertexConsumer normal(float x, float y, float z) { return this; }

        @Override
        public void endVertex() {}

        @Override
        public void defaultColor(int r, int g, int b, int a) {}

        @Override
        public void unsetDefaultColor() {}
    }
}
